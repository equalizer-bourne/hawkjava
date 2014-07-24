package org.hawk.service;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

/**
 * 通用service接口
 * 
 * @author xulinqs
 * 
 */
public interface HawkService {
	/**
	 * 获得Service name
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 处理协议
	 * 
	 * @param obj
	 * @param protocol
	 */
	public boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol);

	/**
	 * 处理消息
	 * 
	 * @param obj
	 * @param protocol
	 */
	public boolean onMessage(HawkAppObj appObj, HawkMsg msg);
}
