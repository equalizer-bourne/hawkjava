package org.hawk.timer;

import org.hawk.os.HawkException;

/**
 * 管理闹钟触发的线程，暴露的方法均是synchronized
 * 
 * @author xulinqs
 */
public class HawkAlarmWaiter implements Runnable {
	/**
	 * 时钟管理器
	 */
	protected HawkTimerManager timerManager;
	/**
	 * 执行线程
	 */
	protected Thread thread;
	/**
	 * sleep单位
	 */
	private long sleepUntil = -1;
	/**
	 * 关闭
	 */
	private boolean shutdown = false;

	/**
	 * 构造函数
	 * 
	 * @param isDaemon
	 *            闹钟计算线程是否是daemon线程
	 * @param waiterName
	 *            线程名称
	 */
	public HawkAlarmWaiter(HawkTimerManager timerManager, boolean isDaemon, String waiterName) {
		this.timerManager = timerManager;
		thread = new Thread(this, waiterName);
		thread.setDaemon(isDaemon);
		thread.start();
	}

	/**
	 * 更新睡眠时间
	 * 
	 * @param sleepUntil
	 */
	public synchronized void update(long sleepUntil) {
		this.sleepUntil = sleepUntil;
		notify();
	}

	/**
	 * 用指定睡眠时间重启
	 * 
	 * @param sleepUntil
	 */
	public synchronized void restart(long sleepUntil) {
		this.sleepUntil = sleepUntil;
		notify();
	}

	/**
	 * 关闭线程
	 */
	public synchronized void stop() {
		shutdown = true;
		notify();
	}

	/**
	 * 运行入口
	 */
	public synchronized void run() {
		while (!shutdown) {
			try {
				if (sleepUntil <= 0) {
					wait();
				} else {
					long timeout = sleepUntil - System.currentTimeMillis();
					if (timeout > 0) {
						wait(timeout);
					}
				}

				if (sleepUntil >= 0 && (sleepUntil - System.currentTimeMillis() < 1000)) {
					sleepUntil = -1;
					timerManager.ringNextAlarm();
				}
			} catch (InterruptedException e) {
				HawkException.catchException(e);
			}
		}
	}
}
