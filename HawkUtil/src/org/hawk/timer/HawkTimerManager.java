package org.hawk.timer;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hawk.os.HawkException;

/**
 * 定时器管理器
 * 
 * @author xulinqs
 */
public class HawkTimerManager {
	/**
	 * 后台静默线程
	 */
	HawkAlarmWaiter waiter;
	/**
	 * 时间处理单位
	 */
	SortedSet<HawkTimerEntry> timerQueue;
	/**
	 * 队列管理的锁
	 */
	Lock lock;
	/**
	 * 实例对象
	 */
	static HawkTimerManager instance;

	/**
	 * 获取时钟管理器
	 * 
	 * @return
	 */
	public static HawkTimerManager getInstance() {
		if (instance == null) {
			instance = new HawkTimerManager();
		}
		return instance;
	}

	/**
	 * 创建时间管理器，AlarmWaiter负责后台时间扫描线程
	 * 
	 * @param isDaemon
	 *            后台扫描线程是否设置成daemon
	 * @param threadName
	 */
	public boolean init(boolean isDaemon) {
		lock = new ReentrantLock();
		timerQueue = new TreeSet<HawkTimerEntry>();
		waiter = new HawkAlarmWaiter(this, isDaemon, "AlarmManager");
		return true;
	}

	/**
	 * 特定时间点出发闹钟，如果日期已经是过去时间或者与当前时间比小于1s会抛出异常
	 * 
	 * @param name
	 * @param date
	 * @param listener
	 * @return
	 * @throws HawkException
	 */
	public HawkTimerEntry addAlarm(String name, Date date, HawkTimerListener listener) throws HawkException {
		HawkTimerEntry entry = new HawkTimerEntry(name, date, listener);
		addAlarm(entry);
		return entry;
	}

	/**
	 * 设置延时处理闹钟，当前时间之后delay秒执行
	 * 
	 * @param name
	 * @param delaySecond
	 * @param isRepeating
	 * @param listener
	 * @return
	 * @exception HawkException
	 */
	public HawkTimerEntry addAlarm(String name, int delaySecond, boolean isRepeating, HawkTimerListener listener) throws HawkException {
		HawkTimerEntry entry = new HawkTimerEntry(name, delaySecond, isRepeating, listener);
		addAlarm(entry);
		return entry;
	}

	/**
	 * 添加闹钟
	 * 
	 * @param second
	 *            Allowed values 0-59, or -1 for all.
	 * @param minute
	 *            Allowed values 0-59, or -1 for all.
	 * @param hour
	 *            hour of the alarm. Allowed values 0-23, or -1 for all.
	 * @param dayOfMonth
	 *            day of month of the alarm. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...), or -1 for all.
	 * @param dayOfWeek
	 *            day of week of the alarm. Allowed values 1-31, or -1 for all.
	 * @param listener
	 *            the alarm listener.
	 * @return the AlarmEntry.
	 * @exception HawkException
	 */
	public HawkTimerEntry addAlarm(String name, int second, int minute, int hour, int dayOfMonth, int dayOfWeek, HawkTimerListener listener) throws HawkException {
		HawkTimerEntry entry = new HawkTimerEntry(name, second, minute, hour, dayOfMonth, dayOfWeek, listener);
		addAlarm(entry);
		return entry;
	}

	/**
	 * 添加闹钟
	 * 
	 * @param second
	 *            Allowed values 0-59, or -1 for all.
	 * @param minutes
	 *            minutes of the alarm. Allowed values 0-59, or -1 for all.
	 * @param hours
	 *            hours of the alarm. Allowed values 0-23, or -1 for all.
	 * @param daysOfMonth
	 *            days of month of the alarm. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...), or -1 for all.
	 * @param daysOfWeek
	 *            days of week of the alarm. Allowed values 1-31, or -1 for all.
	 * @param listener
	 *            the alarm listener.
	 * 
	 * @return the AlarmEntry.
	 * @exception HawkException
	 *                if the alarm date is in the past (or less than 1 second away from the current date).
	 */
	public HawkTimerEntry addAlarm(String name, int[] seconds, int[] minutes, int[] hours, int[] daysOfMonth, int[] daysOfWeek, HawkTimerListener listener) throws HawkException {
		HawkTimerEntry entry = new HawkTimerEntry(name, seconds, minutes, hours, daysOfMonth, daysOfWeek, listener);
		addAlarm(entry);
		return entry;
	}

