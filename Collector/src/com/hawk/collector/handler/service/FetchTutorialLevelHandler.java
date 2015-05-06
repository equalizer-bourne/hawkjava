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
 * 获取新手引导的等级分布信息
 * 
 * @author hawk
 */
public class FetchTutorialLevelHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&channel=%s&beginDate=%s&endDate=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String tutorialInfo = formatTutorialInfo(params.get("game"), params.get("platform"), params.get("channel"), 
						params.get("beginDate"), params.get("endDate"));
				HawkLog.logPrintln(tutorialInfo);
				CollectorHttpServer.response(httpExchange, tutorialInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}

	public static String formatTutorialInfo(String game, String platform, String channel, String beginDate, String endDate) {
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String sql = "select playerlevel, count(id) FROM tutorial WHERE game = '" + game + "'";
			sql += String.format(" and date >= '%s' and date <= '%s'", beginDate, endDate);
			if (channel != null && channel.length() > 0) {
				sql += " and channel = '" + channel +"'";
			} else if (platform != null && platform.length() > 0) {
				sql += " and platform = '" + platform +"'";
			}
			sql += " group by playerlevel";
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("level", resultSet.getString(++column));
				jsonObject.addProperty("count", resultSet.getString(++column));
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
