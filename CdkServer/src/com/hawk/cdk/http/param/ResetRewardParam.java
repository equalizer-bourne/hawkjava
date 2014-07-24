package com.hawk.cdk.http.param;

import java.util.Map;

import org.hawk.os.HawkException;

import com.hawk.cdk.CdkServices;

/**
 * cdk类型奖励重置
 * 
 * @author hawk
 */
public class ResetRewardParam {
	private String game;
	private String platform;
	private String type;
	private String reward;
	private String starttime;
	private String endtime;

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

	public boolean initParam(Map<String, String> params) {
		try {
			game = params.get("game");
			type = params.get("type");
			
			if (game.length() > CdkServices.CDK_HEADER_SIZE) {
				game = game.substring(0, CdkServices.CDK_HEADER_SIZE);
			}
			
			if (type.length() > CdkServices.CDK_HEADER_SIZE) {
				type = type.substring(0, CdkServices.CDK_HEADER_SIZE);
			}
			
			if (params.containsKey("platform")) {
				platform = params.get("platform");
			}
			
			if (params.containsKey("reward")) {
				reward = params.get("reward");
			}
			
			if (params.containsKey("starttime")) {
				starttime = params.get("starttime");
			}

			if (params.containsKey("endtime")) {
				endtime = params.get("endtime");
			}

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
