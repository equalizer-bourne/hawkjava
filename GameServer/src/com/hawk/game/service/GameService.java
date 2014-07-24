package com.hawk.game.service;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.service.HawkService;

import com.hawk.game.player.GsPlayer;

/**
 * 游戏服务基础类
 * 
 * @author hawk
 */
public abstract class GameService implements HawkService {
	/**
	 * 获取服务名字
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 协议处理
	 */
	@Override
	public boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol) {
		return onProtocol((GsPlayer) appObj, protocol);
	}

	/**
	 * 消息处理
	 */
	@Override
	public abstract boolean onMessage(HawkAppObj appObj, HawkMsg msg);

	/**
	 * 协议处理
	 * 
	 * @param appObj
	 * @param protocol
	 * @return
	 */
	public abstract boolean onProtocol(GsPlayer appObj, HawkProtocol protocol);
}
