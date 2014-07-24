package com.hawk.game.module;

import org.hawk.app.HawkObjModule;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.GsPlayer;
import com.hawk.game.util.GsConst;

/**
 * 玩家登陆模块
 * 
 * @author hawk
 */
public class GsPlayerLoginModule extends HawkObjModule {
	/**
	 * 构造函数
	 * 
	 * @param player
	 */
	public GsPlayerLoginModule(GsPlayer player) {
		super(player);
	}

	/**
	 * 更新
	 * 
	 * @return
	 */
	@Override
	public boolean onTick() {
		return true;
	}

	/**
	 * 消息响应
	 * 
	 * @param msg
	 * @return
	 */
	@Override
	public boolean onMessage(HawkMsg msg) {
		if (msg.getMsg() == GsConst.MsgType.SESSION_CLOSED) {
			getAppObj().setSession(null);
			return true;
		}
		return false;
	}

	/**
	 * 协议响应
	 * 
	 * @param protocol
	 * @return
	 */
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		return false;
	}
}
