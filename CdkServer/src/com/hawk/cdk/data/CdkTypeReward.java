package com.hawk.cdk.data;

import com.hawk.cdk.util.CdkUtil;

/**
 * CDK类型奖励
 * 
 * @author hawk
 */
public class CdkTypeReward {
	/**
	 * 游戏名(取2个字符)
	 */
	private String game;
	/**
	 * 限制平台
	 */
	private String platform;
	/**
	 * 类型(取2个字符)
	 */
	private String type;
	/**
	 * 奖励信息
	 */
	private String reward;
	/**
	 * 创建时间
	 */
	private String createtime;
	/**
	 * 有效期(开始时间)
	 */
	private String starttime;
	/**
	 * 有效期(结束时间)
	 */
	private String endtime;

	public CdkTypeReward() {
		this.game = "";
		this.platform = "";
		this.type = "";
		this.reward = "";
		this.starttime = "";
		this.endtime = "";
		this.createtime = CdkUtil.getDateString();
	}

	public String getGame() {
		return game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getCdkGameType() {
		return game + "." + type;
	}

	/**
	 * 转换为字符串
	 */
	@Override
	public String toString() {
		return String.format("game=%s&platform=%s&type=%s&reward=%s&createtime=%s&starttime=%s&endtime=%s", game, platform, type, reward, createtime, starttime, endtime);
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
					if ("game".equals(kv[0])) {
						game = kv[1];
					} else if ("platform".equals(kv[0])) {
						platform = kv[1];
					} else if ("type".equals(kv[0])) {
						type = kv[1];
					} else if ("reward".equals(kv[0])) {
						reward = kv[1];
					} else if ("createtime".equals(kv[0])) {
						createtime = kv[1];
					} else if ("starttime".equals(kv[0])) {
						starttime = kv[1];
					} else if ("endtime".equals(kv[0])) {
						endtime = kv[1];
					}
				}
			}
			return isValid();
		}
		return false;
	}

	/**
	 * 是否有效
	 * 
	 * @return
	 */
	public boolean isValid() {
		return game.length() > 0 && type.length() > 0 && reward.length() > 0 && createtime.length() > 0;
	}

	/**
	 * 有效期未开始
	 * 
	 * @return
	 */
	public boolean isInFuture() {
		String currenttime = CdkUtil.getDateString();
		if (starttime.length() > 0 && !"null".equals(starttime) && currenttime.compareToIgnoreCase(starttime) < 0) {
			return true;
		}

		return false;
	}

	/**
	 * 是否已过期
	 * 
	 * @return
	 */
	public boolean isTimeout() {
		String currenttime = CdkUtil.getDateString();
		if (endtime.length() > 0 && currenttime.compareToIgnoreCase(endtime) > 0) {
			return true;
		}

		return false;
	}
}
