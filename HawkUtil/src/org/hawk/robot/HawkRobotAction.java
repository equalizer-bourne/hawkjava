package org.hawk.robot;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

public abstract class HawkRobotAction {
	/**
	 * 单次行为
	 */
	public static final int ACTION_ONCE = 1;
	/**
	 * 循环行为
	 */
	public static final int ACTION_FOREVER = 2;
	
	/**
	 * 行为类型
	 */
	protected int actionType = 0;
	/**
	 * 激活时间
	 */
	protected long activeTime = 0;
	/**
	 * 行为周期
	 */
	protected long actionPeriod = 0;
	/**
	 * 行为已触发次数
	 */
	protected int triggerTimes = 0;
	/**
	 * 上次触发时间
	 */
	protected long lastTriggerTime = 0;
	/**
	 * 绑定的机器人
	 */
	protected HawkRobotEntity robotEntity;

	/**
	 * 构造
	 * 
	 * @param robotEntity
	 */
	public HawkRobotAction() {
		this.activeTime = HawkTime.getMillisecond();
		this.lastTriggerTime = HawkTime.getMillisecond();
	}
	
	/**
	 * 设置机器人宿主
	 * 
	 * @param robotEntity
	 */
	public void setRobotEntity(HawkRobotEntity robotEntity) {
		this.robotEntity = robotEntity;
	}
	
	/**
	 * 获取对应机器人
	 * 
	 * @return
	 */
	public HawkRobotEntity getRobotEntity() {
		return robotEntity;
	}
	
	/**
	 * 设置行为类型
	 * 
	 * @param actionType
	 */
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}
	
	/**
	 * 设置行为周期
	 * 
	 * @param actionPeriod
	 */
	public void setActionPeriod(long actionPeriod) {
		this.actionPeriod = actionPeriod;
	}
	
	/**
	 * 设置延迟激活时间
	 * 
	 * @param activeTime
	 */
	public void setDelayActive(long delayTime) {
		this.activeTime = HawkTime.getMillisecond() + delayTime;
	}
	
	/**
	 * 更新行为
	 */
	public void update() {
		long curTime = HawkTime.getMillisecond();
		if (curTime >= activeTime && curTime - lastTriggerTime >= actionPeriod) {
			if (actionType == ACTION_FOREVER || (actionType == ACTION_ONCE && triggerTimes <= 0)) {
				try {
					doAction();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				triggerTimes ++;
				lastTriggerTime = curTime;
			}
		}
	}
	
	/**
	 * 行为函数
	 */
	protected abstract void doAction();
}
