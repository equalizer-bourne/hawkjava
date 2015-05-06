package org.hawk.app;

import org.hawk.listener.HawkListener;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

/**
 * 对象功能模块
 * 
 * @author hawk
 */
public class HawkObjModule extends HawkListener {
	/**
	 * 模块宿主对象
	 */
	protected HawkAppObj appObj;

	/**
	 * 模块构造函数
	 * 
	 * @param appObj
	 */
	public HawkObjModule(HawkAppObj appObj) {
		this.appObj = appObj;
	}

	/**
	 * 获取模块宿主对象
	 * 
	 * @return
	 */
	public HawkAppObj getAppObj() {
		return this.appObj;
	}

	/**
	 * 更新
	 * 
	 * @return
	 */
	public boolean onTick() {
		return true;
	}

	/**
	 * 消息响应
	 * 
	 * @param msg
	 * @return
	 */
	public boolean onMessage(HawkMsg msg) {
		return super.invokeMessage(appObj, msg);
	}

	/**
	 * 协议响应
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean onProtocol(HawkProtocol protocol) {
		return super.invokeProtocol(appObj, protocol);
	}
}
