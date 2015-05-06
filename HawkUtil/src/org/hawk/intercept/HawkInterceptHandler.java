package org.hawk.intercept;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

public class HawkInterceptHandler {
	/**
	 * 帧更新
	 * 
	 * @return false表示不拦截, 否则拦截更新调用不往下进行
	 */
	public boolean onTick(HawkAppObj appObj) {
		return false;
	}
	
	/**
	 * 消息响应
	 * 
	 * @param msg
	 * @return false表示不拦截, 否则拦截消息不往下进行
	 */
	public boolean onMessage(HawkAppObj appObj, HawkMsg msg) {
		return false;
	}

	/**
	 * 协议响应
	 * 
	 * @param protocol
	 * @return false表示不拦截, 否则拦截协议不往下进行
	 */
	public boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol) {
		return false;
	}
}
