package com.hawk.collector.handler.report;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.collector.Collector;
import com.hawk.collector.CollectorServices;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 注册信息收集处理
 * 
 * @author hawk
 */
public class ReportRegisterHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&time=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		// 写注册信息
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			Collector.checkToken(params.get("token"));
			doReport(params);
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			CollectorHttpServer.response(httpExchange, null);
		}
	}
	
	public static void doReport(Map<String, String> params) throws Exception {
		String time = (params != null && params.containsKey("time"))? params.get("time") : HawkTime.getTimeString();
		try {
			// 写注册信息
			if (params != null) {
				String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s'", 
											params.get("game"), params.get("platform"), params.get("server"), 
											CollectorServices.getChannelFromPuid(params.get("puid")), params.get("puid"), 
											params.get("device"), params.get("playerid"), 
											time, time.substring(0, 10));
				
				String sql = String.format("INSERT INTO register(game, platform, server, channel, puid, device, playerid, time, date) VALUES(%s);", value);
	
				DBManager.getInstance().executeSql(params.get("game"), sql);
	
				HawkLog.logPrintln("report_register: " + value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		try {
			// 写用户puid信息
			if (params != null) {
				// 先判断用户puid是否存在
				Statement statement = DBManager.getInstance().createStatement(params.get("game"));
				if (statement != null) {
					ResultSet resultSet = statement.executeQuery(String.format("select * from puid where game = '%s' and puid = '%s'", params.get("game"), params.get("puid")));
					// 已存在此puid
					if (resultSet != null && resultSet.next()) {
						return;
					}
				}
				
				String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s'", 
											params.get("game"), params.get("puid"), params.get("platform"), params.get("server"), 
											CollectorServices.getChannelFromPuid(params.get("puid")), params.get("device"), params.get("playerid"), 
											time, time.substring(0, 10));
				
				String sql = String.format("INSERT INTO puid(game, puid, platform, server, channel, device, playerid, time, date) VALUES(%s);", value);
	
				DBManager.getInstance().executeSql(params.get("game"), sql);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			// 写设备信息
			if (params != null) {
				// 先判断设备是否存在
				Statement statement = DBManager.getInstance().createStatement(params.get("game"));
				if (statement != null) {
					ResultSet resultSet = statement.executeQuery(String.format("select * from device where game = '%s' and device = '%s'", params.get("game"), params.get("device")));
					// 已存在此设备
					if (resultSet != null && resultSet.next()) {
						return;
					}
				}
				
				String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s'", 
											params.get("game"), params.get("device"), params.get("platform"), params.get("server"), 
											CollectorServices.getChannelFromPuid(params.get("puid")), params.get("puid"), params.get("playerid"), 
											time, time.substring(0, 10));
				
				String sql = String.format("INSERT INTO device(game, device, platform, server, channel, puid, playerid, time, date) VALUES(%s);", value);
	
				DBManager.getInstance().executeSql(params.get("game"), sql);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
