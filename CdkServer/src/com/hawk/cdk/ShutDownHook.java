package com.hawk.cdk;

import org.hawk.log.HawkLog;

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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				HawkLog.logPrintln("CdkServer Kill Shutdown.");
				CdkServices.getInstance().stop();
			}
		});
	}
}
