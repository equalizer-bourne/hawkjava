package com.hawk.collector.handler.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
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
 * 获取钻石信息
 * 
 * @author hawk
 */
public class FetchGoldInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&channel=%s&changetype=%d&beginDate=%s&endDate=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String goldInfo = formatGoldInfo(params.get("game"), params.get("platform"), params.get("channel"), 
						Integer.valueOf(params.get("changetype")), params.get("beginDate"), params.get("endDate"));
				HawkLog.logPrintln(goldInfo);
				CollectorHttpServer.response(httpExchange, goldInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}

	public static String formatGoldInfo(String game, String platform, String channel, int changeType, String beginDate, String endDate) {
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String sql = "select changeaction, count(gold), sum(gold) FROM gold WHERE game = '" + game + "'";
			sql += String.format(" and date >= '%s' and date <= '%s'", beginDate, endDate);
			sql += String.format(" and changetype = %d", changeType);
			if (channel != null && channel.length() > 0) {
				sql += " and channel = '" + channel +"'";
			} else if (platform != null && platform.length() > 0) {
				sql += " and platform = '" + platform +"'";
			}
			sql += " group by changeaction";
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("changeaction", resultSet.getString(++column));
				jsonObject.addProperty("count", resultSet.getString(++column));
				jsonObject.addProperty("gold", resultSet.getString(++column));
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
