package org.hawk.os;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScriptManager;
import org.hawk.util.HawkCallback;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 进程关闭回调钩子
 * 
 * @author hawk
 */
public class HawkShutdownHook implements Runnable {
	/**
	 * 是否已shutdown
	 */
	volatile boolean isShutdown = false;
	/**
	 * 回调
	 */
	HawkCallback callback;
	/**
	 * hook实例
	 */
	static HawkShutdownHook instance;

	/**
	 * 是否已shutdown
	 * 
	 * @return
	 */
	public boolean isShutdown() {
		return isShutdown;
	}

	/**
	 * 处理shutdown事项, 主要用来数据落地存储
	 * 
	 * @param notify
	 * @return
	 */
	public boolean processShutdown(boolean notify) {
		if (isShutdown) {
			return false;
		}

		HawkLog.logPrintln("hawk start shutting down");

		// 回调唤起
		try {
			if (callback != null) {
				callback.invoke(notify);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 通知app关闭
		HawkApp.getInstance().onShutdown();

		HawkLog.logPrintln("hawk shutting down complete");

		isShutdown = true;
		return true;
	}

	/**
	 * 设置回调
	 * 
	 * @param callback
	 */
	public void setCallback(HawkCallback callback) {
		this.callback = callback;
	}

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static HawkShutdownHook getInstance() {
		if (instance == null) {
			instance = new HawkShutdownHook();
		}
		return instance;
	}

	/**
	 * 装载
	 */
	public void install() {
		// kill命令回调函数注册
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				processShutdown(true);
			}
		});

		// kill -12信号处理注册回调
		try {
			Signal.handle(new Signal("USR2"), new kill_SignalHandler("USR2"));
		} catch (Exception e) {
		}

		// kill -17信号处理注册回调
		try {
			Signal.handle(new Signal("CHLD"), new script_SignalHandler("CHLD"));
		} catch (Exception e) {
		}
		
		if (HawkApp.getInstance().isDebug() && HawkOSOperator.isWindowsOS()) {
			Thread thread = new Thread(this);
			thread.setName("WinConsole");
			thread.start();
		}
	}

	@Override
	public void run() {
		try {
			System.in.read();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		processShutdown(true);
	}
	
	/**
	 * 
	 * @author hawk
	 */
	private class kill_SignalHandler implements SignalHandler {
		private String signalName;
		
		public kill_SignalHandler(String name) {
			this.signalName = name;
		}
		
		public void handle(Signal signal) {
			HawkLog.logPrintln("signal handler: " + this.getClass().getSimpleName() + ", name: " + signalName);
			processShutdown(false);
			System.exit(0);
		}
	}
	
	/**
	 * 
	 * @author hawk
	 */
	private class script_SignalHandler implements SignalHandler {
		private String signalName;
		
		public script_SignalHandler(String name) {
			this.signalName = name;
		}
		
		public void handle(Signal signal) {
			HawkLog.logPrintln("signal handler: " + this.getClass().getSimpleName() + ", name: " + signalName);
			HawkScriptManager.getInstance().restart();
		}
	}
}
