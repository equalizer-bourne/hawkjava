package com.hawk.collector.analyser;

import net.sf.json.JSONObject;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.gson.JsonObject;

public class OperationData {
	/**
	 * 游戏名
	 */
	public String game = "";
	/**
	 * 平台名
	 */
	public String platform = "";
	/**
	 * 渠道名
	 */
	public String channel = "";
	/**
	 * 数据日期
	 */
	public String date = "";
	/**
	 * 总用户
	 */
	public int totalUser = 0;
	/**
	 * 总设备
	 */
	public int totalDevice = 0;
	/**
	 * 总付费用户
	 */
	public int totalPayUser = 0;
	/**
	 * 总付费设备
	 */
	public int totalPayDevice = 0;
	/**
	 * 总付费金额
	 */
	public int totalPayMoney = 0;
	/**
	 * 新增用户
	 */
	public int newUser = 0;
	/**
	 * 新增设备
	 */
	public int newDevice = 0;
	/**
	 * 日活跃用户
	 */
	public int userLogin = 0;
	/**
	 * 日活跃设备
	 */
	public int deviceLogin = 0;
	/**
	 * 当日付费设备
	 */
	public int payDevice = 0;
	/**
	 * 当日付费用户
	 */
	public int payUser = 0;
	/**
	 * 当日付费金额
	 */
	public int payMoney = 0;

	public float ARPU = 0;
	public float ARPD = 0;
	public float ARPPU = 0;

	/**
	 * 付费率
	 */
	public float PayRate = 0;

	public float LTV2 = 0;
	public float LTV3 = 0;
	public float LTV7 = 0;
	public float LTV14 = 0;
	public float LTV30 = 0;

	/**
	 * 用户留存数据
	 */
	public float userRetention2 = 0;
	public float userRetention3 = 0;
	public float userRetention7 = 0;
	public float userRetention14 = 0;
	public float userRetention30 = 0;

	/**
	 * 设备留存数
	 */
	public float deviceRetention2 = 0;
	public float deviceRetention3 = 0;
	public float deviceRetention7 = 0;
	public float deviceRetention14 = 0;
	public float deviceRetention30 = 0;

	public OperationData(String game) {
		this.game = game;
		date = HawkTime.getDateString();
	}

	public OperationData(String game, String data) {
		this.game = game;
		this.date = data;
	}
	
