package com.hawk.collector.handler.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Map;

import org.hawk.cryption.HawkBase64;
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
 * 获取普通数据
 * 
 * @author hawk
 */
public class FetchDataInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&sql=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String sql = params.containsKey("sql")? params.get("sql") : "";
				sql = sql.trim().replace('_', '/').replace('-', '=');
				sql = new String(HawkBase64.decode(sql));
				String info = doFetch(params.get("game"), sql);
				HawkLog.logPrintln(info);
				CollectorHttpServer.response(httpExchange, info);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
	
	public static String doFetch(String game, String sql) {
		if (sql == null || !sql.startsWith("SELECT") || sql.indexOf("FROM data") <= 0 || sql.indexOf(";") > 0) {
			return "";
		}
		
		Statement statement = null;
		try {
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			ResultSetMetaData rsmd = resultSet.getMetaData() ; 
			int columnCount = rsmd.getColumnCount();
			JsonArray jsonArray = new JsonArray();
			while (resultSet.next()) {
				JsonObject jsonObject = new JsonObject();
				for (int i=0; i<columnCount; i++) {
					jsonObject.addProperty(rsmd.getColumnName(i+1), resultSet.getString(i+1));
				}
				jsonArray.add(jsonObject);
			}
			return jsonArray.toString();
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