	/**
	 * 注册闹钟
	 * 
	 * @param minute
	 * @param hour
	 * @param dayOfMonth
	 * @param month
	 * @param dayOfWeek
	 * @param year
	 * @param listener
	 * @return
	 */
	public HawkTimerEntry register(int second, int minute, int hour, int dayOfMonth, int dayOfWeek, HawkTimerListener listener) {
		HawkTimerEntry entry = null;
		try {
			entry = new HawkTimerEntry(null, second, minute, hour, dayOfMonth, dayOfWeek, listener);
			addAlarm(entry);
		} catch (HawkException e) {
			HawkException.catchException(e);
		}
		return entry;
	}

	/**
	 * 注册闹钟
	 * 
	 * @param seconds
	 * @param minutes
	 * @param hours
	 * @param daysOfMonth
	 * @param months
	 * @param daysOfWeek
	 * @param year
	 * @param listener
	 * @return
	 */
	public HawkTimerEntry register(int[] seconds, int[] minutes, int[] hours, int[] daysOfMonth, int[] daysOfWeek, HawkTimerListener listener) {
		HawkTimerEntry entry = null;
		try {
			entry = new HawkTimerEntry(null, seconds, minutes, hours, daysOfMonth, daysOfWeek, listener);
			addAlarm(entry);
		} catch (HawkException e) {
			HawkException.catchException(e);
		}
		return entry;
	}

	/**
	 * 添加闹钟 entry
	 * 
	 * @param entry
	 * @exception HawkException
	 */
	public void addAlarm(HawkTimerEntry entry) throws HawkException {
		try {
			lock.lock();
			timerQueue.add(entry);
			if (timerQueue.first().equals(entry)) {
				waiter.update(entry.alarmTime);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 移除指定闹钟
	 * 
	 * @param entry
	 * @return
	 */
	public boolean removeAlarm(HawkTimerEntry entry) {
		boolean found = false;
		try {
			lock.lock();
			if (!timerQueue.isEmpty()) {
				HawkTimerEntry wasfirst = (HawkTimerEntry) timerQueue.first();
				found = timerQueue.remove(entry);
				if (!timerQueue.isEmpty() && entry.equals(wasfirst)) {
					waiter.update(((HawkTimerEntry) timerQueue.first()).alarmTime);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
		return found;
	}

	/**
	 * 移除所有闹钟
	 */
	public void removeAllAlarms() {
		try {
			lock.lock();
			timerQueue.clear();
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 停止并且移除所有闹钟
	 */
	public void stop() {
		if (waiter != null) {
			waiter.stop();
			waiter = null;
		}

		try {
			lock.lock();
			timerQueue.clear();
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 判断是否停止
	 * 
	 * @return
	 */
	public boolean isStopped() {
		return (waiter == null);
	}

	/**
	 * 是否存在闹钟
	 * 
	 * @param HawkTimerEntry
	 * @return boolean 是否存在
	 */
	public boolean containsAlarm(HawkTimerEntry alarmEntry) {
		try {
			lock.lock();
			return timerQueue.contains(alarmEntry);
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
		return false;
	}

	/**
	 * 获取当前所有闹钟
	 * 
	 * @return
	 */
	public List<HawkTimerEntry> getAllAlarms() {
		List<HawkTimerEntry> result = new ArrayList<HawkTimerEntry>();
		try {
			lock.lock();
			for (HawkTimerEntry entry : timerQueue) {
				result.add(entry);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}
		return result;
	}

	/**
	 * 闹铃触发 这个方法会在时间到的时候调用 或者被自己方法调用（下一个闹钟跟这个时间在1s之内）
	 */
	protected void ringNextAlarm() {
		if (timerQueue.isEmpty()) {
			return;
		}

		HawkTimerEntry entry = null;
		try {
			lock.lock();
			if (!timerQueue.isEmpty()) {
				entry = timerQueue.first();
				timerQueue.remove(entry);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.unlock();
		}

		if (entry == null) {
			return;
		}

		if (entry.isRingInNewThread()) {
			new Thread(new RunnableRinger(entry)).start();
		} else {
			try {
				entry.ringAlarm();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		if (entry.isRepeating) {
			entry.updateAlarmTime();
			timerQueue.add(entry);
		}

		// 唤醒AlarmWaiter继续下一个
		if (!timerQueue.isEmpty()) {
			long alarmTime = timerQueue.first().alarmTime;
			if (alarmTime - System.currentTimeMillis() < 1000) {
				ringNextAlarm();
			} else {
				waiter.restart(alarmTime);
			}
		}
	}

	/**
	 * 异步处理闹钟回调
	 * 
	 */
	private class RunnableRinger implements Runnable {
		HawkTimerEntry entry = null;

		RunnableRinger(HawkTimerEntry entry) {
			this.entry = entry;
		}

		public void run() {
			try {
				entry.ringAlarm();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
