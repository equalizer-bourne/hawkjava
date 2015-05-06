package org.hawk.service;

import java.util.Collection;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.service.HawkService;
import org.hawk.service.HawkServiceManager;

/**
 * 服务代理, 避免直接获取服务对象操作
 * 
 * @author hawk
 */
public class HawkServiceProxy {
	/**
	 * 协议处理
	 * 
	 * @param appObj
	 * @param protocol
	 * @return
	 */
	public static boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol) {
		Collection<HawkService> services = HawkServiceManager.getInstance().getServices();
		if (services != null) {
			for (HawkService service : services) {
				try {
					if (service.onProtocol(appObj, protocol)) {
						return true;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
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
	public static boolean onMessage(HawkAppObj appObj, HawkMsg msg) {
		Collection<HawkService> services = HawkServiceManager.getInstance().getServices();
		if (services != null) {
			for (HawkService service : services) {
				try {
					if (service.onMessage(appObj, msg)) {
						return true;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return false;
	}
}
