package org.hawk.util.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.util.HawkTickable;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.google.gson.JsonObject;

public class HawkChatService extends HawkTickable {
	/**
	 * zmq对象
	 */
	private HawkZmq chatZmq = null;
	/**
	 * 接收数据缓冲区
	 */
	private byte[] bytes = null;
	/**
	 * 信息发送队列
	 */
	private BlockingQueue<String> flushQueue;
	/**
	 * 实例对象
	 */
	private static HawkChatService instance = null;

	/**
	 * 获取全局实例对象
	 * 
	 * @return
	 */
	public static HawkChatService getInstance() {
		if (instance == null) {
			instance = new HawkChatService();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private HawkChatService() {
		flushQueue = new LinkedBlockingQueue<String>();
	}

	/**
	 * 初始化聊天终端服务
	 * 
	 * @param addr
	 * @return
	 */
	public boolean init(String addr, String gameName, String platform, int serverId) {
		bytes = new byte[1024 * 1024];
		if (chatZmq == null) {
			chatZmq = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.DEALER);
			String identify = String.format("%s.%s.%d", gameName, platform, serverId);
			chatZmq.setIdentity(identify.getBytes());
			// 开始连接
			if (!chatZmq.connect(addr)) {
				HawkLog.logPrintln("chat service init failed: " + addr);
				chatZmq = null;
				return false;
			}
			HawkLog.logPrintln("chat service init success, addr: " + addr);
			HawkApp.getInstance().addTickable(this);
		}
		return true;
	}

	/**
	 * 聊天通知
	 * 
	 * @param playerId
	 * @param playerName
	 * @param chatMsg
	 * @param chatChannel
	 */
	public void notifyChatInfo(int playerId, String playerName, String chatMsg, String transFlag) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("playerId", playerId);
		jsonObject.addProperty("playerName", playerName);
		jsonObject.addProperty("chatMsg", chatMsg);
		jsonObject.addProperty("transFlag", transFlag);

		// 添加到发送队列
		flushQueue.add(jsonObject.toString());
	}

	@Override
	public void onTick() {
		if (chatZmq != null) {
			// 接收监控服务器的回复信息
			try {
				while (chatZmq.pollEvent(HawkZmq.HZMQ_EVENT_READ, 0) > 0) {
					int recvSize = chatZmq.recv(bytes, 0);
					if (recvSize > 0) {
						String msg = new String(bytes, 0, recvSize, "UTF-8");
						if (HawkApp.getInstance() != null) {
							HawkApp.getInstance().onChatMonitorNotify(msg);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			// 发送队列消息
			try {
				while (flushQueue.size() > 0) {
					String msg = flushQueue.poll();
					chatZmq.send(msg.getBytes(), 0);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
