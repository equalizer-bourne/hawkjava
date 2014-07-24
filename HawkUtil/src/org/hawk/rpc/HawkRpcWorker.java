package org.hawk.rpc;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.zeromq.ZMQ;

public abstract class HawkRpcWorker {
	/**
	 * zmq响应对象
	 */
	HawkZmq responser;

	/**
	 * 初始化(地址eg: tcp://ip:addr, inproc://dealer_addr)
	 * 
	 * @param addr
	 * @return
	 */
	public boolean init(String addr) {
		responser = HawkZmqManager.getInstance().createZmq(ZMQ.REP);
		if (!responser.connect(addr)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 关闭对象
	 */
	public void close() {
		HawkZmqManager.getInstance().closeZmq(responser);
		responser = null;
	}

	/**
	 * 是否有请求事件
	 * 
	 * @param timeout
	 * @return
	 */
	public boolean hasRequest(int timeout) {
		if (responser != null) {
			return responser.pollEvent(HawkZmq.HZMQ_EVENT_READ, timeout) > 0;
		}
		return false;
	}

	/**
	 * 请求响应
	 * 
	 * @param protocol
	 * @param timeout
	 * @return
	 */
	public void response() {
		if (responser != null) {
			HawkProtocol request = responser.recvProtocol(0);
			if (request != null) {
				HawkProtocol response = response(request);
				responser.sendProtocol(response, 0);
			}
		}
	}

	/**
	 * 请求响应接口
	 * 
	 * @param protocol
	 * @return
	 */
	public abstract HawkProtocol response(HawkProtocol protocol);
}
