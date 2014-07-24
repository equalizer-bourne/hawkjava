package com.hawk.collector.http;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.collector.handler.ReportDataHandler;
import com.hawk.collector.handler.ReportLoginHandler;
import com.hawk.collector.handler.ReportRechargeHandler;
import com.hawk.collector.handler.ReportRegisterHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 收集的HTTP服务
 * 
 * @author hawk
 */
public class CollectorHttpServer {
	/**
	 * 服务器对象
	 */
	private HttpServer httpServer = null;
	/**
	 * 数据库管理器单例对象
	 */
	static CollectorHttpServer instance;

	/**
	 * 获取数据库管理器单例对象
	 * 
	 * @return
	 */
	public static CollectorHttpServer getInstance() {
		if (instance == null) {
			instance = new CollectorHttpServer();
		}
		return instance;
	}

	/**
	 * 函数
	 */
	private CollectorHttpServer() {
	}

	/**
	 * 开启服务
	 */
	public boolean setup(String addr, int port, int pool) {
		try {
			if (addr != null && addr.length() > 0) {
				httpServer = HttpServer.create(new InetSocketAddress(addr, port), 0);
				httpServer.setExecutor(Executors.newFixedThreadPool(pool));

				installContext();

				httpServer.start();
				HawkLog.logPrintln("Reporter Http Server [" + addr + ":" + port + "] Start OK.");
				return true;
			}
		} catch (BindException e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("Reporter Http Server Bind Failed, Address: " + addr + ":" + port);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 创建http上下文
	 * 
	 * @return
	 */
	private boolean installContext() {
		try {
			httpServer.createContext("/report_register", new ReportRegisterHandler());
			httpServer.createContext("/report_login", new ReportLoginHandler());
			httpServer.createContext("/report_recharge", new ReportRechargeHandler());
			httpServer.createContext("/report_data", new ReportDataHandler());
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 停止服务器
	 */
	public void stop() {
		try {
			if (httpServer != null) {
				httpServer.stop(0);
				httpServer = null;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * http请求回应内容
	 * 
	 * @param httpExchange
	 * @param response
	 */
	public static void response(HttpExchange httpExchange) {
		try {
			httpExchange.sendResponseHeaders(200, 0);
			httpExchange.getResponseBody().close();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 解析http请求的参数
	 * 
	 * @param uriQuery
	 * @return
	 */
	public static Map<String, String> parseHttpParam(HttpExchange httpExchange) {
		Map<String, String> paramMap = new HashMap<String, String>();
		try {
			String uriPath = httpExchange.getRequestURI().getPath();
			String uriQuery = httpExchange.getRequestURI().getQuery();
			if (uriQuery != null && uriQuery.length() > 0) {
				uriQuery = URLDecoder.decode(uriQuery, "UTF-8");
				HawkLog.logPrintln("UriQuery: " + uriPath + "?" + uriQuery);
				if (uriQuery != null) {
					String[] querys = uriQuery.split("&");
					for (String query : querys) {
						String[] pair = query.split("=");
						if (pair.length == 2) {
							paramMap.put(pair[0], pair[1]);
						}
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return paramMap;
	}
}
