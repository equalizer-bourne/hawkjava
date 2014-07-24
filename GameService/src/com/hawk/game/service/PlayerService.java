package com.hawk.game.service;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.GsPlayer;
import com.hawk.game.service.GameService;

/**
 * 玩家服务
 * 
 * @author hawk
 */
public class PlayerService extends GameService {
	/**
	 * 协议处理
	 */
	@Override
	public boolean onProtocol(GsPlayer player, HawkProtocol protocol) {
		if (protocol != null) {
			player.sendProtocol(protocol);
			return true;
		}
		return false;
	}

	/**
	 * 消息处理
	 */
	@Override
	public boolean onMessage(HawkAppObj appObj, HawkMsg msg) {
		return false;
	}
}
