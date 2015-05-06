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
import com.hawk.collector.analyser.OperationAnalyser;
import com.hawk.collector.analyser.OperationData;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取实时运营数据信息
 * 
 * @author hawk
 */
public class FetchOperationInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&channel=%s&date=%s&statistics=%d&dataType=%d
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				boolean saveStatistics = false;
				try {
					if (params.containsKey("statistics")) {
						if (Integer.valueOf(params.get("statistics")) > 0) {
							saveStatistics = true;
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				// 获取数据类型(可分离计算)
				int dataType = OperationAnalyser.DATE_TYPE_BASE | OperationAnalyser.DATE_TYPE_RETENTION | OperationAnalyser.DATE_TYPE_LTV;
				if (params.containsKey("dataType")) {
					dataType = Integer.valueOf(params.get("dataType"));
				}
				
				JsonArray jsonArray = new JsonArray();
				if (params.containsKey("beginDate") && params.containsKey("endDate")) {
					Calendar beginDate = HawkTime.stringToCalendar(params.get("beginDate"));
					Calendar endDate = HawkTime.stringToCalendar(params.get("endDate"));
					while (!beginDate.after(endDate)) {
						OperationData operationData = fetchOperationInfo(
								params.get("game"), 
								params.containsKey("platform") ? params.get("platform") : "",
								params.containsKey("channel") ? params.get("channel") : "",
								HawkTime.getDateString(beginDate), dataType, saveStatistics);
						
						jsonArray.add(operationData.toJsonObject(dataType == OperationAnalyser.DATE_TYPE_BASE));
						beginDate.add(Calendar.DAY_OF_YEAR, 1);
					}
				} else {
					OperationData operationData = fetchOperationInfo(
							params.get("game"), 
							params.containsKey("platform") ? params.get("platform") : "",
							params.containsKey("channel") ? params.get("channel") : "",
							params.containsKey("date") ? params.get("date") : HawkTime.getDateString(), 
							dataType, saveStatistics);
					
					jsonArray.add(operationData.toJsonObject(dataType == OperationAnalyser.DATE_TYPE_BASE));
				}
				
				HawkLog.logPrintln(jsonArray.toString());
				CollectorHttpServer.response(httpExchange, jsonArray.toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
	
	private OperationData fetchOperationInfo(String game, String platform, String channel, String date, int dataType, boolean saveStatistics) {
		OperationData operationData = null;

		// 当前一周内数据采用实时计算, 否则按照历史数据计算
		Calendar calendar = HawkTime.stringToCalendar(date);		
		int dayDiff = HawkTime.calendarDiff(HawkTime.getCalendar(), calendar);
		if (dayDiff > 7) {
			List<String> statisticsInfo = OperationAnalyser.fetchStatisticsInfo(game, platform, channel, date, date);
			if (statisticsInfo != null && statisticsInfo.size() > 0) {
				operationData = new OperationData(game);
				if (!operationData.fromJsonString(statisticsInfo.get(0))) {
					operationData = null;
				}
			}
		} 

		if (operationData == null) {
			operationData = OperationAnalyser.fetchOperationInfos(game, platform, channel, date, dataType, saveStatistics);
		}
		
		return operationData;
	}
}
