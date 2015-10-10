package com.hawk.collector.handler.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.collector.AnalyserManager;
import com.hawk.collector.Collector;
import com.hawk.collector.CollectorServices;
import com.hawk.collector.analyser.OperationData;
import com.hawk.collector.http.CollectorHttpServer;
import com.hawk.collector.info.GamePlatform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author hawk
 */
public class DailyAnalyzeHandler implements HttpHandler {
	/**
	 * 格式: game=%s&date=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null && params.containsKey("game")) {
				Collector.checkToken(params.get("token"));
				String date = HawkTime.getDateString();
				if (params.containsKey("date")) {
					date = params.get("date");
				}
				
				Map<String, GamePlatform> gamePlatforms = new HashMap<String, GamePlatform>();
				GamePlatform gp = CollectorServices.getInstance().getGamePlatform(params.get("game"));
				if (gp != null) {
					gamePlatforms.put(params.get("game"), gp);	
				}
				
				AnalyserManager.getInstance().doDailyAnalyze(date, gamePlatforms);
				
				OperationData operationData = new OperationData(params.get("game"));
				operationData.date = date;
				while (!AnalyserManager.getInstance().getStatisticsData(operationData)) {
					HawkOSOperator.sleep();
				}
				CollectorHttpServer.response(httpExchange, operationData.toJsonString(false));
			} else {
				CollectorHttpServer.response(httpExchange, "404 Not Found");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
}
