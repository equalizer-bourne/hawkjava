package com.hawk.collector.handler.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.gson.JsonObject;
import com.hawk.collector.Collector;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取特定订单号信息
 * 
 * @author hawk
 */
public class FetchOrderInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&orderid=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String info = doFetch(params.get("game"), params.get("orderid"));
				HawkLog.logPrintln(info);
				CollectorHttpServer.response(httpExchange, info);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
	
	public static String doFetch(String game, String orderid) {
		Statement statement = null;
		try {
			String sql = String.format("SELECT platform, server, puid, orderMoney, payMoney, addGold, giftGold, currency, time FROM recharge WHERE game = '%s' AND pforder = '%s'", game, orderid);

			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				int column = 0;
				jsonObject.addProperty("orderid", orderid);
				jsonObject.addProperty("platform", resultSet.getString(++column));
				jsonObject.addProperty("server", resultSet.getString(++column));
				jsonObject.addProperty("puid", resultSet.getString(++column));
				jsonObject.addProperty("orderMoney", resultSet.getInt(++column));
				jsonObject.addProperty("pay", resultSet.getInt(++column));
				jsonObject.addProperty("addGold", resultSet.getInt(++column));
				jsonObject.addProperty("giftGold", resultSet.getInt(++column));
				jsonObject.addProperty("currency", resultSet.getString(++column));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				jsonObject.addProperty("time", sdf.format(resultSet.getTimestamp(++column)));
				return jsonObject.toString();
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
		return "";
	}
}
