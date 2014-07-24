package com.hawk.collector;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 数据收集服务器
 * 
 * @author hawk
 */
public class Collector {
	/**
	 * 当前用户路径
	 */
	static String userDir;

	/**
	 * 获取用户路径
	 * 
	 * @return
	 */
	public static String getUserDir() {
		return userDir;
	}

	/**
	 * 主函数
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 退出构造装载
			ShutDownHook.install();

			// 打印系统信息
			HawkOSOperator.printOsEnv();

			// 用户路径
			userDir = System.getProperty("user.dir");

			// 创建并初始化服务
			if (CollectorServices.getInstance().init(userDir + "/cfg/collector.cfg")) {
				// 启动服务器
				CollectorServices.getInstance().run();

			} else {
				HawkLog.errPrintln("Collector Init Failed.");
			}

			// 退出
			HawkLog.logPrintln("Collector Exit.");
			System.exit(0);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
