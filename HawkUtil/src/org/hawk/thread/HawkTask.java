package org.hawk.thread;

import org.hawk.cache.HawkCacheObj;

/**
 * 线程任务对象封装
 * 
 * @author hawk
 * 
 */
public abstract class HawkTask extends HawkCacheObj {
	/**
	 * 任务类型
	 */
	private int taskType;

	/**
	 * 构造函数
	 * 
	 * @param taskType
	 */
	public HawkTask(int taskType) {
		this.taskType = taskType;
	}

	/**
	 * 获取任务类型
	 * 
	 * @return
	 */
	public int getTaskType() {
		return taskType;
	}

	/**
	 * 设置任务类型
	 * 
	 * @param taskType
	 */
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	/**
	 * 线程调用的任务执行函数
	 * 
	 * @return
	 */
	protected abstract int run();

	/**
	 * 执行完之后的清理函数
	 * 
	 * @return
	 */
	@Override
	protected boolean clear() {
		return false;
	}
}
