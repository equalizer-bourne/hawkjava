package com.hawk.collector.handler.report;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 用户日志收集game^logtype^platform^serverid^puid^platforminfo^deviceinfo
 * 
 * @author hawk
 */
public class ReportUserLogHandler implements HttpHandler {
	static Map<String, String> logMd5Map = new ConcurrentHashMap<String, String>();
	
	@Override
	@SuppressWarnings("unused")
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			String contentBody = HawkOSOperator.readRequestBody(httpExchange).replace("\r", "");
			
			int pos = contentBody.indexOf("\n");
			if (pos > 0) {
				int index = 0;
				String header = contentBody.substring(0, pos).trim();
				String items[] = header.split("\\^");
				if (items.length >= 7) {
					String md5 = "";
					int stackPos = contentBody.indexOf("stack traceback");
					if (stackPos > 0) {
						md5 = HawkMd5.makeMD5(contentBody.substring(stackPos));
					} else {
						md5 = HawkMd5.makeMD5(contentBody);
					}
					if (logMd5Map.containsKey(md5)) {
						return;
					}
					logMd5Map.put(md5, md5);
					HawkLog.logPrintln("\r\nUserLog: " + contentBody);
					
					String game = items[index++];
					String logtype = items[index++];
					String platform = items[index++];
					String serverid = items[index++];
					String puid = items[index++];
					String phoneinfo = items[index++];
					String sysinfo = items[index++];
					if (game.length() > 0 && logtype.length() > 0 && platform.length() > 0 && serverid.length() > 0 && puid.length() > 0) {
						// 创建日志目录
						String dir = HawkOSOperator.getWorkPath() + "userlog/" + game + "/" + logtype + "/";
						HawkOSOperator.createDir(dir);
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
						String tm = sdf.format(HawkTime.getDate());
						String fileName = String.format("%s%s.%s.%s.%s", dir, platform, serverid, puid, tm);
						HawkOSOperator.saveAsFile(contentBody, fileName);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
