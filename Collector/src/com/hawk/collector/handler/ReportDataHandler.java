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

public class ReportDataHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playername=%s&arg[1-9]=%s&time=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', %s, %s, '%s', '%s'", 
											params.get("game"), params.get("platform"), params.get("server"), 
											params.get("puid"), params.get("device"), params.get("playerid"), params.get("playername"),
											params.containsKey("arg1")? params.get("arg1") : "",
											params.containsKey("arg2")? params.get("arg2") : "",
											params.containsKey("arg3")? params.get("arg3") : "",
											params.containsKey("arg4")? params.get("arg4") : "",
											params.containsKey("arg5")? params.get("arg5") : "",
											params.containsKey("arg6")? params.get("arg6") : "",
											params.containsKey("arg7")? params.get("arg7") : "",
											params.containsKey("arg8")? params.get("arg8") : "",
											params.containsKey("arg9")? params.get("arg9") : "",
											params.containsKey("time")? params.get("time") : HawkTime.getTimeString());
				
				String sql = String.format("INSERT INTO data(game, platform, server, puid, device, playerid, playername, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, time) VALUES(%s);", value);

				DBManager.getInstance().executeSql(sql);

				HawkLog.logPrintln("report_data: " + value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			CollectorHttpServer.response(httpExchange);
		}
	}
}
