package com.hawk.collector.handler.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.google.gson.JsonArray;
import com.hawk.collector.Collector;
import com.hawk.collector.CollectorServices;
import com.hawk.collector.analyser.OperationAnalyser;
import com.hawk.collector.analyser.OperationData;
import com.hawk.collector.http.CollectorHttpServer;
import com.hawk.collector.info.GamePlatform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FetchGeneralInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&type=%d&date=%s 
	 * type: 1表示平台概要信息, 2表示渠道概要信息
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null && params.containsKey("game") && params.containsKey("type") && params.containsKey("date")) {
				Collector.checkToken(params.get("token"));
				GamePlatform gamePlatform = CollectorServices.getInstance().getGamePlatform(params.get("game"));
				if (gamePlatform != null) {
					OperationData operationData = null;
					JsonArray jsonArray = new JsonArray();

					String[] platforms = gamePlatform.getPlatform().split(",");
					String[] channels = gamePlatform.getChannel().split(",");
					int type = Integer.valueOf(params.get("type"));
					if (type == 1) {
						for (String platform : platforms) {
							operationData = fetchOperationInfo(params.get("game"), platform, "", params.get("date"));
							if (operationData != null) {
								jsonArray.add(operationData.toJsonObject(false));
							}
						}
					} else if (type == 2) {
						for (String channel : channels) {
							operationData = fetchOperationInfo(params.get("game"), "", channel, params.get("date"));
							if (operationData != null) {
								jsonArray.add(operationData.toJsonObject(false));
							}
						}
					}
					
					HawkLog.logPrintln(jsonArray.toString());
					CollectorHttpServer.response(httpExchange, jsonArray.toString());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
	
	private OperationData fetchOperationInfo(String game, String platform, String channel, String date) {
		OperationData operationData = null;

		Calendar calendar = HawkTime.stringToCalendar(date);		
		int dayDiff = HawkTime.calendarDiff(HawkTime.getCalendar(), calendar);
		if (dayDiff > 1) {
			List<String> statisticsInfo = OperationAnalyser.fetchStatisticsInfo(game, platform, channel, date, date);
			if (statisticsInfo != null && statisticsInfo.size() > 0) {
				operationData = new OperationData(game);
				if (!operationData.fromJsonString(statisticsInfo.get(0))) {
					operationData = null;
				}
			}
		} 

		if (operationData == null) {
			operationData = OperationAnalyser.fetchOperationInfos(game, platform, channel, date, 
					OperationAnalyser.DATE_TYPE_BASE | OperationAnalyser.DATE_TYPE_RETENTION, false);
		}
		
		return operationData;
	}
}