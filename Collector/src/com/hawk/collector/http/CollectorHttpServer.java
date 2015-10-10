package com.hawk.collector.http;

import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.collector.Collector;
import com.hawk.collector.handler.report.ReportActivityHandler;
import com.hawk.collector.handler.report.ReportDataHandler;
import com.hawk.collector.handler.report.ReportGoldInfoHandler;
import com.hawk.collector.handler.report.ReportLoginHandler;
import com.hawk.collector.handler.report.ReportRechargeHandler;
import com.hawk.collector.handler.report.ReportRegisterHandler;
import com.hawk.collector.handler.report.ReportServerInfoHandler;
import com.hawk.collector.handler.report.ReportTutorialHandler;
import com.hawk.collector.handler.report.ReportUserLogHandler;
import com.hawk.collector.handler.service.DailyAnalyzeHandler;
import com.hawk.collector.handler.service.FetchActivityInfoHandler;
import com.hawk.collector.handler.service.FetchActivityStatisticsHandler;
import com.hawk.collector.handler.service.FetchBillsInfoHandler;
import com.hawk.collector.handler.service.FetchClientLogHandler;
import com.hawk.collector.handler.service.FetchDataInfoHandler;
import com.hawk.collector.handler.service.FetchGameInfoHandler;
import com.hawk.collector.handler.service.FetchGeneralInfoHandler;
import com.hawk.collector.handler.service.FetchGoldInfoHandler;
import com.hawk.collector.handler.service.FetchMyIpInfoHandler;
import com.hawk.collector.handler.service.FetchOperationInfoHandler;
import com.hawk.collector.handler.service.FetchOrderInfoHandler;
import com.hawk.collector.handler.service.FetchPuidRechargeHandler;
import com.hawk.collector.handler.service.FetchRechargeRankHandler;
import com.hawk.collector.handler.service.FetchServerInfoHandler;
import com.hawk.collector.handler.service.FetchStatisticsInfoHandler;
import com.hawk.collector.handler.service.FetchTutorialLevelHandler;
import com.hawk.collector.handler.service.FetchTutorialStepHandler;
import com.hawk.collector.handler.system.CreateGameRequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 收集的HTTP服务
 * 
 * @author hawk
 */
public class CollectorHttpServer {
	/**
	 * 服务器地址
	 */
	private String httpAddr = "0.0.0.0";
	/**
	 * 开启端口
	 */
	private int httpPort = 9001;
	/**
	 * http线程池阿晓
	 */
	private int poolSize = 4;
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
			httpAddr = addr;
			httpPort = port;
			poolSize = pool;
			if (httpAddr != null && httpAddr.length() > 0) {
				httpServer = HttpServer.create(new InetSocketAddress(httpAddr, httpPort), 0);
				httpServer.setExecutor(Executors.newFixedThreadPool(poolSize));
				
				installContext();

				httpServer.start();
				HawkLog.logPrintln("Reporter Http Server [" + httpAddr + ":" + httpPort + "] Start OK.");
				return true;
			}
		} catch (BindException e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("Reporter Http Server Bind Failed, Address: " + httpAddr + ":" + httpPort);
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
			httpServer.createContext("/report_gold", new ReportGoldInfoHandler());
			httpServer.createContext("/report_server", new ReportServerInfoHandler());
			httpServer.createContext("/report_data", new ReportDataHandler());
			httpServer.createContext("/report_tutorial", new ReportTutorialHandler());
			httpServer.createContext("/report_activity", new ReportActivityHandler());
			
			if (Collector.isUserLogEnable()) {
				httpServer.createContext("/report_userlog", new ReportUserLogHandler());
			}
			
			httpServer.createContext("/create_game", new CreateGameRequestHandler());
			httpServer.createContext("/daily_analyze", new DailyAnalyzeHandler());
			
			httpServer.createContext("/fetch_myip", new FetchMyIpInfoHandler());
			httpServer.createContext("/fetch_game", new FetchGameInfoHandler());
			httpServer.createContext("/fetch_server", new FetchServerInfoHandler());
			httpServer.createContext("/fetch_operation", new FetchOperationInfoHandler());
			httpServer.createContext("/fetch_statistics", new FetchStatisticsInfoHandler());
			httpServer.createContext("/fetch_general", new FetchGeneralInfoHandler());

			httpServer.createContext("/fetch_recharge", new FetchPuidRechargeHandler());
			httpServer.createContext("/fetch_order", new FetchOrderInfoHandler());
			httpServer.createContext("/fetch_bills", new FetchBillsInfoHandler());
			httpServer.createContext("/fetch_recharge_rank", new FetchRechargeRankHandler());
			
			httpServer.createContext("/fetch_gold", new FetchGoldInfoHandler());
			httpServer.createContext("/fetch_tutorial_level", new FetchTutorialLevelHandler());
			httpServer.createContext("/fetch_tutorial_step", new FetchTutorialStepHandler());
			httpServer.createContext("/fetch_activity", new FetchActivityInfoHandler());
			httpServer.createContext("/fetch_activity_statistics", new FetchActivityStatisticsHandler());
			
			httpServer.createContext("/fetch_client_log", new FetchClientLogHandler());
			
			httpServer.createContext("/fetch_data", new FetchDataInfoHandler());
			
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
	 * 重启http服务器
	 */
	public void restart() {
		stop();
		setup(httpAddr, httpPort, poolSize);
	}
	
	/**
	 * http请求回应内容
	 * 
	 * @param httpExchange
	 * @param response
	 */
	public static void response(HttpExchange httpExchange, String response) {
		try {
			OutputStream responseBody = httpExchange.getResponseBody();
			if (response != null && response.length() > 0) {
				byte[] bytes = response.getBytes("UTF-8");
				httpExchange.sendResponseHeaders(200, bytes.length);
				responseBody.write(bytes);
			} else {
				httpExchange.sendResponseHeaders(200, 0);
			}
			responseBody.close();
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
