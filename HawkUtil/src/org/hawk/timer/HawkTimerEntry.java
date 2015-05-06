package org.hawk.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Arrays;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

/**
 * 时间管理实体对象
 * 
 * @change xulinqs
 */
public class HawkTimerEntry implements Comparable<HawkTimerEntry> {
	/**
	 * 时间配置, 秒
	 */
	private int[] seconds = { -1 };
	private static int minSecond = 0;
	private static int maxSecond = 59;
	/**
	 * 时间配置, 分钟
	 */
	private int[] minutes = { -1 };
	private static int minMinute = 0;
	private static int maxMinute = 59;
	/**
	 * 时间配置, 小时
	 */
	private int[] hours = { -1 };
	private static int minHour = 0;
	private static int maxHour = 23;
	/**
	 * 时间配置, 月天
	 */
	private int[] daysOfMonth = { -1 };
	private static int minDayOfMonth = 1;
	/**
	 * 时间配置, 星期
	 */
	private int[] daysOfWeek = { -1 };
	private static int minDayOfWeek = 1;
	private static int maxDayOfWeek = 7;
	/**
	 * 名字
	 */
	private String name;
	/**
	 * 唯一序号
	 */
	static int UNIQUE = 0;
	/**
	 * 新线程闹铃
	 */
	boolean ringInNewThread = false;
	/**
	 * 相对当前时间
	 */
	private boolean isRelative;
	/**
	 * 是否重复
	 */
	public boolean isRepeating;
	/**
	 * 闹铃出发时间
	 */
	public long alarmTime;
	/**
	 * 上一次更新时间
	 */
	private long lastUpdateTime;
	/**
	 * 事件监听对象
	 */
	private transient HawkTimerListener listener;

	/**
	 * 创建一个新的闹钟检测单元，固定的时间，闹钟触发一次
	 * 
	 * @param name
	 *            名字
	 * @param date
	 *            固定时间
	 * @param listener
	 *            回调对象
	 * @exception HawkException
	 */
	public HawkTimerEntry(String name, Date date, HawkTimerListener listener) throws HawkException {
		setName(name);
		this.listener = listener;
		Calendar alarm = HawkTime.getCalendar();
		alarm.setTime(date);

		seconds = new int[] { alarm.get(Calendar.SECOND) };
		minutes = new int[] { alarm.get(Calendar.MINUTE) };
		hours = new int[] { alarm.get(Calendar.HOUR_OF_DAY) };
		daysOfMonth = new int[] { alarm.get(Calendar.DAY_OF_MONTH) };

		isRepeating = false;
		isRelative = false;
		alarmTime = date.getTime();
	}

	/**
	 * 根据延迟时间创建时间单元,可重复
	 * 
	 * @param name
	 *            名字
	 * @param delaySecond
	 *            延迟秒数
	 * @param isRepetitive
	 *            是否重复
	 * @param listener
	 *            时间事件处理回调
	 * @exception HawkException
	 */
	public HawkTimerEntry(String name, int delaySecond, boolean isRepeating, HawkTimerListener listener) throws HawkException {
		if (delaySecond <= 0) {
			throw new HawkException("dealy second less than 1.");
		}
		setName(name);
		seconds = new int[] { delaySecond };
		this.listener = listener;
		this.isRepeating = isRepeating;
		isRelative = true;
	}

	/**
	 * 设置闹铃名字
	 * 
	 * @param name
	 */
	private void setName(String name) {
		this.name = name;
		if (this.name == null || this.name.length() <= 0)
			this.name = "Alarm" + (UNIQUE++);
	}

	/**
	 * 获取名字
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置在新线程开启闹铃模式
	 */
	public void setRingInNewThead() {
		ringInNewThread = true;
	}

	/**
	 * 是否在新线程开启闹铃模式
	 * 
	 * @return
	 */
	public boolean isRingInNewThread() {
		return ringInNewThread;
	}

	/**
	 * 出发闹铃
	 */
	public void ringAlarm() {
		listener.handleAlarm(this);
	}

