package com.hawk.collector.handler.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.collector.Collector;
import com.hawk.collector.analyser.OperationAnalyser;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取历史分析运营数据
 * 
 * @author hawk
 */
public class FetchStatisticsInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&channel=%s&beginDate=%s&endDate=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				List<String> statisticsInfo = OperationAnalyser.fetchStatisticsInfo(
						params.get("game"), 
						params.containsKey("platform") ? params.get("platform") : "",
						params.containsKey("channel") ? params.get("channel") : "",
						params.get("beginDate"), params.get("endDate"));

				JSONArray jsonArray = new JSONArray();
				for (String info : statisticsInfo) {
					HawkLog.logPrintln(info);
					jsonArray.add(info);
				}
				CollectorHttpServer.response(httpExchange, jsonArray.toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
}
