package com.hawk.collector.handler.report;

import java.io.IOException;
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
 * 登陆信息收集处理
 * 
 * @author hawk
 */
public class ReportLoginHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&period=%d&time=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
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
		if (params != null) {
			String time = params.containsKey("time")? params.get("time") : HawkTime.getTimeString();
			String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, %s, '%s', '%s'", 
					params.get("game"), params.get("platform"), params.get("server"), 
					CollectorServices.getChannelFromPuid(params.get("puid")), params.get("puid"), 
					params.get("device"), params.get("playerid"), params.get("period"), 
					time, time.substring(0, 10));

			HawkLog.logPrintln("report_login: " + value);
			
			// 优先更新
			String sql = String.format("UPDATE login SET period = period + %d WHERE game = '%s' AND platform = '%s' AND server = '%s' AND puid = '%s' AND date = '%s'",
					Integer.valueOf(params.get("period")), params.get("game"), params.get("platform"), params.get("server"), params.get("puid"), time.substring(0, 10));
			
			if (DBManager.getInstance().executeSql(params.get("game"), sql) > 0) {
				return;
			}

			// 更新失败即插入新记录
			sql = String.format("INSERT INTO login(game, platform, server, channel, puid, device, playerid, period, time, date) VALUES(%s);", value);
			DBManager.getInstance().executeSql(params.get("game"), sql);
		}
	}
}