	/**
	 * 定时器比较接口
	 */
	public int compareTo(HawkTimerEntry object) {
		HawkTimerEntry entry = (HawkTimerEntry) object;
		if (alarmTime < entry.alarmTime) {
			return -1;
		} else if (alarmTime > entry.alarmTime) {
			return 1;
		} else {
			if (lastUpdateTime < entry.lastUpdateTime)
				return -1;
			else if (lastUpdateTime > entry.lastUpdateTime)
				return 1;
			else
				return 0;
		}
	}

	/**
	 * 相等判断
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof HawkTimerEntry)) {
			return false;
		}
		HawkTimerEntry entry = (HawkTimerEntry) object;
		return (name.equals(entry.name) && alarmTime == entry.alarmTime && isRelative == entry.isRelative && isRepeating == entry.isRepeating && Arrays.equals(seconds, entry.seconds) && Arrays.equals(minutes, entry.minutes) && Arrays.equals(hours, entry.hours) && Arrays.equals(daysOfMonth, entry.daysOfMonth) && Arrays.equals(daysOfWeek, entry.daysOfWeek));
	}

	/**
	 * 转换为字符串
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Alarm(" + name + ") params");
		sb.append(" second=");
		sb.append(arrToString(seconds));
		sb.append(" minute=");
		sb.append(arrToString(minutes));
		sb.append(" hour=");
		sb.append(arrToString(hours));
		sb.append(" dayOfMonth=");
		sb.append(arrToString(daysOfMonth));
		sb.append(" dayOfWeek=");
		sb.append(arrToString(daysOfWeek));
		sb.append(" (next alarm date=" + new Date(alarmTime) + ")");
		return sb.toString();
	}

	/**
	 * 创建闹钟实体 crontab format
	 * 
	 * @param second
	 *            秒，取值范围 0-59.
	 * @param minute
	 *            分钟，取值范围 0-59.
	 * @param hour
	 *            小时，取值范围0-23
	 * @param dayOfMonth
	 *            一个月中的第几天，取值范围1-31
	 * @param dayOfWeek
	 *            day of week of the alarm (-1 if every day). Allowed values 1-7 (1 = Sunday, 2 = Monday, ...)
	 * @param listener
	 *            the alarm listener.
	 * @return the AlarmEntry.
	 * @exception HawkTimerException
	 *                if the alarm date is in the past (or less than 1 second away from the current date).
	 */
	public HawkTimerEntry(String name, int second, int minute, int hour, int dayOfMonth, int dayOfWeek, HawkTimerListener listener) throws HawkException {
		this(name, new int[] { second }, new int[] { minute }, new int[] { hour }, new int[] { dayOfMonth }, new int[] { dayOfWeek }, listener);
	}

	/**
	 * 创建闹钟实体 crontab format
	 * 
	 * @param seconds
	 *            valid minutes of the alarm. Allowed values 0-59, or {-1} for all.
	 * @param minutes
	 *            valid minutes of the alarm. Allowed values 0-59, or {-1} for all.
	 * @param hours
	 *            valid hours of the alarm. Allowed values 0-23, or {-1} for all.
	 * @param daysOfMonth
	 *            valid days of month of the alarm. Allowed values 1-31, or {-1} for all.
	 * @param daysOfWeek
	 *            valid days of week of the alarm. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...), or {-1} for all.
	 * @param listener
	 *            the alarm listener.
	 * @return the AlarmEntry.
	 * @exception HawkException
	 *                if the alarm date is in the past (or less than 1 second away from the current date).
	 */
	public HawkTimerEntry(String name, int[] seconds, int[] minutes, int[] hours, int[] daysOfMonth, int[] daysOfWeek, HawkTimerListener listener) throws HawkException {
		setName(name);
		this.seconds = seconds;
		this.minutes = minutes;
		this.hours = hours;
		this.daysOfMonth = daysOfMonth;
		this.daysOfWeek = daysOfWeek;
		this.listener = listener;
		isRepeating = true;
		isRelative = false;
		updateAlarmTime();
	}

