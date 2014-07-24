package org.hawk.util;

/**
 * 计数 | 计时功能
 * 
 * @author hawk
 * 
 */
public class HawkCounter {
	/**
	 * 是否暂停
	 */
	private boolean pause;
	/**
	 * 计数个数
	 */
	private long counter;
	/**
	 * 最大计数
	 */
	private long period;

	/**
	 * 默认构造函数
	 */
	public HawkCounter() {
		pause = false;
		counter = 0;
		period = 0;
	}

	/**
	 * 默认构造函数
	 */
	public HawkCounter(long period) {
		this();
		this.period = period;
	}

	/**
	 * 计数是否达到满值
	 * 
	 * @return
	 */
	public boolean isFull() {
		return counter >= period;
	}

	/**
	 * 重置
	 */
	public void reset() {
		counter = 0;
	}

	/**
	 * 重置
	 */
	public void resetFull() {
		counter = period;
	}

	/**
	 * 暂停
	 * 
	 * @param pause
	 */
	public void setPause(boolean pause) {
		this.pause = pause;
	}

	/**
	 * 增加计数
	 * 
	 * @param counter
	 * @return
	 */
	public boolean incCounter(long counter) {
		if (!pause) {
			this.counter += counter;
		}
		return (this.counter >= period) ? true : false;
	}

	/**
	 * 减少计数
	 * 
	 * @param counter
	 */
	public void decCounter(long counter) {
		if (pause) {
			return;
		}

		if (this.counter <= counter) {
			this.counter = 0;
		} else {
			this.counter -= counter;
		}
	}
}
