package org.hawk.net;

import java.io.IOException;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HawkHttpHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> httpParams = HawkOSOperator.parseHttpParam(httpExchange);
			if (httpParams.containsKey("token")) {
				
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
