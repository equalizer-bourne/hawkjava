package com.hawk.collector.analyser;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.info.GamePlatform;

public class ServerAnalyser {
	/**
	 * 获取游戏对应的平台和渠道信息
	 * @return
	 */
	public static Map<String, GamePlatform> fetchGamePlatforms() {
		Statement statement = null;
		try {
			String sql = "select game, platform, channel, logUserName, logUserPwd, logPath, sshPort from game";
			statement = DBManager.getInstance().createStatement("oods");
			ResultSet resultSet = statement.executeQuery(sql);
			Map<String, GamePlatform> gamePlatforms = new ConcurrentHashMap<String, GamePlatform>();
			while (resultSet.next()) {
				GamePlatform gamePlatform = new GamePlatform(
						resultSet.getString(1), 
						resultSet.getString(2), 
						resultSet.getString(3),
						resultSet.getString(4),
						resultSet.getString(5),
						resultSet.getString(6),
						resultSet.getString(7));
				gamePlatforms.put(gamePlatform.getGame(), gamePlatform);
			}
			return gamePlatforms;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/**
	 * 获取指定游戏的特定平台服务器列表信息
	 * 
	 * @param game
	 * @param platform
	 * @return
	 */
	public static String fetchServerInfos(String game, String platform) {
		if (game == null || game.length() <= 0) {
			return "404 Not Found";
		}
		
		JsonArray jsonArray = new JsonArray();
		Statement statement = null;
		try {
			String sql = "select game, platform, server, ip, localip, folder, listen_port, script_port, dburl, dbuser, dbpwd from server where game = '" + game + "'";
			if (platform != null && platform.length() > 0) {
				sql += " and platform = '" + platform +"'";
			}
			
			statement = DBManager.getInstance().createStatement(game);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				int column = 0;
				JsonObject jsonObject = new JsonObject();
				
				jsonObject.addProperty("game", resultSet.getString(++column));
				jsonObject.addProperty("platform", resultSet.getString(++column));
				jsonObject.addProperty("server", resultSet.getString(++column));
				jsonObject.addProperty("ip", resultSet.getString(++column));
				jsonObject.addProperty("localip", resultSet.getString(++column));
				jsonObject.addProperty("folder", resultSet.getString(++column));
				jsonObject.addProperty("listen_port", resultSet.getString(++column));
				jsonObject.addProperty("script_port", resultSet.getString(++column));
				jsonObject.addProperty("dburl", resultSet.getString(++column));
				jsonObject.addProperty("dbuser", resultSet.getString(++column));
				jsonObject.addProperty("dbpwd", resultSet.getString(++column));
				
				jsonArray.add(jsonObject);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
			}
		}
		return jsonArray.toString();
	}
}
