package com.hawk.game.service;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.service.HawkService;
import org.hawk.service.HawkServiceManager;

/**
 * 服务代理, 避免直接获取服务对象操作
 * 
 * @author hawk
 */
public class ServiceProxy {
	/**
	 * 协议处理
	 * 
	 * @param serviceName
	 * @param appObj
	 * @param protocol
	 * @return
	 */
	public static boolean onProtocol(String serviceName, HawkAppObj appObj, HawkProtocol protocol) {
		HawkService service = HawkServiceManager.getInstance().getService(serviceName);
		if (service != null) {
			return service.onProtocol(appObj, protocol);
		} else {
			HawkLog.debugPrintln("game service not exist: " + serviceName);
		}
		return false;
	}

	/**
	 * 消息处理
	 * 
	 * @param serviceName
	 * @param appObj
	 * @param msg
	 * @return
	 */
	public static boolean onMessage(String serviceName, HawkAppObj appObj, HawkMsg msg) {
		HawkService service = HawkServiceManager.getInstance().getService(serviceName);
		if (service != null) {
			return service.onMessage(appObj, msg);
		} else {
			HawkLog.debugPrintln("game service not exist: " + serviceName);
		}
		return false;
	}
}
