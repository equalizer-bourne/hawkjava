package org.hawk.timer;

/**
 * 时间事件处理接口，闹钟回调
 * 
 * @change xulinqs
 */
public interface HawkTimerListener {
	/**
	 * 触发TimerEntry的时候调用
	 * 
	 * @param entry
	 *            被触发的时间单元
	 */
	void handleAlarm(HawkTimerEntry entry);
}
