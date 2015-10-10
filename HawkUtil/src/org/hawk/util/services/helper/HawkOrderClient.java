package org.hawk.util.services.helper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.log.HawkLog;
import org.hawk.net.mq.HawkMQClient;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.google.gson.JsonObject;

public class HawkOrderClient extends HawkMQClient {
	/**
	 * 服务器地址
	 */
	private String addr;
	/**
	 * 客户端标识
	 */
	private String identify;
	/**
	 * 连接是否有效
	 */
	private boolean connectOK;
	/**
	 * 订单服务的客户端通讯对象
	 */
	private HawkZmq orderZmq;
	/**
	 * 上次重连时间
	 */
	private long lastReconnectTime = 0;
	/**
	 * 重连周期
	 */
	private long reconnectPeriod = 5000;
	/**
	 * 协议接收队列
	 */
	BlockingQueue<String> recvQueue;
	
	/**
	 * 构造函数
	 * 
	 * @param identify
	 */
	public HawkOrderClient() {
		super();
		recvQueue = new LinkedBlockingQueue<String>();
	}

	/**
	 * 初始化订单服务
	 * 
	 * @param addr
	 * @return
	 */
	public boolean init(String identify, String addr) {
		this.addr = addr;
		this.identify = identify;
		// 初始化网络服务对象
		if (addr.indexOf("tcp://") >= 0) {
			orderZmq = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.DEALER);
			orderZmq.setIdentity(identify.getBytes());
			orderZmq.startMonitor(HawkZmq.SocketEvent.CONNECTED | HawkZmq.SocketEvent.DISCONNECTED);
			if (!orderZmq.connect(addr)) {
				return false;
			}
		} else {
			String items[] = addr.split(":");
			connect(items[0], Integer.valueOf(items[1]), 1000);
			lastReconnectTime = HawkTime.getMillisecond();
		}
		return true;
	}
	
	/**
	 * 连接是否有效
	 * 
	 * @return
	 */
	public boolean isConnectOK() {
		return connectOK;
	}
	
	/**
	 * 会话开启
	 */
	@Override
	protected void onOpened() {
		super.onOpened();
		// 设置当前状态
		lastReconnectTime = HawkTime.getMillisecond();
		connectOK = true;
		// 绑定标识
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("identify", identify);
		sendNotify(jsonObject.toString());
	}
	
	/**
	 * 接收到协议的回调
	 * 
	 * @param message
	 * @return
	 */
	@Override
	protected boolean onReceived(Object message) {
		if (message instanceof HawkProtocol) {
			try {
				HawkProtocol protocol = (HawkProtocol) message;
				String data = new String(protocol.getOctets().getBuffer().array(), 0, protocol.getSize());
				synchronized (recvQueue) {
					recvQueue.add(data);
				}
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}
	
	/**
	 * 会话关闭通知
	 */
	protected void onClosed() {
		super.onClosed();
		lastReconnectTime = HawkTime.getMillisecond();
		connectOK = false;
	}
	
	/**
	 * 网络发送请求
	 * 
	 * @param identify
	 * @param data
	 * @return
	 */
	private boolean sendNotify(String data) {
		try {
			if (orderZmq != null) {
				orderZmq.send(data.getBytes(), 0);
			} else {
				sendProtocol(HawkProtocol.valueOf(0, data.getBytes()));
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 更新zmq服务对象的网络事件
	 */
	private void updateZmqEvent() {
		// 检测连接是否正常
		try {
			int events = orderZmq.updateMonitor(0);
			if ((events & HawkZmq.SocketEvent.CONNECTED) > 0) {
				connectOK = true;
				HawkLog.logPrintln("order zmq client connected: " + this.addr);
			} else if ((events & HawkZmq.SocketEvent.DISCONNECTED) > 0) {
				connectOK = false;
				HawkLog.logPrintln("order zmq client disconnected: " + this.addr);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
				
		// 接收支付服务器消息
		while (orderZmq.pollEvent(HawkZmq.HZMQ_EVENT_READ, 0) > 0) {
			try {
				byte[] bytes = new byte[4096];
				int size = orderZmq.recv(bytes, 0);
				if (size > 0) {
					String data = new String(bytes, 0, size, "UTF-8");
					synchronized (recvQueue) {
						recvQueue.add(data);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 检测连接
	 */
	public boolean checkConnection() {
		if (orderZmq == null && !isValid()) {
			long curTime = HawkTime.getMillisecond();
			if (curTime - lastReconnectTime >= reconnectPeriod) {
				lastReconnectTime = curTime;
				tryReconnect(100);
				HawkLog.logPrintln("order client reconnect, addr: " + getAddress().toString());
			}
		}
		return connectOK;
	}
	
	/**
	 * 接收网络请求
	 * 
	 * @param infoQueue
	 * @return
	 */
	public boolean updateRecvQueue(BlockingQueue<String> infoQueue) {
		try {
			// 更新zmq消息并接收到队列
			if (orderZmq != null) {
				updateZmqEvent();
			}

			// 写出队列
			synchronized (recvQueue) {
				infoQueue.addAll(recvQueue);
				recvQueue.clear();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return infoQueue.size() > 0;
	}
	
	/**
	 * 写出发送队列所有内容
	 * 
	 * @param infoQueue
	 */
	public void flushSendQueue(BlockingQueue<String> infoQueue) {
		while (infoQueue.size() > 0) {
			String info = infoQueue.poll();
			sendNotify(info);
		}
	}
}
