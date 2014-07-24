package org.hawk.thread;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 线程类封装
 * 
 * @author hawk
 * 
 */
public class HawkThread extends Thread {
	/**
	 * 运行中
	 */
	private volatile boolean running;
	/**
	 * 等待退出
	 */
	private volatile boolean waitBreak;
	/**
	 * 线程任务队列
	 */
	private Deque<HawkTask> taskQueue;
	/**
	 * 任务队列互斥锁
	 */
	private Lock queueLock;
	/**
	 * 线程状态
	 */
	private ThreadState state;
	/**
	 * 添加的任务总数
	 */
	private long pushTaskCnt;
	/**
	 * 执行的任务总数
	 */
	private long popTaskCnt;

	/**
	 * 状态定义
	 * 
	 * @author hawk
	 * 
	 */
	private enum ThreadState {
		STATE_NONE, STATE_RUNNING, STATE_CLOSING, STATE_CLOSED
	};

	/**
	 * 构造初始化
	 */
	protected HawkThread() {
		running = false;
		waitBreak = false;
		queueLock = new ReentrantLock();
		state = ThreadState.STATE_NONE;
		taskQueue = new LinkedList<HawkTask>();
	}

	/**
	 * 开始接口
	 */
	@Override
	public synchronized void start() {
		running = true;
		super.start();

		while (state != ThreadState.STATE_RUNNING) {
			HawkOSOperator.osSleep(10);
		}
	}

	/**
	 * 阻塞等待结束线程
	 * 
	 * @return
	 */
	protected boolean close(boolean waitBreak) {
		if (this.running && !this.waitBreak) {
			this.waitBreak = waitBreak;
			if (!waitBreak) {
				this.running = false;
			}

			// 等待线程结束
			state = ThreadState.STATE_CLOSING;
			try {
				join();
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			// 设置状态
			this.running = false;
			this.waitBreak = false;
			this.state = ThreadState.STATE_CLOSED;
		}
		return true;
	}

	/**
	 * 添加任务对象在末尾,只能使用在多任务线程
	 * 
	 * @param task
	 * @return
	 */
	protected boolean addTask(HawkTask task) {
		if (running && !waitBreak) {
			queueLock.lock();
			try {
				taskQueue.add(task);
				pushTaskCnt++;
				return true;
			} finally {
				queueLock.unlock();
			}
		}
		return false;
	}

	/**
	 * 添加任务对象在头,只能使用在多任务线程
	 * 
	 * @param task
	 * @return
	 */
	protected boolean insertTask(HawkTask task) {
		if (running && !waitBreak) {
			queueLock.lock();
			try {
				taskQueue.push(task);
				pushTaskCnt++;
				return true;
			} finally {
				queueLock.unlock();
			}
		}
		return false;
	}

	/**
	 * 获取Push进去的任务数
	 * 
	 * @return
	 */
	public long getPushTaskCnt() {
		return pushTaskCnt;
	}

	/**
	 * 获取Pop出来的任务数
	 * 
	 * @return
	 */
	public long getPopTaskCnt() {
		return popTaskCnt;
	}

	/**
	 * 获取线程运行状态
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 线程是否等待执行完任务等待退出模式
	 * 
	 * @return
	 */
	public boolean isWaitBreak() {
		return waitBreak;
	}

	/**
	 * 是否等待关闭
	 * 
	 * @return
	 */
	public boolean isClosing() {
		return state == ThreadState.STATE_CLOSING;
	}

	/**
	 * 线程主执行函数
	 */
	@Override
	public void run() {
		state = ThreadState.STATE_RUNNING;
		while (running) {
			try {
				HawkTask task = null;
				if (!taskQueue.isEmpty()) {
					queueLock.lock();
					try {
						if (!taskQueue.isEmpty()) {
							task = taskQueue.pop();
							popTaskCnt++;
						}
					} finally {
						queueLock.unlock();
					}
				}
	
				if (task != null) {
					task.run();
					task.clear();
				} else if (waitBreak) {
					break;
				} else {
					HawkOSOperator.sleep();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
