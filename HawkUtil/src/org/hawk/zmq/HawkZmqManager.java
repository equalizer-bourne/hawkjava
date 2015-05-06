package org.hawk.zmq;

import java.util.Collection;
import java.util.LinkedList;

import org.zeromq.ZMQ;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.octets.HawkOctetsStream;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * ZMQ管理器对象
 * 
 * @author hawk
 * 
 */
public class HawkZmqManager {
	/**
	 * zmq上下文环境，创建时会传入使用线程数目
	 */
	ZMQ.Context context;
	/**
	 * 当前锁创建的zmq对象
	 */
	Collection<HawkZmq> zmqList;
	/**
	 * 公用buffer
	 */
	HawkOctetsStream stream;
	/**
	 * 单例使用
	 */
	static HawkZmqManager instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkZmqManager getInstance() {
		if (instance == null) {
			if (HawkOSOperator.isWindowsOS()) {
				System.loadLibrary("libzmq");
			} else {
				System.loadLibrary("zmq");
			}
			System.loadLibrary("jzmq");

			instance = new HawkZmqManager();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private HawkZmqManager() {
		zmqList = new LinkedList<HawkZmq>();
	}

	/**
	 * 初始化
	 * 
	 * @param threads线程数
	 * @return
	 */
	public boolean init(int threads) {
		// 检测
		if (!HawkNativeApi.checkHawk()) {
			return false;
		}
		
		return setupZmqCtx(threads);
	}

	/**
	 * 创建上下文
	 * 
	 * @param threads
	 * @return
	 */
	private boolean setupZmqCtx(int threads) {
		if (context == null) {
			HawkLog.logPrintln("zmqversion: " + ZMQ.getVersionString());
			context = ZMQ.context(threads);
		}
		return true;
	}

	/**
	 * 创建zmq对象
	 * 
	 * @param type
	 * @return
	 */
	public HawkZmq createZmq(int type) {
		if (context != null || setupZmqCtx(HawkZmq.HZMQ_CONTEXT_THREAD)) {
			HawkZmq zmq = new HawkZmq();
			try {
				if (zmq.init(context, type)) {
					zmqList.add(zmq);
					return zmq;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return null;
	}

	/**
	 * 关闭zmq对象
	 * 
	 * @param zmq
	 */
	public void closeZmq(HawkZmq zmq) {
		if (zmq != null) {
			zmq.close();
			zmqList.remove(zmq);
		}
	}

	/**
	 * 检测通用缓冲区
	 */
	public HawkOctetsStream checkStream() {
		if (stream == null) {
			stream = HawkOctetsStream.create(HawkZmq.HZMQ_BUFFER_SIZE, true);
		}
		stream.clear();
		return stream;
	}

	/**
	 * 桥接代理对象事件传递
	 * 
	 * @param zmqProxy
	 * @param timeout
	 * @param once
	 * @return
	 */
	public boolean proxyZmq(HawkZmqProxy zmqProxy, int timeout, boolean once) {
		if (zmqProxy == null || zmqProxy.getFrontend() == null || zmqProxy.getBackend() == null) {
			return false;
		}

		// 预备缓冲区
		checkStream();

		try {
			do {
				// 检查事件
				int events = ZMQ.poll(zmqProxy.pollItems, zmqProxy.isBothway() ? 2 : 1, timeout);
				if (events < 0) {
					HawkLog.errPrintln("zmqproxy poll event error");
					return false;
				}

				// 前端通道有数据可读取
				if ((zmqProxy.pollItems[0].readyOps() & ZMQ.Poller.POLLIN) != 0) {
					while (true) {
						// 接收
						stream.clear();
						if (zmqProxy.getFrontend().recv(stream.getBuffer(), 0) < 0) {
							HawkLog.errPrintln("zmqproxy frontend recv error");
							return false;
						}

						// 发送
						boolean recvMore = zmqProxy.getFrontend().isWaitRecv();
						if (!zmqProxy.getBackend().send(stream.getBuffer(), recvMore ? ZMQ.SNDMORE : 0)) {
							HawkLog.errPrintln("zmqproxy backend send error");
							return false;
						}

						// 单帧数据接收完成
						if (!recvMore) {
							break;
						}
					}
				}

				// 后端通道有数据可读取
				if (zmqProxy.isBothway() && (zmqProxy.pollItems[1].readyOps() & ZMQ.Poller.POLLIN) != 0) {
					while (true) {
						// 接收
						stream.clear();
						if (zmqProxy.getBackend().recv(stream.getBuffer(), 0) < 0) {
							HawkLog.errPrintln("zmqproxy backend recv error");
							return false;
						}

						// 发送
						boolean recvMore = zmqProxy.getBackend().isWaitRecv();
						if (!zmqProxy.getFrontend().send(stream.getBuffer(), recvMore ? ZMQ.SNDMORE : 0)) {
							HawkLog.errPrintln("zmqproxy frontend send error");
							return false;
						}

						// 单帧数据接收完成
						if (!recvMore) {
							break;
						}
					}
				}

				// 单次执行, 退出while循环
				if (once) {
					return events > 0;
				}

			} while (!once);
		} catch (Exception e) {
			// 异常退出
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
