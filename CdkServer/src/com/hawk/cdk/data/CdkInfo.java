package com.hawk.cdk.data;

import com.hawk.cdk.util.CdkUtil;

/**
 * CDK 包含信息
 * 
 * @author hawk
 */
public class CdkInfo {
	/**
	 * cdk字符串
	 */
	private String cdk;
	/**
	 * 游戏名(2个字符)
	 */
	private String game;
	/**
	 * 平台限制
	 */
	private String platform;
	/**
	 * 服务器信息
	 */
	private String server;
	/**
	 * 玩家id
	 */
	private String playerid;
	/**
	 * 玩家名
	 */
	private String playername;
	/**
	 * 使用名字
	 */
	private String usetime;
	/**
	 * 奖励信息
	 */
	private String reward;

	public String getCdk() {
		return cdk;
	}

	public void setCdk(String cdk) {
		this.cdk = cdk;
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

	public String getPlayername() {
		return playername;
	}

	public void setPlayername(String playername) {
		this.playername = playername;
	}

	public String getUsetime() {
		return usetime;
	}

	public void setUsetime(String usetime) {
		this.usetime = usetime;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return String.format("cdk=%s&game=%s&platform=%s&server=%s&playerid=%s&playername=%s&usetime=%s&reward=%s", cdk, game, platform, server, playerid, playername, usetime, reward);
	}

	/**
	 * 从字符串转换
	 * 
	 * @param info
	 * @return
	 */
	public boolean parse(String info) {
		String[] params = info.split("&");
		if (params != null && params.length >= 4) {
			for (String param : params) {
				String[] kv = param.split("=");
				if (kv != null && kv.length == 2 && kv[0].length() > 0 && kv[1].length() > 0) {
					if ("cdk".equals(kv[0])) {
						cdk = kv[1];
					} else if ("game".equals(kv[0])) {
						game = kv[1];
					} else if ("platform".equals(kv[0])) {
						platform = kv[1];
					} else if ("server".equals(kv[0])) {
						server = kv[1];
					} else if ("playerid".equals(kv[0])) {
						playerid = kv[1];
					} else if ("playername".equals(kv[0])) {
						playername = kv[1];
					} else if ("usetime".equals(kv[0])) {
						usetime = kv[1];
					} else if ("reward".equals(kv[0])) {
						reward = kv[1];
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 是否被使用
	 * 
	 * @return
	 */
	public boolean isBeused() {
		if (playerid != null && playerid.length() > 0 && !"null".equals(playerid)) {
			return true;
		}

		if (playername != null && playername.length() > 0 && !"null".equals(playername)) {
			return true;
		}

		return false;
	}

	/**
	 * 设置被使用
	 */
	public boolean setUsed(String game, String platform, String server, String playerid, String playername, String reward) {
		this.game = game;
		this.platform = platform;
		this.server = server;
		this.playerid = playerid;
		this.playername = playername;
		this.reward = reward;
		this.usetime = CdkUtil.getDateString();
		return true;
	}
}
