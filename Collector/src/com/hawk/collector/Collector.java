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
	 * 系统token校验
	 */
	private static String httpToken = "";
	private static boolean userLog = false;
	
	public static void setToken(String token) {
		httpToken = token.trim();
	}
	
	public static void setUserLogEnable(boolean enable) {
		userLog = enable;
	}
	
	public static boolean checkToken(String token) {
		if (httpToken != null && httpToken.length() > 0) {
			if (token == null || !token.equals(httpToken)) {
				throw new RuntimeException("http token check failed.");
			}
		}
		return true;
	}
	
	public static boolean isUserLogEnable() {
		return userLog;
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
			
			// 创建并初始化服务
			if (CollectorServices.getInstance().init(System.getProperty("user.dir") + "/cfg/config.xml")) {
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
