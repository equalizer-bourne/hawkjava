package com.hawk.collector;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.XMLConfiguration;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkTickable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hawk.collector.analyser.ServerAnalyser;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.http.CollectorHttpServer;
import com.hawk.collector.info.GamePlatform;
import com.hawk.collector.zmq.CollectorZmqServer;

/**
 * 收集服务
 * 
 * @author hawk
 */
public class CollectorServices {
	/**
	 * 服务器是否允许中
	 */
	volatile boolean running;
	/**
	 * 可更新列表
	 */
	Set<HawkTickable> tickableSet;
	/**
	 * 当前游戏平台列表
	 */
	Map<String, GamePlatform> gamePlatforms;
	/**
	 * 单例对象
	 */
	static CollectorServices instance;

	/**
	 * 获取实例
	 */
	public static CollectorServices getInstance() {
		if (instance == null) {
			instance = new CollectorServices();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private CollectorServices() {
		// 初始化变量
		running = true;
		tickableSet = new HashSet<HawkTickable>();
		gamePlatforms = new ConcurrentHashMap<String, GamePlatform>();
	}

	/**
	 * 是否运行中
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 退出服务主循环
	 */
	public void breakLoop() {
		running = false;
	}

	/**
	 * 定时更新
	 */
	private void onTick() {
		for (HawkTickable tick : tickableSet) {
			try {
				tick.onTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 添加可更新对象列表
	 * 
	 * @param tickable
	 */
	public void addTickable(HawkTickable tickable) {
		tickableSet.add(tickable);
	}

	/**
	 * 移除可更新对象
	 * 
	 * @param tickable
	 */
	public void removeTickable(HawkTickable tickable) {
		tickableSet.remove(tickable);
	}

	/**
	 * 使用配置文件初始化
	 * 
	 * @param cfgFile
	 * @return
	 */
	public boolean init(String cfgFile) {
		// 添加库加载目录		
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/hawk");
		new File(".").list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (dir.isDirectory() && name.endsWith("lib")) {
					HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/" + name);
					return true;
				}
				return false;
			}
		});
		
		try {
			// 初始化
			System.loadLibrary("hawk");
			if (!HawkNativeApi.initHawk()) {
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		boolean initOK = true;
		try {
			// 加载配置文件
			XMLConfiguration conf = new XMLConfiguration(cfgFile);

			// 线程池大小
			int threadPool = 4;
			if (conf.containsKey("database.threads")) {
				threadPool = conf.getInt("database.threads");
			}
			
			// 设置校验码
			if (conf.containsKey("httpserver.token")) {
				Collector.setToken(conf.getString("httpserver.token"));
			}
			
			if (conf.containsKey("httpserver.userlog")) {
				Collector.setUserLogEnable(conf.getBoolean("httpserver.userlog"));
			}
			
			// 初始化数据库
			initOK &= DBManager.getInstance().init(conf.getString("database.dbHost"), conf.getString("database.userName"), conf.getString("database.passWord"), threadPool);
			if (initOK) {
				HawkLog.logPrintln("Init DBManager Success, dbHost: " + conf.getString("database.dbHost"));
			} else {
				HawkLog.logPrintln("Init DBManager Failed, dbHost: " + conf.getString("database.dbHost"));
			}
			
			// 创建db会话
			initOK &= createDbSessions();
			if (initOK) {
				HawkLog.logPrintln("Create Database Session Success");
			} else {
				HawkLog.logPrintln("Create Database Session Failed");
			}
			
			// 初始化http服务器
			initOK &= CollectorHttpServer.getInstance().setup(conf.getString("httpserver.addr"), conf.getInt("httpserver.port"), conf.getInt("httpserver.pool"));
			if (initOK) {
				HawkLog.logPrintln("Setup HttpServer Success, " + conf.getString("httpserver.addr") + ":" + conf.getInt("httpserver.port"));
			} else {
				HawkLog.logPrintln("Setup HttpServer Failed, " + conf.getString("httpserver.addr") + ":" + conf.getInt("httpserver.port"));
			}

			// 初始化zmq服务器
			if (conf.containsKey("zmqserver.addr")) {
				initOK &= CollectorZmqServer.getInstance().setup(conf.getString("zmqserver.addr"), conf.getInt("zmqserver.pool"));
				if (initOK) {
					HawkLog.logPrintln("Setup ZmqServer Success: " + conf.getString("zmqserver.addr"));
				} else {
					HawkLog.logPrintln("Setup ZmqServer Failed: " + conf.getString("zmqserver.addr"));
				}
			}
			
			// 开启分析器
			if (initOK) {
				initOK &= AnalyserManager.getInstance().init(threadPool);
			}
				
		} catch (Exception e) {
			HawkException.catchException(e);
			initOK = false;
		}
		
		return initOK;
	}

	private boolean createDbSessions() {
		// 创建默认会话
		if (DBManager.getInstance().createDbSession("oods") == null) {
			HawkLog.logPrintln("Create DbSession Failed, Database: oods");
			return false;
		}
		
		Map<String, GamePlatform> gps = ServerAnalyser.fetchGamePlatforms();
		if (gps == null) {
			HawkLog.logPrintln("Fetch Game Platforms Failed.");
			return false;
		} else {
			this.gamePlatforms.putAll(gps);
		}
		
		for (Entry<String, GamePlatform> entry : gamePlatforms.entrySet()) {
			GamePlatform gp= entry.getValue();
			
			if (DBManager.getInstance().createDbSession(gp.getGame()) == null) {
				return false;
			}
		}
		
		HawkLog.logPrintln("Load Game Platforms Finish.");
		return true;
	}
	
	/**
	 * 获取所有游戏平台和渠道信息
	 * 
	 * @return
	 */
	public Map<String, GamePlatform> getGamePlatforms() {
		return gamePlatforms;
	}

	/**
	 * 获取指定游戏的平台信息
	 * 
	 * @param game
	 * @return
	 */
	public GamePlatform getGamePlatform(String game) {
		return gamePlatforms.get(game);
	}
	
	/**
	 * 创建新游戏平台
	 * 
	 * @param gamePlatform
	 * @return
	 */
	public boolean createGamePlatform(GamePlatform gamePlatform) {
		String sql = String.format("INSERT INTO game(game, platform, channel, logUserName, logUserPwd, logPath, sshPort, time) VALUES('%s', '%s', '%s', '%s', '%s', '%s', %s, '%s');", 
				gamePlatform.getGame(), gamePlatform.getPlatform(), gamePlatform.getChannel(), 
				gamePlatform.getLogUserName(), gamePlatform.getLogUserPwd(), gamePlatform.getLogPath(), gamePlatform.getSshPort(), HawkTime.getTimeString());

		HawkLog.logPrintln(String.format("create_game, game: %s, platform: %s, channel: %s, logUserName: %s, logUserPwd: %s, logPath: %s, sshPort: %s", 
				gamePlatform.getGame(), gamePlatform.getPlatform(), gamePlatform.getChannel(), 
				gamePlatform.getLogUserName(), gamePlatform.getLogUserPwd(), gamePlatform.getLogPath(), gamePlatform.getSshPort()));
		
		if (DBManager.getInstance().executeSql("oods", sql) > 0) {
			if (!DBManager.getInstance().createCollectorDB(gamePlatform.getGame())) {
				DBManager.getInstance().executeSql("oods", "DELETE FROM oods WHERE game = '" + gamePlatform.getGame() + "'");
				return false;
			}
			
			gamePlatforms.put(gamePlatform.getGame(), gamePlatform);
		}
		return true;
	}
	
	/**
	 * 更新游戏平台信息
	 * 
	 * @param gamePlatform
	 * @return
	 */
	public boolean updateGamePlatform(GamePlatform gamePlatform) {
		String sql = String.format("UPDATE game SET platform = '%s', channel = '%s', logUserName = '%s', logUserPwd = '%s', logPath = '%s', sshPort = %s, time = '%s' WHERE game = '%s'", 
				gamePlatform.getPlatform(), gamePlatform.getChannel(), 
				gamePlatform.getLogUserName(), gamePlatform.getLogUserPwd(), gamePlatform.getLogPath(),gamePlatform.getSshPort(),
				HawkTime.getTimeString(), gamePlatform.getGame());

		HawkLog.logPrintln(String.format("update_game, game: %s, platform: %s, channel: %s, logUserName: %s, logUserPwd: %s, logPath: %s, sshPort: %s", 
				gamePlatform.getGame(), gamePlatform.getPlatform(), gamePlatform.getChannel(),
				gamePlatform.getLogUserName(), gamePlatform.getLogUserPwd(), gamePlatform.getLogPath(), gamePlatform.getSshPort()));
		
		if (DBManager.getInstance().executeSql("oods", sql) > 0) {
			gamePlatforms.put(gamePlatform.getGame(), gamePlatform);
		}
		return true;
	}
	
	/**
	 * 获取游戏信息
	 * 
	 * @return
	 */
	public String formatGameInfo() {
		JsonArray jsonArray = new JsonArray();
		for (Entry<String, GamePlatform> entry : gamePlatforms.entrySet()) {
			GamePlatform gamePlatform = entry.getValue();
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("game", gamePlatform.getGame());
			jsonObject.addProperty("platform", gamePlatform.getPlatform());
			jsonObject.addProperty("channel", gamePlatform.getChannel());
			jsonObject.addProperty("logUserName", gamePlatform.getLogUserName());
			jsonObject.addProperty("logUserPwd", gamePlatform.getLogUserPwd());
			jsonObject.addProperty("logPath", gamePlatform.getLogPath());
			jsonObject.addProperty("sshPort", gamePlatform.getSshPort());
			jsonArray.add(jsonObject);
		}
		return jsonArray.toString();
	}
	
	/**
	 * 运行服务器
	 */
	public void run() {
		HawkLog.logPrintln("MainLoop Running OK.");
		while (running) {
			try {
				onTick();

				HawkOSOperator.sleep();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		CollectorHttpServer.getInstance().stop();
		CollectorZmqServer.getInstance().stop();
		AnalyserManager.getInstance().stop();
	}
	
	public static String getChannelFromPuid(String puid) {
		int pos = puid.indexOf("_");
		if (pos > 0) {
			return puid.substring(0, pos).toLowerCase();
		}
		return "";
	}
}
