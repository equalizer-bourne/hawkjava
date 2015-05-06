package com.hawk.collector.handler.service;

import java.io.IOException;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.collector.Collector;
import com.hawk.collector.analyser.ServerAnalyser;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取服务器信息
 * 
 * @author hawk
 */
public class FetchServerInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null) {
				Collector.checkToken(params.get("token"));
				String serverInfo = ServerAnalyser.fetchServerInfos(
						params.get("game"), 
						params.containsKey("platform") ? params.get("platform") : "");

				HawkLog.logPrintln(serverInfo);
				CollectorHttpServer.response(httpExchange, serverInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
}
