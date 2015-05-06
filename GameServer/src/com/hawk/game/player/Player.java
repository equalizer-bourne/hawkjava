package com.hawk.game.player;

import java.util.Map.Entry;

import org.hawk.app.HawkAppObj;
import org.hawk.app.HawkObjModule;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.service.HawkServiceProxy;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.module.PlayerIdleModule;
import com.hawk.game.module.PlayerLoginModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.game.util.GsConst;

/**
 * 玩家对象
 * 
 * @author hawk
 * 
 */
public class Player extends HawkAppObj {
	/**
	 * 协议日志记录器
	 */
	private static final Logger logger = LoggerFactory.getLogger("Protocol");
	
	/**
	 * 挂载玩家数据管理集合
	 */
	private PlayerData playerData;

	/**
	 * 组装状态
	 */
	private boolean assembleFinish;
	
	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	public Player(HawkXID xid) {
		super(xid);

		initModules();

		playerData = new PlayerData(this);
	}

	/**
	 * 初始化模块
	 * 
	 */
	public void initModules() {
		registerModule(GsConst.ModuleType.LOGIN_MODULE, new PlayerLoginModule(this));
		
		// 最后注册空闲模块, 用来消息收尾处理
		registerModule(GsConst.ModuleType.IDLE_MODULE, new PlayerIdleModule(this));
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
	 * 获取玩家实体
	 * 
	 * @return
	 */
	public PlayerEntity getEntity() {
		return playerData.getPlayerEntity();
	}

	/**
	 * 是否组装完成
	 * 
	 * @return
	 */
	public boolean isAssembleFinish() {
		return assembleFinish;
	}

	/**
	 * 设置组装完成状态
	 * 
	 * @param assembleFinish
	 */
	public void setAssembleFinish(boolean assembleFinish) {
		this.assembleFinish = assembleFinish;
	}

	/**
	 * 通知错误码
	 * 
	 * @param errCode
	 */
	public void sendError(int hpCode, int errCode) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(hpCode);
		builder.setErrCode(errCode);
		sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
	}

	/**
	 * 通知错误码
	 * 
	 * @param errCode
	 */
	public void sendError(int hpCode, int errCode, int errFlag) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(hpCode);
		builder.setErrCode(errCode);
		builder.setErrFlag(errFlag);
		sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
	}
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		if (protocol.getSize() >= 2048) {
			logger.info("send protocol size overflow, protocol: {}, size: {}", new Object[] { protocol.getType(), protocol.getSize() });
		}
		return super.sendProtocol(protocol);
	}
	
	/**
	 * 踢出玩家
	 * @param reason
	 */
	public void kickout(int reason) {
		session = null;
	}
	/**
	 * 玩家消息预处理
	 * 
	 * @param msg
	 * @return
	 */
	private boolean onPlayerMessage(HawkMsg msg) {
		// 优先服务拦截
		if (HawkServiceProxy.onMessage(this, msg)) {
			return true;
		}
		
		// 系统级消息, 所有模块都进行处理的消息
		if (msg.getMsg() == GsConst.MsgType.PLAYER_LOGIN) {
			for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
				PlayerModule playerModule = (PlayerModule) entry.getValue();
				playerModule.onPlayerLogin();
			}
			return true;
		} else if (msg.getMsg() == GsConst.MsgType.PLAYER_ASSEMBLE) {
			for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
				PlayerModule playerModule = (PlayerModule) entry.getValue();
				playerModule.onPlayerAssemble();
			}
			return true;
		} else if (msg.getMsg() == GsConst.MsgType.SESSION_CLOSED) {
			if (isAssembleFinish()) {
				for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
					PlayerModule playerModule = (PlayerModule) entry.getValue();
					playerModule.onPlayerLogout();
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 玩家协议预处理
	 * 
	 * @param protocol
	 * @return
	 */
	private boolean onPlayerProtocol(HawkProtocol protocol) {
		// 优先服务拦截
		if (HawkServiceProxy.onProtocol(this, protocol)) {
			return true;
		}
		
		// 玩家不在线而且不是登陆协议(非法协议时机)
		if (!isOnline() && !protocol.checkType(HP.code.LOGIN_C)) {
			HawkLog.errPrintln(String.format("player is offline, session: %s, protocol: %d", protocol.getSession().getIpAddr(), protocol.getType()));
			return true;
		}

		// 玩家未组装完成
		if (!isAssembleFinish() && !protocol.checkType(HP.code.LOGIN_C)) {
			HawkLog.errPrintln(String.format("player assemble unfinish, session: %s, protocol: %d", protocol.getSession().getIpAddr(), protocol.getType()));
			return true;
		}
		
		return false;
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		// 玩家未组装完成直接不走时钟tick机制
		if (!isAssembleFinish()) {
			return true;
		}
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
		if (onPlayerMessage(msg)) {
			return true;
		}
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
		if (onPlayerProtocol(protocol)) {
			return true;
		}
		return super.onProtocol(protocol);
	}

	/**
	 * 获取玩家id
	 * 
	 * @return
	 */
	public int getId() {
		return playerData.getPlayerEntity().getId();
	}

	/**
	 * 获取puid
	 * 
	 * @return
	 */
	public String getPuid() {
		return playerData.getPlayerEntity().getPuid();
	}

	/**
	 * 获取设备
	 * 
	 * @return
	 */
	public String getDevice() {
		return playerData.getPlayerEntity().getDevice();
	}

	/**
	 * 获取平台
	 * 
	 * @return
	 */
	public String getPlatform() {
		return playerData.getPlayerEntity().getPlatform();
	}

	/**
	 * 获取手机信息
	 * @return
	 */
	public String getPhoneInfo() {
		return playerData.getPlayerEntity().getPhoneInfo();
	}
	
	/**
	 * 获取钻石
	 * 
	 * @return
	 */
	public int getGold() {
		return playerData.getPlayerEntity().getGold();
	}

	/**
	 * 获取金币
	 * 
	 * @return
	 */
	public long getCoin() {
		return playerData.getPlayerEntity().getCoin();
	}

	/**
	 * 获取玩家vip等级
	 * 
	 * @return
	 */
	public int getVipLevel() {
		return playerData.getPlayerEntity().getVipLevel();
	}

	/**
	 * 获取玩家名字
	 * 
	 * @return
	 */
	public String getName() {
		return playerData.getPlayerEntity().getName();
	}

	/**
	 * 获取玩家等级
	 * 
	 * @return
	 */
	public int getLevel() {
		return 0;
	}

	/**
	 * 获取经验
	 * @return
	 */
	public int getExp() {
		return playerData.getPlayerEntity().getExp();
	}
	
	/**
	 * 获取会话ip地址
	 * 
	 * @return
	 */
	public String getIp() {
		if (session != null) {
			return session.getIpAddr();
		}
		return null;
	}
}
