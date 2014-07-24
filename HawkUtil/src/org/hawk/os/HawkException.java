package org.hawk.os;

import org.hawk.log.HawkLog;
import org.slf4j.Logger;

/**
 * hawk系统异常封装
 * 
 * @author hawk
 * 
 */
public class HawkException extends Exception {
	/**
	 * 序列ID
	 */
	private static final long serialVersionUID = -521355582712781650L;

	/**
	 * 默认构造
	 */
	public HawkException() {
		super(HawkException.class.getName());
	}

	/**
	 * 构造函数
	 * 
	 * @param e
	 */
	public HawkException(Throwable e) {
		super(HawkException.class.getName(), e);
	}

	/**
	 * 构造函数
	 * 
	 * @param msg
	 * @param e
	 */
	public HawkException(String msg, Throwable e) {
		super(msg, e);
	}

	/**
	 * 构造函数
	 * 
	 * @param msg
	 */
	public HawkException(String msg) {
		super(HawkException.class.getName() + ":" + msg);
	}
	
	/**
	 * 异常捕获
	 * @param e
	 */
	public static void catchException(Exception e) {
		HawkLog.exceptionPrint(e);
	}
	
	/**
	 * 异常捕获
	 * @param e
	 */
	public static void catchException(Exception e, Logger logger) {
		// 打印堆栈
		e.printStackTrace();
				
		// 异常信息按照错误打印
		logger.error(HawkException.formatStackMsg(e));
	}
	
	/**
	 * 格式化异常堆栈结构
	 * @param e
	 * @return
	 */
	public static String formatStackMsg(Exception e) {		
		StackTraceElement[] stackArray = e.getStackTrace();
		
		StringBuffer sb = new StringBuffer();
		sb.append(e.toString() + "\n");
		
		for (int i = 0; i < stackArray.length; i++) {
			StackTraceElement element = stackArray[i];
			sb.append(element.toString() + "\n");
		}
		return sb.toString();
	}
}
