package org.hawk.app.task;

import java.util.LinkedList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.cache.HawkCacheObj;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

/**
 * 更新类型任务
 * 
 * @author hawk
 */
public class HawkTickTask extends HawkTask {
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
	 */
	protected HawkTickTask() {
	}
	
	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	protected HawkTickTask(HawkXID xid) {
		setParam(xid);
	}

	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	protected HawkTickTask(List<HawkXID> xidList) {
		setParam(xidList);
	}

	/**
	 * 设置任务参数
	 * 
	 * @param xid
	 * @param msg
	 */
	public void setParam(HawkXID xid) {
		this.xid = xid;
	}

	/**
	 * 设置任务参数
	 * 
	 * @param xidList
	 * @param msg
	 */
	public void setParam(List<HawkXID> xidList) {
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
		if (xidList != null) {
			xidList.clear();
		}
	}

	/**
	 * 对象克隆
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkTickTask();
	}
	
	/**
	 * 执行tick任务
	 */
	@Override
	protected int run() {
		if (xidList != null && xidList.size() > 0) {
			for (HawkXID xid : xidList) {
				try {
					HawkApp.getInstance().dispatchTick(xid);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} else {
			try {
				HawkApp.getInstance().dispatchTick(xid);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return 0;
	}

	/**
	 * 构建对象
	 * 
	 * @param xidList
	 * @return
	 */
	public static HawkTickTask valueOf(HawkXID xid) {
		HawkTickTask task = new HawkTickTask(xid);
		return task;
	}

	/**
	 * 构建对象
	 * 
	 * @param xidList
	 * @return
	 */
	public static HawkTickTask valueOf(List<HawkXID> xidList) {
		HawkTickTask task = new HawkTickTask(xidList);
		return task;
	}
}
