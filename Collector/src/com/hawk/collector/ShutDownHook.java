package com.hawk.collector;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 关闭退出钩子
 * 
 * @author hawk
 */
public class ShutDownHook {
	/**
	 * 安装钩子
	 */
	public static void install() {
		// windows开启线程关闭程序
		if (HawkOSOperator.isWindowsOS()) {
			Thread thread = new Thread(new Thread() {
				public void run() {
					try {
						System.in.read();
						HawkLog.logPrintln("Collector Kill Shutdown.");
						CollectorServices.getInstance().breakLoop();
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			});
			thread.setName("WinConsole");
			thread.start();
		}
		
		// 添加关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				HawkLog.logPrintln("Collector Kill Shutdown.");
				CollectorServices.getInstance().breakLoop();
			}
		});
	}
}
