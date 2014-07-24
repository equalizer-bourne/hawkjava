package org.hawk.os;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间通用处理
 * 
 * @author hawk
 * 
 */
public class HawkTime {
	private static long msOffset = 0;

	/**
	 * 获取时间偏移
	 * 
	 * @return
	 */
	public static long getMsOffset() {
		return msOffset;
	}

	/**
	 * 设置时间偏移
	 * 
	 * @param msOffset
	 */
	public static void setMsOffset(long msOffset) {
		HawkTime.msOffset = msOffset;
	}

	/**
	 * 获取系统时间
	 * 
	 * @return
	 */
	public static Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		if (getMsOffset() != 0) {
			calendar.setTimeInMillis(calendar.getTimeInMillis() + getMsOffset());
		}
		return calendar;
	}

	/**
	 * 获取系统距1970年1月1日总毫秒
	 * 
	 * @return
	 */
	public static long getMillisecond() {
		return getCalendar().getTimeInMillis() + getMsOffset();
	}

	/**
	 * 获取系统距1970年1月1日总秒
	 * 
	 * @return
	 */
	public static long getSeconds() {
		return (getCalendar().getTimeInMillis() + getMsOffset()) / 1000;
	}

	/**
	 * 获取系统当前时间
	 * 
	 * @return
	 */
	public static Timestamp getTimestamp() {
		Timestamp ts = new Timestamp(getMillisecond());
		return ts;
	}

	/**
	 * 获取当日0点时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getAM0Date() {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取指定日期的0点时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getAM0Date(Date date) {
		Calendar calendar = getCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 是否是相同的日子（月 和 天 相同）
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static boolean isSameDay(long time1, long time2) {
		Calendar dt1 = getCalendar();
		Calendar dt2 = getCalendar();
		dt1.setTime(new Date(time1));
		dt2.setTime(new Date(time2));
		if (dt1.get(Calendar.MONTH) == dt2.get(Calendar.MONTH) && dt1.get(Calendar.DAY_OF_MONTH) == dt2.get(Calendar.DAY_OF_MONTH)) {
			return true;
		}
		return false;
	}

	/**
	 * 格式化日期
	 * 
	 * @return
	 */
	public static String getTimeString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(getCalendar().getTime());
	}
	
	/**
	 * 格式化日期
	 * 
	 * @return
	 */
	public static String getDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(getCalendar().getTime());
	}
}
