package org.hawk.delay;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkException;

public class HawkDelayManager {
	/**
	 * 延时队列
	 */
	private List<HawkDelayAction> delayActions;
	
	/**
	 * 默认构造函数
	 */
	public HawkDelayManager() {
		delayActions = new LinkedList<HawkDelayAction>();
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return true;
	}
	
	/**
	 * 添加延时行为
	 * 
	 * @param action
	 */
	public void addDelayAction(long delayTime, HawkDelayAction action) {
		action.setDelayTime(delayTime);
		synchronized (delayActions) {
			delayActions.add(action);
		}
	}
	
	/**
	 * 更新
	 */
	public void updateAction() {
		try {
			synchronized (delayActions) {
				Iterator<HawkDelayAction> it = delayActions.iterator();
				while (it.hasNext()) {
					HawkDelayAction action = it.next();
					if (action.updateAction()) {
						it.remove();
					} 
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
