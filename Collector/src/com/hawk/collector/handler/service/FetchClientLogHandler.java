package com.hawk.collector.handler.service;

import java.io.IOException;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.shell.HawkShellExecutor;

import com.hawk.collector.Collector;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取服务器信息
 * 
 * @author hawk
 */
public class FetchClientLogHandler implements HttpHandler {
	/**
	 * 格式: 
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			if (params != null && params.containsKey("game") && params.containsKey("logType") && params.containsKey("logKey")) {
				Collector.checkToken(params.get("token"));
				String logPath = HawkOSOperator.getWorkPath() + "userlog/" + params.get("game") + "/" + params.get("logType") + "/";
				String cmd = "sh -c cat " + logPath + "/*" + params.get("logKey") + "*";
				String result = HawkShellExecutor.execute(cmd, 30000);
				CollectorHttpServer.response(httpExchange, result);
			} else {
				CollectorHttpServer.response(httpExchange, "params error");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, HawkException.formatStackMsg(e));
		}
	}
}
