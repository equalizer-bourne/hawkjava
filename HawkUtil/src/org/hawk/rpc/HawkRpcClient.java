package org.hawk.rpc;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.zeromq.ZMQ;

/**
 * rpc客户端封装
 * 
 * @author hawk
 */
public class HawkRpcClient {
	/**
	 * 请求zmq对象
	 */
	HawkZmq requester;

	/**
	 * 初始化(地址eg: tcp://ip:port, inproc://router_addr)
	 * 
	 * @param addr
	 * @return
	 */
	public boolean init(String addr) {
		requester = HawkZmqManager.getInstance().createZmq(ZMQ.REQ);
		if (!requester.connect(addr)) {
			return false;
		}
		return true;
	}

	/**
	 * 关闭对象
	 */
	public void close() {
		HawkZmqManager.getInstance().closeZmq(requester);
		requester = null;
	}

	/**
	 * 返回请求zmq对象
	 * 
	 * @return
	 */
	public HawkZmq getRequester() {
		return requester;
	}

	/**
	 * 请求调用
	 * 
	 * @param protocol
	 * @param timeout
	 * @return
	 */
	public HawkProtocol request(HawkProtocol protocol, int timeout) throws Exception {
		if (requester != null) {
			// 抛弃之前超时的数据, 接收, 丢弃
			requester.discardMsg();
			// 发送请求
			if (requester.sendProtocol(protocol, 0)) {
				// 接收响应
				if (requester.pollEvent(HawkZmq.HZMQ_EVENT_READ, timeout) > 0) {
					HawkProtocol response = requester.recvProtocol(0);
					if (response != null && response.getReserve() == protocol.getReserve()) {
						return response;
					}
				} else {
					throw new HawkException(String.format("rpc request timeout, protocol: %d, timeout: %d", protocol.getType(), timeout));
				}
			}
		}
		return null;
	}
}
