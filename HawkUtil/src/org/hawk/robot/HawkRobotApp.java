package org.hawk.robot;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.XMLConfiguration;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.util.HawkTickable;

public class HawkRobotApp {
	/**
	 * 配置文件
	 */
	protected XMLConfiguration config;
	/**
	 * 更新对象列表
	 */
	protected Set<HawkTickable> tickableSet;
	/**
	 * 延时队列
	 */
	private BlockingQueue<HawkRobotEntity> robotEntities;
	
	/**
	 * 单例对象
	 */
	static HawkRobotApp instance;

	/**
	 * 获取实例
	 */
	public static HawkRobotApp getInstance() {
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	public HawkRobotApp() {
		if (instance != null) {
			throw new RuntimeException("robotapp instance exist");
		}
		instance = this;
		
		tickableSet = new HashSet<HawkTickable>();
		robotEntities = new LinkedBlockingQueue<HawkRobotEntity>();
	}

	/**
	 * 定时更新
	 */
	private void onTick() {
		for (HawkTickable tick : tickableSet) {
			try {
				tick.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 添加可更新对象列表
	 * 
	 * @param tickable
	 */
	public void addTickable(HawkTickable tickable) {
		tickableSet.add(tickable);
	}

	/**
	 * 获取配置文件
	 * 
	 * @return
	 */
	public XMLConfiguration getConfig() {
		return config;
	}
	
	/**
	 * 使用配置文件初始化
	 * 
	 * @param cfgFile
	 * @return
	 */
	public boolean init(String cfgFile) {
		// 添加库加载目录
		HawkOSOperator.installLibPath();
		try {
			// 加载配置文件
			config = new XMLConfiguration(cfgFile);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 添加机器人
	 * 
	 * @param robotEntity
	 */
	public void addRobot(HawkRobotEntity robotEntity) {
		robotEntities.add(robotEntity);
	}
	
	/**
	 * 运行服务器
	 */
	public void run() {
		HawkLog.logPrintln("MainLoop Running OK.");
		while (true) {
			try {
				onTick();
				
				for (HawkRobotEntity robotEntity : robotEntities) {
					robotEntity.update();
				}
				
				HawkOSOperator.sleep();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
