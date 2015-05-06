package com.hawk.game.service;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.service.GameService;

/**
 * 玩家服务
 * 
 * @author hawk
 */
public class PlayerService extends GameService {
	/**
	 * 消息拦截
	 */
	@Override
	public boolean onMessage(HawkAppObj appObj, HawkMsg msg) {
		return false;
	}

	/**
	 * 协议拦截
	 */
	@Override
	public boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol) {
		return false;
	}
}
