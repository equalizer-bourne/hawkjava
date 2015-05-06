package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import com.hawk.game.ServerData;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.ProtoUtil;

/**
 * 玩家登陆模块
 * 
 * @author hawk
 */
public class PlayerLoginModule extends PlayerModule {
	/**
	 * 构造函数
	 * 
	 * @param player
	 */
	public PlayerLoginModule(Player player) {
		super(player);
	}

	/**
	 * 登陆协议处理
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.LOGIN_C_VALUE)
	private boolean onPlayerLogin(HawkProtocol cmd) {
		HPLogin protocol = cmd.parseProtocol(HPLogin.getDefaultInstance());
		HawkSession session = cmd.getSession();
		int hpCode = cmd.getType();

		// 在线人数达到上限
		int sessionMaxSize = HawkApp.getInstance().getAppCfg().getSessionMaxSize();
		if (sessionMaxSize > 0 && ServerData.getInstance().getOnlinePlayer() >= sessionMaxSize) {
			session.sendProtocol(ProtoUtil.genErrorProtocol(hpCode, Status.error.ONLINE_MAX_LIMIT_VALUE, 1));
			return false;
		}

		// 参数解析
		String puid = protocol.getPuid().trim();
		String device = protocol.getDeviceId().trim().toLowerCase();
		String platInfo = protocol.getPlatform().trim().toLowerCase();

		// 解析平台名称
		// phonetype#osversion#OS#platform#channel#id#mac#rmac&rip&route
		String[] platInfos = platInfo.split("#");
		String phoneType = "";
		String osVersion = "";
		String osName = "";
		String platName = "";
		String channel = "";
		for (int i = 0; i < platInfos.length; i++) {
			if (i == 0) {
				phoneType = platInfos[i];
			} else if (i == 1) {
				osVersion = platInfos[i];
			} else if (i == 2) {
				osName = platInfos[i];
			} else if (i == 3) {
				platName = platInfos[i];
			} else if (i == 4) {
				channel = platInfos[i];
			}
		}

		// 机器信息
		String phoneInfo = osName + "#" + osVersion + "#" + phoneType;
		// 整理平台名字
		String platform = channel;
		if (!channel.startsWith(platName)) {
			platform = platName + "_" + channel;
		}

		// 加载玩家实体信息
		PlayerEntity playerEntity = player.getPlayerData().loadPlayer(puid);

		// 更新玩家设备相关信息
		if (playerEntity != null) {
			boolean needUpdate = false;
			if (playerEntity.getDevice() == null || playerEntity.getDevice().length() <= 0) {
				playerEntity.setDevice(device);
				needUpdate = true;
			}

			if (playerEntity.getPlatform() == null || playerEntity.getPlatform().length() <= 0) {
				playerEntity.setPlatform(platform);
				needUpdate = true;
			}

			if (playerEntity.getPhoneInfo() == null || playerEntity.getPhoneInfo().length() <= 0) {
				playerEntity.setPhoneInfo(phoneInfo);
				needUpdate = true;
			}

			// 回写设备信息
			if (needUpdate) {
				playerEntity.notifyUpdate(true);
			}
		}

		// 登陆回复协议
		HPLoginRet.Builder response = HPLoginRet.newBuilder();
		response.setPlayerId(playerEntity.getId());
		// 设置时间戳
		response.setTimeStamp(HawkTime.getSeconds());

		// 绑定会话
		player.setSession(session);
		// 发送登陆成功协议
		sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
		return true;
	}

	@Override
	protected boolean onPlayerLogout() {
		// 重要数据下线就存储
		player.getEntity().notifyUpdate(false);
		return true;
	}
}
