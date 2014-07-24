package org.hawk.app.task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
	 * 对象id列表
	 */
	List<HawkXID> xidList;
	/**
	 * 是否可锁定
	 */
	AtomicBoolean lockEnable;

	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	protected HawkTickTask() {
		super(HawkTaskManager.TASK_TICK);

		xidList = new LinkedList<HawkXID>();
		lockEnable = new AtomicBoolean();
		lockEnable.set(true);
	}

	/**
	 * 重设id列表
	 * 
	 * @param xidList
	 */
	public void resetXids(Collection<HawkXID> xidList) {
		this.xidList.clear();
		if (xidList != null) {
			// 拷贝参数大小
			for (HawkXID xid : xidList) {
				this.xidList.add(xid);
			}
		}
	}

	/**
	 * 执行tick任务
	 */
	@Override
	protected int run() {
		for (HawkXID xid : xidList) {
			try {
				HawkApp.getInstance().dispatchTick(xid);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return 0;
	}

	/**
	 * 锁定
	 * 
	 * @return
	 */
	public boolean lock() {
		return lockEnable.compareAndSet(true, false);
	}

	/**
	 * 解锁
	 * 
	 * @return
	 */
	public boolean unlock() {
		return lockEnable.compareAndSet(false, true);
	}

	/**
	 * 清理重置任务
	 */
	@Override
	protected boolean clear() {
		if (xidList != null) {
			xidList.clear();
		}

		// 缓存线程任务
		HawkTaskManager.getInstance().releaseTask(this);

		// 解除锁定
		unlock();
		return super.clear();
	}

	/**
	 * 克隆创建对象
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkTickTask();
	}
}
