package org.hawk.msg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkException;

/**
 * 消息远程过程调用
 * @author hawk
 */
public class HawkMsgRpc {
	/**
	 * 响应者列表
	 */
	private Map<Integer, HawkRpcInvoker> rpcInvokerMap;
	
	/**
	 * 全局实例对象
	 */
	private static HawkMsgRpc instance = null;
	
	/**
	 * 获取rpc实例
	 * 
	 * @return
	 */
	public static HawkMsgRpc getInstance() {
		if (instance == null) {
			instance = new HawkMsgRpc();
		}
		return instance;
	}
	
	/**
	 * 构造
	 */
	private HawkMsgRpc() {
		rpcInvokerMap = new ConcurrentHashMap<Integer, HawkRpcInvoker>();
	}
	
	/**
	 * 消息远程调用
	 * 
	 * @param msg 必须有target和source
	 * @return
	 */
	public boolean call(HawkMsg msg, HawkRpcInvoker invoker) {
		if (invoker == null) {
			return false;
		}
		
		if (!msg.getTarget().isValid() || !msg.getSource().isValid()) {
			throw new RuntimeException("rpc message need target and source");
		}
		
		int rpcId = msg.buildRpcMsg();
		if (rpcId > 0) {
			rpcInvokerMap.put(rpcId, invoker);
			return HawkApp.getInstance().postMsg(msg);
		}
		return false;
	}
	
	/**
	 * 响应处理, 系统调用
	 * 
	 * @param msg
	 */
	public void onRequest(HawkAppObj targetObj, HawkMsg msg) {
		if (msg.getType() == HawkMsg.MSG_RPC_REQ && msg.getRpcId() > 0 && rpcInvokerMap.containsKey(msg.getRpcId())) {
			HawkRpcInvoker invoker = rpcInvokerMap.get(msg.getRpcId());
			
			try {
				invoker.onMessage(targetObj, msg);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			HawkMsg respMsg = HawkMsg.valueOf(msg.getMsg(), msg.getSource(), msg.getTarget());
			respMsg.setType(HawkMsg.MSG_RPC_RESP).setRpcId(msg.getRpcId());
			HawkApp.getInstance().postMsg(respMsg);
		}
	}
	
	/**
	 * 回调处理, 系统调用
	 * 
	 * @param msg
	 */
	public void onResponse(HawkAppObj callerObj, HawkMsg msg) {
		if (msg.getType() == HawkMsg.MSG_RPC_RESP && msg.getRpcId() > 0 && rpcInvokerMap.containsKey(msg.getRpcId())) {
			HawkRpcInvoker invoker = rpcInvokerMap.remove(msg.getRpcId());
			
			try {
				invoker.onComplete(callerObj);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
