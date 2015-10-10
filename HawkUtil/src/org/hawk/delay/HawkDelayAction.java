package org.hawk.delay;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

/**
 * 延时行为对象
 * 
 * @author hawk
 */
public abstract class HawkDelayAction {
	/**
	 * 行为时间
	 */
	protected long actionTime = 0;
	/**
	 * 是否完成
	 */
	protected boolean complete = false;
	
	/**
	 * 构造函数
	 * 
	 * @param delayTime
	 */
	public HawkDelayAction() {
		this.complete = false;
	}
	
	/**
	 * 设置延时时间
	 * 
	 * @param delayTime
	 */
	public void setDelayTime(long delayTime) {
		actionTime = HawkTime.getMillisecond() + delayTime;
	}
	
	/**
	 * 是否完成
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return complete;
	}
	
	/**
	 * 更新执行
	 * 
	 * @return
	 */
	public boolean updateAction() {
		if (!complete && HawkTime.getMillisecond() >= actionTime) {
			try {
				if (!complete) {
					complete = true;
					doAction();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 执行
	 */
	protected abstract void doAction();
}
