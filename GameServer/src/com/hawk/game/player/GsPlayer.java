package com.hawk.game.player;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

import com.hawk.game.module.GsPlayerLoginModule;
import com.hawk.game.service.ServiceProxy;
import com.hawk.game.util.GsConst;

/**
 * 玩家对象
 * 
 * @author hawk
 * 
 */
public class GsPlayer extends HawkAppObj {
	/**
	 * 挂载玩家数据管理集合
	 */
	private PlayerData playerData;

	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	public GsPlayer(HawkXID xid) {
		super(xid);

		initModules();

		playerData = new PlayerData();
	}

	/**
	 * 初始化模块
	 * 
	 */
	public void initModules() {
		registerModule(GsConst.ModuleType.LOGIN_MODULE, new GsPlayerLoginModule(this));
	}

	/**
	 * 获取玩家数据
	 * 
	 * @return
	 */
	public PlayerData getPlayerData() {
		return playerData;
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/**
	 * 消息响应
	 * 
	 * @param msg
	 * @return
	 */
	@Override
	public boolean onMessage(HawkMsg msg) {
		return super.onMessage(msg);
	}

	/**
	 * 协议响应
	 * 
	 * @param protocol
	 * @return
	 */
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		if (ServiceProxy.onProtocol("PlayerService", this, protocol)) {
			return true;
		}
		return super.onProtocol(protocol);
	}
}
