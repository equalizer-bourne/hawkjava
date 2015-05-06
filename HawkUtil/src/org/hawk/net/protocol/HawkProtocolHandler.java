package org.hawk.net.protocol;

import org.hawk.app.HawkAppObj;

/**
 * 协议处理句柄
 * 
 * @author hawk
 */
public interface HawkProtocolHandler {
	/**
	 * 协议处理
	 * 
	 * @param appObj
	 * @param protocol
	 * @return
	 */
	public boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol);
}
