package com.hawk.collector.handler.system;

import java.io.IOException;
import java.util.Map;

import org.hawk.os.HawkException;

import com.google.gson.JsonObject;
import com.hawk.collector.Collector;
import com.hawk.collector.CollectorServices;
import com.hawk.collector.http.CollectorHttpServer;
import com.hawk.collector.info.GamePlatform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 注册信息收集处理
 * 
 * @author hawk
 */
public class CreateGameRequestHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		try {
			Map<String, String> params = CollectorHttpServer.parseHttpParam(httpExchange);
			Collector.checkToken(params.get("token"));
			
			String response = createGame(params);
			CollectorHttpServer.response(httpExchange, response);
		} catch (Exception e) {
			HawkException.catchException(e);
			CollectorHttpServer.response(httpExchange, null);
		}
	}
	
	public static String createGame(Map<String, String> params) throws Exception {
		if (!params.containsKey("game")) {
			return "";
		}
		
		String platform = params.containsKey("platform")? params.get("platform") : "";
		String channel = params.containsKey("channel")? params.get("channel") : "";
		GamePlatform gamePlatform = CollectorServices.getInstance().getGamePlatform(params.get("game"));
		String logUserName = params.containsKey("logUserName")? params.get("logUserName") : "";
		String logUserPwd = params.containsKey("logUserPwd")? params.get("logUserPwd") : "";
		String logPath = params.containsKey("logPath")? params.get("logPath") : "";
		String sshPort = params.containsKey("sshPort")? params.get("sshPort") : "";
		
		int status = 0;
		if (gamePlatform != null) {
			if (!CollectorServices.getInstance().updateGamePlatform(new GamePlatform(params.get("game"), platform, channel, logUserName, logUserPwd, logPath, sshPort))) {
				status = -1;
			}
		} else {
			if (!CollectorServices.getInstance().createGamePlatform(new GamePlatform(params.get("game"), platform, channel, logUserName, logUserPwd, logPath, sshPort))) {
				status = -1;
			}
		}
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("game", params.get("game"));
		jsonObject.addProperty("platform", platform);
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("logUserName", logUserName);
		jsonObject.addProperty("logUserPwd", logUserPwd);
		jsonObject.addProperty("logPath", logPath);
		jsonObject.addProperty("sshPort", sshPort);
		jsonObject.addProperty("status", status);
		return jsonObject.toString();
	}
}
