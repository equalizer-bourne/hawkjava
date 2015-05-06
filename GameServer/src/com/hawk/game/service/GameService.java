package com.hawk.game.service;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.service.HawkService;

/**
 * 游戏服务基础类
 * 
 * @author hawk
 */
public abstract class GameService implements HawkService {
	/**
	 * 获取服务名字
	 * 
	 * @return 返回名字
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 协议处理
	 * 
	 * @return 返回true即表示协议拦截
	 */
	@Override
	public abstract boolean onProtocol(HawkAppObj appObj, HawkProtocol protocol);

	/**
	 * 消息处理
	 * 
	 * @return 返回true即表示消息拦截
	 */
	@Override
	public abstract boolean onMessage(HawkAppObj appObj, HawkMsg msg);
}

