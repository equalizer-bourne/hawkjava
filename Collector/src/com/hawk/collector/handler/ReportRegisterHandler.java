package com.hawk.collector.handler;

import java.io.IOException;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

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
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				String value = String.format("'%s', '%s', '%s', '%s', '%s', %s, '%s'", 
											params.get("game"), params.get("platform"), params.get("server"), 
											params.get("puid"), params.get("device"), params.get("playerid"), 
											params.containsKey("time")? params.get("time") : HawkTime.getTimeString());
				
				String sql = String.format("INSERT INTO register(game, platform, server, puid, device, playerid, time) VALUES(%s);", value);

				DBManager.getInstance().executeSql(sql);

				HawkLog.logPrintln("report_register: " + value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			CollectorHttpServer.response(httpExchange);
		}
	}
}
