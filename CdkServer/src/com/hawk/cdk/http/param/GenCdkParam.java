package com.hawk.cdk.http.param;

import java.util.Map;

import org.hawk.os.HawkException;

import com.hawk.cdk.CdkServices;

/**
 * cdk生成参数
 * 
 * @author hawk
 */
public class GenCdkParam {
	private String game;
	private String platform;
	private String type;
	private String reward;
	private String starttime;
	private String endtime;
	private int count;

	public GenCdkParam() {
		game = "";
		platform = "";
		type = "";
		reward = "";
		starttime = "";
		endtime = "";
		count = 0;
	}

	public void toLowerCase() {
		if (game != null) {
			game.toLowerCase();
		}

		if (platform != null) {
			platform.toLowerCase();
		}

		if (type != null) {
			type.toLowerCase();
		}

		if (reward != null) {
			reward.toLowerCase();
		}

		if (starttime != null) {
			starttime.toLowerCase();
		}

		if (endtime != null) {
			endtime.toLowerCase();
		}
	}

	public boolean isValid() {
		return game.length() >= 2 && type.length() >= 2 && reward.length() > 0 && count > 0;
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * 参数解析
	 * 
	 * @param params
	 * @return
	 */
	public boolean initParam(Map<String, String> params) {
		try {
			game = params.get("game");
			type = params.get("type");
			reward = params.get("reward");

			if (game.length() > CdkServices.CDK_HEADER_SIZE) {
				game = game.substring(0, CdkServices.CDK_HEADER_SIZE);
			}

			if (type.length() > CdkServices.CDK_HEADER_SIZE) {
				type = type.substring(0, CdkServices.CDK_HEADER_SIZE);
			}

			if (params.containsKey("platform")) {
				platform = params.get("platform");
			}

			if (params.containsKey("starttime")) {
				starttime = params.get("starttime");
			}

			if (params.containsKey("endtime")) {
				endtime = params.get("endtime");
			}

			count = Integer.valueOf(params.get("count"));

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
