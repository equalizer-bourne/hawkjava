package com.hawk.cdk.http.param;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.services.HawkCdkService;

/**
 * cdk使用参数
 * 
 * @author hawk
 */
public class UseCdkParam {
	private String game;
	private String platform;
	private String server;
	private String playerid;
	private String puid;
	private String playername;
	private String cdk;
	private String reward; // 用作返回值

	public void toLowerCase() {
		if (game != null) {
			game = game.toLowerCase();
		}

		if (platform != null) {
			platform = platform.toLowerCase();
		}

		if (server != null) {
			server = server.toLowerCase();
		}

		if (playerid != null) {
			playerid = playerid.toLowerCase();
		}

		if (puid != null) {
			puid = puid.toLowerCase();
		}

		if (playername != null) {
			playername = playername.toLowerCase();
		}

		if (cdk != null) {
			cdk = cdk.toLowerCase();
		}
	}

	public String getGame() {
		return game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPlayerid() {
		return playerid;
	}

	public void setPlayerid(String playerid) {
		this.playerid = playerid;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public String getPlayername() {
		return playername;
	}

	public void setPlayername(String playername) {
		this.playername = playername;
	}

	public String getCdk() {
		return cdk;
	}

	public void setCdk(String cdk) {
		this.cdk = cdk;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public boolean initParam(Map<String, String> params) {
		try {
			game = params.get("game");
			platform = params.get("platform");
			server = params.get("server");
			playerid = params.get("playerid");
			puid = params.get("puid");
			playername = params.get("playername");
			cdk = params.get("cdk");

			if (game.length() > HawkCdkService.CDK_HEADER_SIZE) {
				game = game.substring(0, HawkCdkService.CDK_HEADER_SIZE);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
