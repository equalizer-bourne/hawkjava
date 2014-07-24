package org.hawk.util;

/**
 * 可更新列表
 * 
 * @author hawk
 */
public abstract class HawkTickable {
	/**
	 * 是否可tick
	 */
	boolean tickable = true;

	/**
	 * 更新
	 * 
	 */
	public abstract void onTick();

	/**
	 * 是否有效
	 * 
	 * @return
	 */
	public boolean isTickable() {
		return tickable;
	}

	/**
	 * 设置可更新
	 * 
	 * @param tickable
	 */
	public void setTickable(boolean tickable) {
		this.tickable = tickable;
	}
}
