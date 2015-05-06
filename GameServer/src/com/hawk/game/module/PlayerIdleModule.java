package com.hawk.game.module;

import org.hawk.app.HawkApp;
import org.hawk.msg.HawkMsg;

import com.hawk.game.ServerData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.util.GsConst;

/**
 * 空闲模块, 所有模块最后操作
 * 
 * @author hawk
 */
public class PlayerIdleModule extends PlayerModule {
	/**
	 * 构造
	 * 
	 * @param player
	 */
	public PlayerIdleModule(Player player) {
		super(player);
	}

	/**
	 * 玩家上线处理
	 * 
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 通知玩家组装完成
		HawkMsg msg = HawkMsg.valueOf(GsConst.MsgType.PLAYER_ASSEMBLE, player.getXid());
		HawkApp.getInstance().postMsg(msg);
		return true;
	}

	/**
	 * 组装完成
	 */
	@Override
	protected boolean onPlayerAssemble() {
		// 设置组装状态
		player.setAssembleFinish(true);
		// 添加在线信息
		ServerData.getInstance().addOnlinePlayerId(player.getId());
		return true;
	}

	/**
	 * 玩家下线处理
	 * 
	 * @return
	 */
	@Override
	protected boolean onPlayerLogout() {
		// 移除玩家在线id
		ServerData.getInstance().removeOnlinePlayerId(player.getId());
		// 情况玩家会话
		player.setSession(null);
		// 设置组装状态
		player.setAssembleFinish(false);
		return true;
	}
}
