package com.hawk.collector;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.HawkTickable;

import com.hawk.collector.analyser.OperationAnalyser;
import com.hawk.collector.analyser.OperationData;
import com.hawk.collector.db.DBManager;
import com.hawk.collector.info.GamePlatform;

public class AnalyserManager extends HawkTickable {
	/**
	 * 计算周期(次日, 三日, 7日, 14日, 30日)
	 */
	private static Integer[] ANALYZE_DAY = {2, 3, 7, 14, 30};
	
	/**
	 * 上次tick检测时间
	 */
	private int lastCheckSeconds = 0;
	private int lastCheckYearDay = 0;
	
	/**
	 * 逻辑线程池
	 */
	private HawkThreadPool threadPool;
	
	/**
	 * 全局实例对象
	 */
	private static AnalyserManager instance;
	public static AnalyserManager getInstance() {
		if (instance == null) {
			instance = new AnalyserManager();
		}
		return instance;
	}
	
	private AnalyserManager() {
		lastCheckSeconds = HawkTime.getSeconds();
		lastCheckYearDay = HawkTime.getYearDay();
		CollectorServices.getInstance().addTickable(this);
	}
	
	public boolean init(int pool) {
		if (threadPool == null) {
			threadPool = new HawkThreadPool(getName());
			threadPool.initPool(pool);
			threadPool.start();
		}
		return true;
	}
	
