package org.hawk.app.task;

import java.util.LinkedList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.cache.HawkCacheObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.HawkMsgManager;
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
	 * 消息对象
	 */
	HawkMsg msg;
	/**
	 * 对象id
	 */
	HawkXID xid;
	/**
	 * 对象id列表
	 */
	List<HawkXID> xidList;

	/**
	 * 构造函数
	 * 
	 * @param xid
	 * @param msg
	 */
	protected HawkMsgTask(HawkXID xid, HawkMsg msg) {
		super(HawkTaskManager.TASK_MSG);

		setParam(xid, msg);
	}

	/**
	 * 构造函数
	 * 
	 * @param xidList
	 * @param msg
	 */
	protected HawkMsgTask(List<HawkXID> xidList, HawkMsg msg) {
		super(HawkTaskManager.TASK_MSG);

		this.msg = msg;
		if (this.xidList == null) {
			this.xidList = new LinkedList<HawkXID>();
		}
		resetXids(xidList);
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
		resetXids(xidList);
	}

	/**
	 * 重设id列表
	 * 
	 * @param xidList
	 */
	public void resetXids(List<HawkXID> xidList) {
		this.xidList.clear();
		if (xidList != null) {
			// 拷贝参数大小
			for (HawkXID xid : xidList) {
				this.xidList.add(xid);
			}
		}
	}

	/**
	 * 执行消息任务
	 */
	@Override
	public int run() {
		try {
			if (msg != null) {
				if (xidList != null) {
					for (HawkXID xid : xidList) {
						boolean dispatchOK = HawkApp.getInstance().dispatchMsg(xid, msg);
						if (!dispatchOK) {
							HawkLog.errPrintln("dispatch message failed, msgId: " + msg.getMsg());
						}
					}
				} else if (xid.isValid()) {
					boolean dispatchOK = HawkApp.getInstance().dispatchMsg(xid, msg);
					if (!dispatchOK) {
						HawkLog.errPrintln("dispatch message failed, msgId: " + msg.getMsg());
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 清理重置任务
	 */
	@Override
	protected boolean clear() {
		if (xidList != null) {
			xidList.clear();
		}

		// 缓存消息对象
		if (msg != null) {
			HawkMsgManager.getInstance().release(msg);
			msg = null;
		}

		// 缓存线程任务
		HawkTaskManager.getInstance().releaseTask(this);
		return super.clear();
	}

	/**
	 * 克隆创建对象
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkMsgTask(HawkXID.nullXid(), null);
	}
}
