package org.hawk.msg;

import org.hawk.app.HawkAppObj;

/**
 * rpc调用响应器
 * 
 * @author hawk
 */
public abstract class HawkRpcInvoker {
	/**
	 * 消息响应
	 * 
	 * @param targetObj
	 * @param msg
	 * @return
	 */
	public abstract boolean onMessage(HawkAppObj targetObj, HawkMsg msg); 
	
	/**
	 * 消息完成
	 * 
	 * @param callerObj
	 * @return
	 */
	public abstract boolean onComplete(HawkAppObj callerObj);
}
