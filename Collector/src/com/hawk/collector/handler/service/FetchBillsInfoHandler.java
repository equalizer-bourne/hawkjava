package com.hawk.collector.handler.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hawk.collector.Collector;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取渠道对账单信息
 * 
 * @author hawk
 */
public class FetchBillsInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&channel=%s&beginDate=%s&endDate=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String info = doFetch(params.get("game"), params.get("channel"), params.get("beginDate"), params.get("endDate"));
				HawkLog.logPrintln(info);
				CollectorHttpServer.response(httpExchange, info);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
	
	public static String doFetch(String game, String channel, String beginDate, String endDate) {
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String sql = String.format("SELECT orderid, platform, server, puid, pay, currency, time FROM recharge WHERE game = '%s' AND date >= '%s' AND date <= '%s'", 
					game, beginDate, endDate);
			
			if (channel != null && channel.length() > 0) {
				sql = String.format("SELECT orderid, platform, server, puid, pay, currency, time FROM recharge WHERE game = '%s' AND channel = '%s' AND date >= '%s' AND date <= '%s'", 
						game, channel, beginDate, endDate);
			}
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("orderid", resultSet.getString(++column));
				jsonObject.addProperty("platform", resultSet.getString(++column));
				jsonObject.addProperty("server", resultSet.getString(++column));	
				jsonObject.addProperty("puid", resultSet.getString(++column));	
				jsonObject.addProperty("pay", resultSet.getInt(++column));
				jsonObject.addProperty("currency", resultSet.getString(++column));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				jsonObject.addProperty("time", sdf.format(resultSet.getTimestamp(++column)));
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
		return jsonArray.toString();
	}
}
