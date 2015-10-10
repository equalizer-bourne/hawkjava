package com.hawk.collector.handler.report;

import java.io.IOException;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.collector.Collector;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 注册信息收集处理
 * 
 * @author hawk
 */
public class ReportServerInfoHandler implements HttpHandler {
	/**
	 * 格式: game=%s&platform=%s&server=%s&ip=%s&localip=%s&folder=%s&listen_port=%d&script_port=%d&dburl=%s&dbuser=%s&dbpwd=%s
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			Collector.checkToken(params.get("token"));
			String remoteIp = httpExchange.getRemoteAddress().getAddress().getHostAddress();
			doReport(params, remoteIp);
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			CollectorHttpServer.response(httpExchange, null);
		}
	}
	
	public static void doReport(Map<String, String> params, String remoteIp) throws Exception {
		if (params != null) {
			if (params.containsKey("ip") && ((String)params.get("ip")).length() > 0) {
				remoteIp = (String)params.get("ip");
			}
			
			String folder = "";
			if (params.containsKey("folder")) {
				folder = (String) params.get("folder");
				folder = folder.replace('#', '/');
			}
			
			String localIp = "127.0.0.1";
			if (params.containsKey("localip")) {
				localIp = (String) params.get("localip");
			}
			
			String value = String.format("'%s', '%s', '%s', '%s', '%s', '%s', %s, %s, '%s', '%s', '%s'", 
					params.get("game"), params.get("platform"), params.get("server"), 
					remoteIp, localIp, folder, params.get("listen_port"), params.get("script_port"), 
					params.get("dburl"), params.get("dbuser"), params.get("dbpwd"));
			
			HawkLog.logPrintln("report_server: " + value);
			
			// 优先更新
			String sql = String.format("UPDATE server SET "
					+ "ip = '%s', localip = '%s', folder = '%s', listen_port = %s, script_port = %s, dburl = '%s', dbuser = '%s', dbpwd = '%s' "
					+ "WHERE game = '%s' AND platform = '%s' AND server = '%s'", 
					remoteIp, localIp, folder, params.get("listen_port"), params.get("script_port"), 
					params.get("dburl"), params.get("dbuser"), params.get("dbpwd"),
					params.get("game"), params.get("platform"), params.get("server"));
			
			if (DBManager.getInstance().executeSql(params.get("game"), sql) > 0) {
				return;
			}
			
			// 更新失败即插入记录
			sql = String.format("INSERT INTO server(game, platform, server, ip, localip, folder, listen_port, script_port, dburl, dbuser, dbpwd) VALUES(%s);", value);
			DBManager.getInstance().executeSql(params.get("game"), sql);
		}
	}
}
