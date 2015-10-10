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
 * 获取活动统计信息
 * 
 * @author hawk
 */
public class FetchActivityStatisticsHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&channel=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String activityInfo = formatActivityStatistics(params.get("game"), params.get("platform"), params.get("channel"));
				HawkLog.logPrintln(activityInfo);
				CollectorHttpServer.response(httpExchange, activityInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}

	public static String formatActivityStatistics(String game, String platform, String channel) {
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String sql = "select activityid, count(distinct activityno), count(puid), sum(consumegold) FROM activity WHERE game = '" + game + "'";
			if (channel != null && channel.length() > 0) {
				sql += " and channel = '" + channel +"'";
			} else if (platform != null && platform.length() > 0) {
				sql += " and platform = '" + platform +"'";
			}
			sql += " group by activityid";
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("activityid", resultSet.getString(++column));
				jsonObject.addProperty("activityno", resultSet.getString(++column));
				jsonObject.addProperty("puidcount", resultSet.getString(++column));
				jsonObject.addProperty("goldconsume", resultSet.getString(++column));
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
