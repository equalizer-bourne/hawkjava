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
 * 充值数据上报处理
 * 
 * @author hawk
 */
public class ReportRechargeHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playername=%s&playerlevel=%d&orderid=%s&productid=%spay=%d&currency=%s&time=%s
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
			String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', %s, '%s', %s, '%s', '%s', '%s'", 
										params.get("orderid"), params.get("game"), params.get("platform"), params.get("server"), 
										CollectorServices.getChannelFromPuid(params.get("puid")), params.get("puid"), 
										params.get("device"), params.get("playerid"), params.get("playername"), params.get("playerlevel"),
										params.get("productid"), params.get("pay"), params.containsKey("currency")? params.get("currency") : "rmb", 
										time, time.substring(0, 10));
			
			String sql = String.format("INSERT INTO recharge(orderid, game, platform, channel, server, puid, device, playerid, playername, playerlevel, productid, pay, currency, time, date) VALUES(%s);", value);

			DBManager.getInstance().executeSql(params.get("game"), sql);

			HawkLog.logPrintln("report_recharge: " + value);
		}
	}
}
