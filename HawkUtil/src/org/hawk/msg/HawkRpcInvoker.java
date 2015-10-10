package org.hawk.msg;

import org.hawk.app.HawkAppObj;

/**
 * rpc调用响应器
 * 
 * @author hawk
 */
public abstract class HawkRpcInvoker {
	/**
	 * 携带参数
	 */
	protected Object[] params;
	
	/**
	 * 默认构造
	 */
	public HawkRpcInvoker() {
	}
	
	/**
	 * 携带应用逻辑层参数, 常用
	 * 
	 * @param params
	 */
	public HawkRpcInvoker(Object... params) {
		this.params = params;
	}
	
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
