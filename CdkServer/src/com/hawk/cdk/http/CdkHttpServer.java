package com.hawk.cdk.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import net.sf.json.JSONObject;
import sun.security.krb5.internal.Ticket;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.hawk.cdk.CdkServices;
import com.hawk.cdk.http.handler.AppendCdkHandler;
import com.hawk.cdk.http.handler.DelCdkHandler;
import com.hawk.cdk.http.handler.DelTypeHandler;
import com.hawk.cdk.http.handler.GenCdkHandler;
import com.hawk.cdk.http.handler.QueryCdkHandler;
import com.hawk.cdk.http.handler.QueryTypeHandler;
import com.hawk.cdk.http.handler.ResetRewardHandler;
import com.hawk.cdk.http.handler.UseCdkHandler;
import com.hawk.cdk.util.CdkUtil;

/**
 * 
 * @author hawk
 */
@SuppressWarnings("unused")
public class CdkHttpServer {
	/**
	 * cdk服务参数串格式
	 */
	private static final String del_cdk 	 = "/del_cdk?cdk=%s"; // 多个可用逗号隔开
	private static final String del_type 	 = "/del_type?game=%s&type=%s";
	private static final String gen_cdk 	 = "/gen_cdk?game=%s&platform=%s&type=%s&count=%s&reward=%s&starttime=%s&endtime=%s";
	private static final String append_cdk 	 = "/append_cdk?game=%s&platform=%s&type=%s&count=%s";
	private static final String query_cdk 	 = "/query_cdk?cdk=%s";
	private static final String query_type 	 = "/query_type?game=%s&type=%s";
	private static final String reset_reward = "/reset_reward?game=%s&platform=%s&type=%s&reward=%s&starttime=%s&endtime=%s";
	private static final String use_cdk 	 = "/use_cdk?game=%s&platform=%s&server=%s&playerid=%s&puid=%s&playername=%s&cdk=%s";

	/**
	 * 服务器对象
	 */
	private HttpServer httpServer = null;

	/**
	 * 开启服务
	 */
	public void setup(String addr, int port, int pool) {
		try {
			if (addr != null && addr.length() > 0) {
				httpServer = HttpServer.create(new InetSocketAddress(addr, port), 0);				
				// TODO: 暂时不支持多线程(若以后性能不足, 及时修改)
				httpServer.setExecutor(Executors.newFixedThreadPool(1));

				httpServer.createContext("/gen_cdk", new GenCdkHandler());
				httpServer.createContext("/append_cdk", new AppendCdkHandler());
				httpServer.createContext("/query_type", new QueryTypeHandler());
				httpServer.createContext("/query_cdk", new QueryCdkHandler());
				httpServer.createContext("/reset_reward", new ResetRewardHandler());
				httpServer.createContext("/del_type", new DelTypeHandler());
				httpServer.createContext("/del_cdk", new DelCdkHandler());
				httpServer.createContext("/use_cdk", new UseCdkHandler());

				httpServer.start();
				HawkLog.logPrintln("Cdk Http Server [" + addr + ":" + port + "] Start OK.");
			}
		} catch (BindException e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("Cdk Http Server Bind Failed, Address: " + addr + ":" + port);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
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
	 * @param jsonObject
	 */
	public static void response(HttpExchange httpExchange, JSONObject jsonObject) {
		response(httpExchange, jsonObject.toString());
	}

	/**
	 * http请求回应内容
	 * 
	 * @param httpExchange
	 * @param response
	 */
	public static void response(HttpExchange httpExchange, String response) {
		try {
			if (response != null && response.length() > 0) {
				final byte[] bytes = response.getBytes("UTF-8");
				httpExchange.sendResponseHeaders(200, bytes.length);
				httpExchange.getResponseBody().write(bytes);
				httpExchange.getResponseBody().close();
			}
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

	/**
	 * 主循环函数
	 */
	public void run() {
		while (true) {
			try {
				if (!CdkServices.getInstance().tick()) {
					HawkLog.logPrintln("Cdk Service Exit.");
					System.exit(0);
				}

				Thread.sleep(50);

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
