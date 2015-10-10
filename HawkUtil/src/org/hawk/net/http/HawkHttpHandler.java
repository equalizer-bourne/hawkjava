package org.hawk.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HawkHttpHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			String method = httpExchange.getRequestMethod();
			if ("GET".equals(method)) {
				doGet(httpExchange);
			} else if ("POST".equals(method)) {
				doPost(httpExchange);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			httpExchange.close();
		}
	}
	
	private void doGet(HttpExchange httpExchange) throws Exception {
		String uriQuery = httpExchange.getRequestURI().getQuery();
		if (uriQuery != null && uriQuery.length() > 0) {
			uriQuery = URLDecoder.decode(uriQuery, "UTF-8");
			onRecvProtocol(httpExchange, uriQuery);
		}
	}
	
	private void doPost(HttpExchange httpExchange) throws Exception {
		String content = "";
		InputStream inputStream = httpExchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));  
        String line = null;
        while((line = reader.readLine()) != null) {
        	if (content.length() > 0) {
        		content += "\n";
        	}
        	content += line;
        }
        onRecvProtocol(httpExchange, content);
	}
	
	private void onRecvProtocol(HttpExchange httpExchange, String params) throws Exception {
		// 调试打印
		HawkLog.debugPrintln("HttpParams: " + params);
		
		// 参数分析
		String token = "", data = "";
		String[] kvPairs = params.split("&");
		for (String kv : kvPairs) {
			String[] pair = kv.split("=");
			if (pair.length != 2) {
				continue;
			}
			
			if (pair[0].equals("token")) {
				token = pair[1];
			}
			
			if (pair[0].equals("data")) {
				data = pair[1];
			}
		}
		
		if (token != null && data != null && data.length() > 0) {
			HawkOSOperator.sendHttpResponse(httpExchange, HawkOSOperator.randomString(HawkRand.randInt(64, 2048)));
		}
	}
}
