package org.hawk.security;

import org.hawk.net.HawkNetManager;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

/**
 * 会话安全组件
 * 
 * @author hawk
 */
public class HawkSecurity {
	/**
	 * 每秒协议数统计
	 */
	private int protocolCount = 0;
	/**
	 * 上次开始统计的时间(毫秒)
	 */
	private long statisticsTime = 0;

	/**
	 * 协议接收控制, 返回true表示正常, false表示异常(会关闭连接)
	 * 
	 * @param session
	 * @param protocol
	 * @return
	 */
	public boolean update(HawkSession session, HawkProtocol protocol) {
		// 1秒内的统计
		if (statisticsTime + 1000 > HawkTime.getMillisecond()) {
			protocolCount++;
			if (HawkNetManager.getInstance().getSessionPPS() > 0 &&
				protocolCount > HawkNetManager.getInstance().getSessionPPS()) {
				return false;
			}
		} else {
			protocolCount = 1;
			statisticsTime = HawkTime.getMillisecond();
		}
		return true;
	}
}