	public void stop() {
		if (threadPool != null) {
			threadPool.close(true);
			threadPool = null;
		}
	}
	
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public boolean getStatisticsData(OperationData operationData) {
		Statement statement = null;
		try {
			String sql = String.format("SELECT statistics FROM statistics WHERE date = '%s' AND game = '%s' AND platform = '%s' AND channel = '%s'",
					operationData.date, operationData.game, operationData.platform, operationData.channel);
			
			statement = DBManager.getInstance().createStatement(operationData.game);
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				String statistics = resultSet.getString(1);
				if (statistics != null && statistics.length() > 0) {
					operationData.fromJsonString(statistics);
					return true;
				}
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
		return false;
	}
	
	public synchronized boolean createOrUpdateStatisticsData(OperationData operationData) {
		Statement statement = null;
		try {
			HawkLog.logPrintln(operationData.toJsonString(false));

			String sql = String.format("INSERT INTO statistics(date, game, platform, channel, statistics) VALUES('%s', '%s', '%s', '%s', '%s') ON DUPLICATE KEY UPDATE statistics = '%s'",
					operationData.date, operationData.game, operationData.platform, operationData.channel, operationData.toJsonString(false), operationData.toJsonString(false));
			
			statement = DBManager.getInstance().createStatement(operationData.game);
			statement.execute(sql);
			return true;
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
		return false;
	}
	
	@Override
	public void onTick() {
		// 每5分钟检测一次
		if (HawkTime.getSeconds() - lastCheckSeconds > 300) {
			lastCheckSeconds = HawkTime.getSeconds();
			// 跨天计算
			if (HawkTime.getYearDay() != lastCheckYearDay) {
				Map<String, GamePlatform> gamePlatforms = new HashMap<String, GamePlatform>();
				gamePlatforms.putAll(CollectorServices.getInstance().getGamePlatforms());
				// 计算昨日数据
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar calendar = HawkTime.getCalendar();
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				String date = sdf.format(calendar.getTime());
				doDailyAnalyze(date, gamePlatforms);
				// 设置上次分析日期
				lastCheckYearDay = HawkTime.getYearDay();
			}
		}
	}

	/**
	 * 每日的分析任务
	 * 
	 * @param date
	 * @param gamePlatforms
	 */
	public void doDailyAnalyze(String date, Map<String, GamePlatform> gamePlatforms) {
		// 分析当天
		analyzeToday(date, gamePlatforms);
		
		// 分析之前的历史数据 (ltv & 留存)
		for (int i=0; i<ANALYZE_DAY.length; i++) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				sdf.parse(date);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			Calendar calendar = (Calendar) sdf.getCalendar().clone();
			calendar.add(Calendar.DAY_OF_YEAR, 1-ANALYZE_DAY[i]);
			String historyDate = sdf.format(calendar.getTime());
			// 分析历史数据
			analyzeHistory(historyDate, ANALYZE_DAY[i], gamePlatforms);
		}
	}
	
	/**
	 * 分析当天数据
	 * 
	 * @param date
	 * @param gamePlatforms
	 */
	public void analyzeToday(String date, Map<String, GamePlatform> gamePlatforms) {
		for (Entry<String, GamePlatform> entry : gamePlatforms.entrySet()) {
			GamePlatform gamePlatform = entry.getValue();
			OperationData operationData = new OperationData(gamePlatform.getGame(), date);
			analyzeToday(operationData);
			
			// 平台分析
			String[] platformArray = gamePlatform.getPlatform().split(",");
			for (String platform : platformArray) {
				operationData = new OperationData(gamePlatform.getGame(), date);
				operationData.platform = platform;
				operationData.date = date;
				analyzeToday(operationData);
			}

			// 渠道分析
			String[] channelArray = gamePlatform.getChannel().split(",");
			for (String channel : channelArray) {
				operationData = new OperationData(gamePlatform.getGame(), date);
				operationData.channel = channel;
				operationData.date = date;
				analyzeToday(operationData);
			}
		}
	}
	
	/**
	 * 分析当天数据
	 * 
	 * @param date
	 * @param gamePlatforms
	 */
	public void analyzeToday(final OperationData operationData) {
		// 已线程任务模式进行数据分析
		threadPool.addTask(new HawkTask(true) {
			@Override
			protected int run() {
				// 计算数据
				OperationAnalyser.calcDailyData(
						operationData.game, 
						operationData.platform, 
						"",
						operationData.channel, 
						operationData.date, 
						operationData);
				
				// 存储or更新
				createOrUpdateStatisticsData(operationData);
				return 0;
			}
		});
	}
	
	/**
	 * 分析历史数据
	 * 
	 * @param date
	 * @param analyzedDay
	 * @param gamePlatforms
	 */
	public void analyzeHistory(String date, int analyzedDay, Map<String, GamePlatform> gamePlatforms) {
		for (Entry<String, GamePlatform> entry : gamePlatforms.entrySet()) {
			GamePlatform gamePlatform = entry.getValue();
			OperationData operationData = new OperationData(gamePlatform.getGame(), date);
			analyzeHistory(date, analyzedDay, operationData);
			
			// 平台分析
			String[] platformArray = gamePlatform.getPlatform().split(",");
			for (String platform : platformArray) {
				operationData = new OperationData(gamePlatform.getGame(), date);
				operationData.platform = platform;
				analyzeHistory(date, analyzedDay, operationData);
			}

			// 渠道分析
			String[] channelArray = gamePlatform.getChannel().split(",");
			for (String channel : channelArray) {
				operationData = new OperationData(gamePlatform.getGame(), date);
				operationData.channel = channel;
				analyzeHistory(date, analyzedDay, operationData);
			}
		}
	}

	private void analyzeHistory(final String date, final int analyzedDay, final OperationData operationData) {
		// 线程池处理
		threadPool.addTask(new HawkTask(true) {
			@Override
			protected int run() {
				// 先取历史存档数据
				if (!getStatisticsData(operationData)) {
					return 0;
				}
				
				Statement statement = null;
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						sdf.parse(date);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					Calendar calendar = (Calendar) sdf.getCalendar().clone();
					
					statement = DBManager.getInstance().createStatement(operationData.game);
					float ltv = OperationAnalyser.calcLTV(
							statement, 
							operationData.game, 
							operationData.platform, 
							operationData.channel, 
							calendar, 
							operationData.newUser, 
							analyzedDay);
					
					float userRetention = OperationAnalyser.calcUserRetention(
							statement, 
							operationData.game, 
							operationData.platform, 
							operationData.channel, 
							calendar, 
							operationData.newUser, 
							analyzedDay);
					
					float deviceRetention = OperationAnalyser.calcDeviceRetention(
							statement, 
							operationData.game, 
							operationData.platform, 
							operationData.channel, 
							calendar, 
							operationData.newDevice, 
							analyzedDay);
					
					if (analyzedDay == ANALYZE_DAY[0]) {
						operationData.LTV2 = ltv;
						operationData.userRetention2 = userRetention;
						operationData.deviceRetention2 = deviceRetention;
					}
					
					if (analyzedDay == ANALYZE_DAY[1]) {
						operationData.LTV3 = ltv;
						operationData.userRetention3 = userRetention;
						operationData.deviceRetention3 = deviceRetention;
					}
					
					if (analyzedDay == ANALYZE_DAY[2]) {
						operationData.LTV7 = ltv;
						operationData.userRetention7 = userRetention;
						operationData.deviceRetention7 = deviceRetention;
					}
					
					if (analyzedDay == ANALYZE_DAY[3]) {
						operationData.LTV14 = ltv;
						operationData.userRetention14 = userRetention;
						operationData.deviceRetention14 = deviceRetention;
					}
					
					if (analyzedDay == ANALYZE_DAY[4]) {
						operationData.LTV30 = ltv;
						operationData.userRetention30 = userRetention;
						operationData.deviceRetention30 = deviceRetention;
					}
					
					// 存储计算数据
					createOrUpdateStatisticsData(operationData);
					
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
				return 0;
			}
		});
	}
}
