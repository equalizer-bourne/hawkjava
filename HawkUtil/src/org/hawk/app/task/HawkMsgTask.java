package org.hawk.app.task;

import java.util.LinkedList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.cache.HawkCacheObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.HawkMsgProxy;
import org.hawk.msg.HawkMsgRpc;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

/**
 * 消息类型任务
 * 
 * @author hawk
 */
public class HawkMsgTask extends HawkTask {
	/**
	 * 对象id
	 */
	HawkXID xid;
	/**
	 * 消息对象
	 */
	HawkMsg msg;
	/**
	 * 消息代理对象
	 */
	HawkMsgProxy msgProxy;
	/**
	 * 对象id列表
	 */
	List<HawkXID> xidList;

	/**
	 * 构造函数
	 * 
	 */
	protected HawkMsgTask() {
	}
	
	/**
	 * 构造函数
	 * 
	 * @param xid
	 * @param msgProxy
	 */
	protected HawkMsgTask(HawkXID xid, HawkMsgProxy msgProxy) {
		this.xid = xid;
		this.msgProxy = msgProxy;
	}

	/**
	 * 构造函数
	 * 
	 * @param xid
	 * @param msg
	 */
	protected HawkMsgTask(HawkXID xid, HawkMsg msg) {
		setParam(xid, msg);
	}
	
	/**
	 * 构造函数
	 * 
	 * @param xidList
	 * @param msg
	 */
	protected HawkMsgTask(List<HawkXID> xidList, HawkMsg msg) {
		setParam(xidList, msg);
	}

	/**
	 * 获取对象id
	 * 
	 * @return
	 */
	public HawkXID getXid() {
		return xid;
	}

	/**
	 * 获取对象id列表
	 * 
	 * @return
	 */
	public List<HawkXID> getXidList() {
		return xidList;
	}
	
	/**
	 * 设置任务参数
	 * 
	 * @param xid
	 * @param msg
	 */
	public void setParam(HawkXID xid, HawkMsg msg) {
		this.xid = xid;
		this.msg = msg;
	}

	/**
	 * 设置任务参数
	 * 
	 * @param xidList
	 * @param msg
	 */
	public void setParam(List<HawkXID> xidList, HawkMsg msg) {
		this.msg = msg;
		if (this.xidList == null) {
			this.xidList = new LinkedList<HawkXID>();
		}
		this.xidList.clear();
		this.xidList.addAll(xidList);
	}

	/**
	 * 缓存对象清理
	 */
	@Override
	protected void clear() {
		xid = null;
		msg = null;
		if (xidList != null) {
			xidList.clear();
		}
	}

	/**
	 * 缓存对象克隆
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkMsgTask();
	}
	
	/**
	 * 执行消息任务
	 */
	@Override
	protected int run() {
		try {
			boolean dispatchOK = false;
			// proxy请求
			if (msgProxy != null) {
				return onProxyMessage();
			}
			
			// 通用消息处理
			if (msg != null) {
				// rpc请求
				if (msg.getType() == HawkMsg.MSG_RPC_REQ || msg.getType() == HawkMsg.MSG_RPC_RESP) {
					return onRpcMessage();
				}
				
				if (xidList != null && xidList.size() > 0) {
					for (HawkXID xid : xidList) {
						dispatchOK = HawkApp.getInstance().dispatchMsg(xid, msg);
					}
				} else if (xid.isValid()) {
					dispatchOK = HawkApp.getInstance().dispatchMsg(xid, msg);
				}
			} else {
				HawkLog.errPrintln("dispatch message null");
			}

			if (!dispatchOK) {
				HawkLog.errPrintln("dispatch message failed, msgId: " + msg.getMsg());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * rpc消息处理
	 * 
	 * @return
	 */
	private int onRpcMessage() {
		// rpc请求
		if (msg.getType() == HawkMsg.MSG_RPC_REQ) {
			HawkObjBase<HawkXID, HawkAppObj> objBase = HawkApp.getInstance().lockObject(msg.getTarget());
			if (objBase != null) {
				try {
					if (msg.getType() == HawkMsg.MSG_RPC_REQ) {
						HawkMsgRpc.getInstance().onRequest(objBase.getImpl(), msg);
					} else if (msg.getType() == HawkMsg.MSG_RPC_RESP) {
						HawkMsgRpc.getInstance().onResponse(objBase.getImpl(), msg);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					objBase.unlockObj();
				}
			}
		}
		return 0;
	}
	
	/**
	 * proxy消息处理
	 * 
	 * @return
	 */
	private int onProxyMessage() {
		HawkObjBase<HawkXID, HawkAppObj> objBase = HawkApp.getInstance().lockObject(xid);
		if (objBase != null) {
			try {
				msgProxy.onInvoke(objBase.getImpl());
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				objBase.unlockObj();
			}
		}
		return 0;
	}
	
	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param msg
	 * @return
	 */
	public static HawkMsgTask valueOf(HawkMsg msg) {
		HawkMsgTask task = new HawkMsgTask(msg.getTarget(), msg);
		return task;
	}
	
	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param xid
	 * @param msg
	 * @return
	 */
	public static HawkMsgTask valueOf(HawkXID xid, HawkMsg msg) {
		HawkMsgTask task = new HawkMsgTask(xid, msg);
		return task;
	}

	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param xidList
	 * @param msg
	 * @return
	 */
	public static HawkMsgTask valueOf(List<HawkXID> xidList, HawkMsg msg) {
		HawkMsgTask task = new HawkMsgTask(xidList, msg);
		return task;
	}
	
	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param xid
	 * @param msg
	 * @return
	 */
	public static HawkMsgTask valueOf(HawkXID xid, HawkMsgProxy msgProxy) {
		HawkMsgTask task = new HawkMsgTask(xid, msgProxy);
		return task;
	}
}
