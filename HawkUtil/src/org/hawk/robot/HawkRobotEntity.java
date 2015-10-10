package org.hawk.robot;

import java.util.LinkedList;
import java.util.List;

public class HawkRobotEntity {
	/**
	 * 行为列表
	 */
	protected List<HawkRobotAction> robotActions;
	
	/**
	 * 构造
	 */
	public HawkRobotEntity() {
		robotActions = new LinkedList<HawkRobotAction>();
	}
	
	/**
	 * 添加行为
	 * 
	 * @param robotAction
	 */
	public void attachAction(HawkRobotAction robotAction, long delayTime) {
		robotAction.setRobotEntity(this);
		robotAction.setDelayActive(delayTime);
		synchronized (robotActions) {
			robotActions.add(robotAction);
		}
	}
	
	/**
	 * 机器人对象更新
	 */
	public void update() {
		synchronized (robotActions) {
			for (HawkRobotAction robotAction : robotActions) {
				robotAction.update();
			}
		}
	}
}
