package org.hawk.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;

/**
 * zmq代理对象事件封装
 * 
 * @author hawk
 * 
 */
public class HawkZmqProxy {
	/**
	 * 前端对象
	 */
	private HawkZmq frontend;
	/**
	 * 后端对象
	 */
	private HawkZmq backend;
	/**
	 * 双向通信
	 */
	private boolean bothway;
	/**
	 * zmq事件查询对象数组
	 */
	ZMQ.PollItem[] pollItems;

	/**
	 * 构造函数
	 * 
	 * @param frontend
	 * @param backend
	 * @param bothway
	 */
	public HawkZmqProxy(HawkZmq frontend, HawkZmq backend, boolean bothway) {
		this.frontend = frontend;
		this.backend = backend;
		this.bothway = bothway;
		this.pollItems = new ZMQ.PollItem[] { new ZMQ.PollItem(frontend.getSocket(), ZMQ.Poller.POLLIN), bothway ? new ZMQ.PollItem(backend.getSocket(), ZMQ.Poller.POLLIN) : null };
	}

	/**
	 * 获取前端对象
	 * 
	 * @return
	 */
	public HawkZmq getFrontend() {
		return frontend;
	}

	/**
	 * 获取后端对象
	 * 
	 * @return
	 */
	public HawkZmq getBackend() {
		return backend;
	}

	/**
	 * 获取poll实体对象
	 * 
	 * @return
	 */
	public PollItem[] getPollItems() {
		return pollItems;
	}

	/**
	 * 是否双向通信
	 * 
	 * @return
	 */
	public boolean isBothway() {
		return bothway;
	}

	/**
	 * 检查事件
	 * 
	 * @param timeout
	 * @return
	 */
	public boolean pollEvent(int timeout) {
		return ZMQ.poll(pollItems, bothway ? 2 : 1, timeout) > 0;
	}

	/**
	 * 清理数据
	 */
	public void clear() {
		frontend = null;
		backend = null;
		pollItems = null;
	}
}
