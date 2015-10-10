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
 * 活动参与消耗信息收集处理
 * 
 * @author hawk
 */
public class ReportActivityHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playerlevel=%d&activityid=%d&activityno=%d&consumegold=%d&time=%s
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
			String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %s, %s, 1, %s, '%s', '%s'", 
					params.get("game"), params.get("platform"), params.get("server"), 
					CollectorServices.getChannelFromPuid(params.get("puid")), params.get("puid"), 
					params.get("device"), params.get("playerid"), params.get("playerlevel"), 
					params.get("activityid"), params.get("activityno"), params.get("consumegold"),
					time, time.substring(0, 10));

			HawkLog.logPrintln("report_activity " + value);
			
			// 优先更新
			String sql = String.format("UPDATE activity SET jointimes = jointimes + 1, consumegold = consumegold + %s, playerlevel = %d, time = '%s', date = '%s' WHERE game = '%s' AND platform = '%s' AND server = '%s' AND activityid = %d AND activityno = %d AND puid = '%s'",
					params.get("consumegold"), Integer.valueOf(params.get("playerlevel")), time, time.substring(0, 10),
					params.get("game"), params.get("platform"), params.get("server"), 
					Integer.valueOf(params.get("activityid")), Integer.valueOf(params.get("activityno")), params.get("puid"));
			
			if (DBManager.getInstance().executeSql(params.get("game"), sql) > 0) {
				return;
			}

			// 更新失败即插入新记录
			sql = String.format("INSERT INTO activity(game, platform, server, channel, puid, device, playerid, playerlevel, activityid, activityno, jointimes, consumegold, time, date) VALUES(%s);", value);
			DBManager.getInstance().executeSql(params.get("game"), sql);
		}
	}
}