	/**
	 * 更新闹铃时间
	 */
	public void updateAlarmTime() {
		Calendar now = HawkTime.getCalendar();
		if (isRelative) {
			alarmTime = now.getTime().getTime() + (seconds[0] * 1000);
			return;
		}

		int offset = 0, current = 0;
		Calendar alarm = (Calendar) now.clone();

		// 秒
		current = alarm.get(Calendar.SECOND);
		offset = getOffsetToNext(current, minSecond, maxSecond, seconds);
		alarm.add(Calendar.SECOND, offset);

		// 分
		current = alarm.get(Calendar.MINUTE);
		offset = getOffsetToNext(current, minMinute, maxMinute, minutes);
		alarm.add(Calendar.MINUTE, offset);

		// 时
		current = alarm.get(Calendar.HOUR_OF_DAY);
		offset = getOffsetToNextOrEqual(current, minHour, maxHour, hours);
		alarm.add(Calendar.HOUR_OF_DAY, offset);

		if (daysOfMonth[0] != -1 && daysOfWeek[0] != -1) {
			Calendar dayOfWeekAlarm = (Calendar) alarm.clone();
			updateDayOfWeek(dayOfWeekAlarm);

			Calendar dayOfMonthAlarm = (Calendar) alarm.clone();
			updateDayOfMonth(dayOfMonthAlarm);

			if (dayOfMonthAlarm.getTime().getTime() < dayOfWeekAlarm.getTime().getTime()) {
				alarm = dayOfMonthAlarm;
			} else {
				alarm = dayOfWeekAlarm;
			}
		} else if (daysOfWeek[0] != -1) {
			updateDayOfWeek(alarm);
		} else if (daysOfMonth[0] != -1) {
			updateDayOfMonth(alarm);
		}
		alarmTime = alarm.getTime().getTime();
		lastUpdateTime = System.currentTimeMillis();
	}

	private void updateDayOfMonth(Calendar alarm) {
		int currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
		while (!isIn(currentDayOfMonth, daysOfMonth)) {
			int maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
			int offset = getOffsetToNextOrEqual(currentDayOfMonth, minDayOfMonth, maxDayOfMonth, daysOfMonth);
			alarm.add(Calendar.DAY_OF_MONTH, offset);
			currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
		}
	}

	private void updateDayOfWeek(Calendar alarm) {
		int currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK);
		while (!isIn(currentDayOfWeek, daysOfWeek)) {
			int offset = getOffsetToNextOrEqual(currentDayOfWeek, minDayOfWeek, maxDayOfWeek, daysOfWeek);
			alarm.add(Calendar.DAY_OF_YEAR, offset);
			currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK);
		}
	}

	static int getOffsetToNext(int current, int min, int max, int[] values) {
		int offset = 0;
		if (values[0] == -1) {
			offset = 1;
		} else {
			if (current >= last(values)) {
				int next = values[0];
				offset = (max - current + 1) + (next - min);
			} else {
				findvalue: for (int i = 0; i < values.length; i++) {
					if (current < values[i]) {
						offset = values[i] - current;
						break findvalue;
					}
				}
			}
		}
		return offset;
	}

	static int getOffsetToNextOrEqual(int current, int min, int max, int[] values) {
		int offset = 0;
		int[] safeValues = null;
		if (values[0] == -1 || isIn(current, values)) {
			offset = 0;
		} else {
			safeValues = discardValuesOverMax(values, max);
			if (current > last(safeValues)) {
				int next = safeValues[0];
				offset = (max - current + 1) + (next - min);
			} else {
				findvalue: for (int i = 0; i < values.length; i++) {
					if (current < safeValues[i]) {
						offset = safeValues[i] - current;
						break findvalue;
					}
				}
			}
		}
		return offset;
	}

	static int last(int[] intArray) {
		return intArray[intArray.length - 1];
	}

	static boolean isIn(int find, int[] values) {
		if (values[0] == -1) {
			return true;
		} else {
			for (int i = 0; i < values.length; i++) {
				if (find == values[i])
					return true;
			}
			return false;
		}
	}

	static int[] discardValuesOverMax(int[] values, int max) {
		int[] safeValues = null;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				safeValues = new int[i];
				System.arraycopy(values, 0, safeValues, 0, i);
				return safeValues;
			}
		}
		return values;
	}

	static String arrToString(int[] intArray) {
		if (intArray == null) {
			return "null";
		}

		if (intArray.length == 0) {
			return "{}";
		}

		String string = "{";
		for (int i = 0; i < intArray.length - 1; i++) {
			string += intArray[i] + ", ";
		}
		string += intArray[intArray.length - 1] + "}";
		return string;
	}
}