	public JsonObject toJsonObject(boolean onlyBase) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("game", game);
		jsonObject.addProperty("platform", platform);
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("date", date);
		jsonObject.addProperty("totalUser", totalUser);
		jsonObject.addProperty("newUser", newUser);
		jsonObject.addProperty("totalDevice", totalDevice);
		jsonObject.addProperty("newDevice", newDevice);
		jsonObject.addProperty("userLogin", userLogin);
		jsonObject.addProperty("deviceLogin", deviceLogin);
		jsonObject.addProperty("totalPayUser", totalPayUser);
		jsonObject.addProperty("payUser", payUser);
		jsonObject.addProperty("totalPayDevice", totalPayDevice);
		jsonObject.addProperty("payDevice", payDevice);
		jsonObject.addProperty("totalPayMoney", totalPayMoney);
		jsonObject.addProperty("payMoney", payMoney);
		jsonObject.addProperty("ARPU", HawkOSOperator.floatToString(ARPU, 2));
		jsonObject.addProperty("ARPD", HawkOSOperator.floatToString(ARPD, 2));
		jsonObject.addProperty("ARPPU", HawkOSOperator.floatToString(ARPPU, 2));
		jsonObject.addProperty("PayRate", HawkOSOperator.floatToString(PayRate, 4));
		if (!onlyBase) {
			jsonObject.addProperty("LTV2", HawkOSOperator.floatToString(LTV2, 2));
			jsonObject.addProperty("LTV3", HawkOSOperator.floatToString(LTV3, 2));
			jsonObject.addProperty("LTV7", HawkOSOperator.floatToString(LTV7, 2));
			jsonObject.addProperty("LTV14", HawkOSOperator.floatToString(LTV14, 2));
			jsonObject.addProperty("LTV30", HawkOSOperator.floatToString(LTV30, 2));
			jsonObject.addProperty("userRetention2", HawkOSOperator.floatToString(userRetention2, 4));
			jsonObject.addProperty("userRetention3", HawkOSOperator.floatToString(userRetention3, 4));
			jsonObject.addProperty("userRetention7", HawkOSOperator.floatToString(userRetention7, 4));
			jsonObject.addProperty("userRetention14", HawkOSOperator.floatToString(userRetention14, 4));
			jsonObject.addProperty("userRetention30", HawkOSOperator.floatToString(userRetention30, 4));
			jsonObject.addProperty("deviceRetention2", HawkOSOperator.floatToString(deviceRetention2, 4));
			jsonObject.addProperty("deviceRetention3", HawkOSOperator.floatToString(deviceRetention3, 4));
			jsonObject.addProperty("deviceRetention7", HawkOSOperator.floatToString(deviceRetention7, 4));
			jsonObject.addProperty("deviceRetention14", HawkOSOperator.floatToString(deviceRetention14, 4));
			jsonObject.addProperty("deviceRetention30", HawkOSOperator.floatToString(deviceRetention30, 4));
		}
		return jsonObject;
	}
	
	public String toJsonString(boolean onlyBase) {
		return toJsonObject(onlyBase).toString();
	}

	public boolean fromJsonString(String jsonInfo) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(jsonInfo);
			this.game = (String) jsonObject.get("game");
			this.platform = (String) jsonObject.get("platform");
			this.channel = (String) jsonObject.get("channel");
			this.date = (String) jsonObject.get("date");
			this.totalUser = (Integer) jsonObject.get("totalUser");
			this.newUser = (Integer) jsonObject.get("newUser");
			this.totalDevice = (Integer) jsonObject.get("totalDevice");
			this.newDevice = (Integer) jsonObject.get("newDevice");
			this.userLogin = (Integer) jsonObject.get("userLogin");
			this.deviceLogin = (Integer) jsonObject.get("deviceLogin");
			this.totalPayUser = (Integer) jsonObject.get("totalPayUser");
			this.payUser = (Integer) jsonObject.get("payUser");
			this.totalPayDevice = (Integer) jsonObject.get("totalPayDevice");
			this.payDevice = (Integer) jsonObject.get("payDevice");
			this.totalPayMoney = (Integer) jsonObject.get("totalPayMoney");
			this.payMoney = (Integer) jsonObject.get("payMoney");
			this.ARPU = Float.valueOf(jsonObject.getString("ARPU"));
			this.ARPD = Float.valueOf(jsonObject.getString("ARPD"));
			this.ARPPU = Float.valueOf(jsonObject.getString("ARPPU"));
			this.PayRate = Float.valueOf(jsonObject.getString("PayRate"));
			this.LTV2 = Float.valueOf(jsonObject.getString("LTV2"));
			this.LTV3 = Float.valueOf(jsonObject.getString("LTV3"));
			this.LTV7 = Float.valueOf(jsonObject.getString("LTV7"));
			this.LTV14 = Float.valueOf(jsonObject.getString("LTV14"));
			this.LTV30 = Float.valueOf(jsonObject.getString("LTV30"));
			this.userRetention2 = Float.valueOf(jsonObject.getString("userRetention2"));
			this.userRetention3 = Float.valueOf(jsonObject.getString("userRetention3"));
			this.userRetention7 = Float.valueOf(jsonObject.getString("userRetention7"));
			this.userRetention14 = Float.valueOf(jsonObject.getString("userRetention14"));
			this.userRetention30 = Float.valueOf(jsonObject.getString("userRetention30"));
			this.deviceRetention2 = Float.valueOf(jsonObject.getString("deviceRetention2"));
			this.deviceRetention3 = Float.valueOf(jsonObject.getString("deviceRetention3"));
			this.deviceRetention7 = Float.valueOf(jsonObject.getString("deviceRetention7"));
			this.deviceRetention14 = Float.valueOf(jsonObject.getString("deviceRetention14"));
			this.deviceRetention30 = Float.valueOf(jsonObject.getString("deviceRetention30"));
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
}
