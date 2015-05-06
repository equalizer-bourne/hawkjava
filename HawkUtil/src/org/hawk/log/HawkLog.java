package org.hawk.log;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志类
 * 
 * @author hawk
 * 
 */
public class HawkLog {
	/**
	 * 控制台打印
	 */
	static boolean consolePrint = false;
	/**
	 * 配置日志对象
	 */
	static Logger logger = LoggerFactory.getLogger("Hawk");
	/**
	 * 调试日志对象
	 */
	static Logger debugLogger = LoggerFactory.getLogger("Debug");
	/**
	 * 配置日志对象
	 */
	static Logger exceptionLogger = LoggerFactory.getLogger("Exception");
	
	/**
	 * 开启控制台打印
	 * 
	 * @param enable
	 */
	public static void enableConsole(boolean enable) {
		consolePrint = enable;
	}

	/**
	 * 调试模式输出
	 * @param msg
	 */
	public synchronized static void debugPrintln(String msg) {
		if (HawkApp.getInstance().isDebug()) {
			debugLogger.info(msg);
			
			// 控制台输出
			if (consolePrint) {
				System.out.println(msg);
			}
		}
	}
	
	/**
	 * 日志打印
	 * 
	 * @param msg
	 */
	public synchronized static void logPrintln(String msg) {
		logger.info(msg);
		
		// 控制台输出
		if (consolePrint) {
			System.out.println(msg);
		}
	}

	/**
	 * 错误打印
	 * 
	 * @param msg
	 */
	public synchronized static void errPrintln(String msg) {
		logger.error(msg);
		
		// 打印错误
		System.err.println(msg);
	}

	/**
	 * 异常打印
	 * 
	 * @param excep
	 */
	public synchronized static void exceptionPrint(Exception e) {
		if (e != null) {
			// 打印堆栈
			e.printStackTrace();
					
			// 异常信息按照错误打印
			String stackMsg = HawkException.formatStackMsg(e);
			exceptionLogger.error(stackMsg);
		}
	}
}
