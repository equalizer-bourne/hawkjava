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
 * 充值数据上报处理
 * 
 * @author hawk
 */
public class ReportRechargeHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playername=%s&playerlevel=%d&orderid=%s&pay=%d&currency=%s&time=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', %s, %s, '%s', '%s'", 
											params.get("orderid"), params.get("game"), params.get("platform"), params.get("server"), 
											params.get("puid"), params.get("device"), params.get("playerid"), params.get("playername"), params.get("playerlevel"),
											params.get("pay"), params.containsKey("currency")? params.get("currency") : "rmb", 
											params.containsKey("time")? params.get("time") : HawkTime.getTimeString());
				
				String sql = String.format("INSERT INTO recharge(orderid, game, platform, server, puid, device, playerid, playername, playerlevel, pay, currency, time) VALUES(%s);", value);

				DBManager.getInstance().executeSql(sql);

				HawkLog.logPrintln("report_recharge: " + value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			CollectorHttpServer.response(httpExchange);
		}
	}
}
