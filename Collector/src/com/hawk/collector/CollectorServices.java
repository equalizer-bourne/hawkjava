package com.hawk.collector;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.XMLConfiguration;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.util.HawkTickable;

import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;

/**
 * 收集服务
 * 
 * @author hawk
 */
public class CollectorServices {
	/**
	 * 服务器是否允许中
	 */
	volatile boolean running;
	/**
	 * 可更新列表
	 */
	Set<HawkTickable> tickableSet;
	/**
	 * 单例对象
	 */
	static CollectorServices instance;

	/**
	 * 获取实例
	 */
	public static CollectorServices getInstance() {
		if (instance == null) {
			instance = new CollectorServices();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private CollectorServices() {
		// 初始化变量
		running = true;
		tickableSet = new HashSet<HawkTickable>();
	}

	/**
	 * 是否运行中
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 退出服务主循环
	 */
	public void breakLoop() {
		running = false;
	}

	/**
	 * 定时更新
	 */
	private void onTick() {
		for (HawkTickable tick : tickableSet) {
			tick.onTick();
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
	 * 移除可更新对象
	 * 
	 * @param tickable
	 */
	public void removeTickable(HawkTickable tickable) {
		tickableSet.remove(tickable);
	}

	/**
	 * 使用配置文件初始化
	 * 
	 * @param cfgFile
	 * @return
	 */
	public boolean init(String cfgFile) {
		boolean initOK = true;
		try {
			// 加载配置文件
			XMLConfiguration conf = new XMLConfiguration(System.getProperty("user.dir") + "/cfg/config.xml");

			// 初始化数据库
			initOK &= DBManager.getInstance().init(conf.getString("hibernate.connUrl"), conf.getString("hibernate.userName"), conf.getString("hibernate.passWord"));

			// 初始化http服务器
			initOK &= CollectorHttpServer.getInstance().setup(conf.getString("httpserver.addr"), conf.getInt("httpserver.port"), conf.getInt("httpserver.pool"));
		} catch (Exception e) {
			HawkException.catchException(e);
			initOK = false;
		}
		return initOK;
	}

	/**
	 * 运行服务器
	 */
	public void run() {
		HawkLog.logPrintln("MainLoop Running OK.");
		while (running) {
			try {
				onTick();

				HawkOSOperator.sleep();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
