package com.hawk.collector.handler.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hawk.collector.Collector;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取累计充值排行数据
 * 
 * @author hawk
 */
public class FetchRechargeRankHandler implements HttpHandler {
	/**(type: 0获取排行榜, 1获取排行榜数量)
	 * 格式: game=%s&platform=%s&channel=%s&server=%d&beginDate=%s&endDate=%s&minPay=%d&maxPay=%d&type=%d
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String info = doFetch(params.get("game"), params);
				HawkLog.logPrintln(info);
				CollectorHttpServer.response(httpExchange, info);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}

	public static String doFetch(String game, Map<String, String> params) {
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String beginDate = HawkTime.getDateString();
			String endDate = HawkTime.getDateString();
			if (params.containsKey("beginDate")) {
				beginDate = params.get("beginDate");
			}
			
			if (params.containsKey("endDate")) {
				endDate = params.get("endDate");
			}
			
			String sql = String.format("select platform, channel, server, puid, count(puid) as payTimes, sum(payMoney) as totalPay from recharge where game = '%s' and date >= '%s' and date <= '%s'", game, beginDate, endDate);
			if (params.containsKey("platform") && params.get("platform").length() > 0) {
				sql += " and platform = '" + params.get("platform") +"'";
			}
			
			if (params.containsKey("channel") && params.get("channel").length() > 0) {
				sql += " and channel = '" + params.get("channel") +"'";
			}
			
			if (params.containsKey("server") && params.get("server").length() > 0) {
				sql += " and server = '" + params.get("server") +"'";
			}
			
			int minPay = params.containsKey("minPay")? Integer.valueOf(params.get("minPay")) : 0;
			int maxPay = params.containsKey("maxPay")? Integer.valueOf(params.get("maxPay")) : 99999999;
			sql += String.format(" group by platform, channel, server, puid having totalPay >= %d and totalPay <= %d ORDER BY totalPay DESC", minPay, maxPay);
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("platform", resultSet.getString(++column));
				jsonObject.addProperty("channel", resultSet.getString(++column));
				jsonObject.addProperty("server", resultSet.getInt(++column));
				jsonObject.addProperty("puid", resultSet.getString(++column));
				jsonObject.addProperty("payTimes", resultSet.getString(++column));
				jsonObject.addProperty("payMoney", resultSet.getInt(++column));
				jsonArray.add(jsonObject);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}
		}
		
		int type = 0;
		if (params.containsKey("type")) {
			type = Integer.valueOf(params.get("type"));
		}
		
		if (type == 1) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("rangeCount", jsonArray.size());
			return jsonObject.toString();
		}
		return jsonArray.toString();
	}
}
