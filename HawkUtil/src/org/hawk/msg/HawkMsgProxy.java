package org.hawk.msg;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.app.task.HawkMsgTask;

public abstract class HawkMsgProxy {
	/**
	 * 代理投递消息
	 * 
	 * @param targetObj
	 */
	public boolean post(HawkAppObj targetObj) {
		return HawkApp.getInstance().postMsgTask(HawkMsgTask.valueOf(targetObj.getXid(), this));
	}
	
	/**
	 * 被调用的回调函数
	 * 
	 * @param targetObj
	 * @return
	 */
	public abstract int onInvoke(HawkAppObj targetObj);
}
