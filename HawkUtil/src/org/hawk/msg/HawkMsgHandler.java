package org.hawk.msg;

import org.hawk.app.HawkAppObj;

/**
 * 消息处理句柄
 * 
 * @author hawk
 */
public interface HawkMsgHandler {
	/**
	 * 消息处理器
	 * 
	 * @param appObj
	 * @param msg
	 * @return
	 */
	public boolean onMessage(HawkAppObj appObj, HawkMsg msg);
}
