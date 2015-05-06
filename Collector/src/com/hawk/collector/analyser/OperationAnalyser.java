package com.hawk.collector.analyser;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.collector.AnalyserManager;
import com.hawk.collector.db.DBManager;

public class OperationAnalyser {
	/**
	 * 计算数据类型(基础数据, 留存, LTV)
	 */
	public static final int DATE_TYPE_BASE = 0x00;
	public static final int DATE_TYPE_RETENTION = 0x01;
	public static final int DATE_TYPE_LTV = 0x02;
	
	/**
	 * 获取历史分析数据
	 * 
	 * @param game
	 * @param platform
	 * @param channel
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static List<String> fetchStatisticsInfo(String game, String platform, String channel, String beginDate, String endDate) {
		List<String> statisticsInfo = new LinkedList<String>();
		if (game != null && game.length() > 0) {
			Statement statement = null;
			try {
				String statisticsSql = "select statistics from statistics where game = '" + game + "'";
				statisticsSql += String.format(" and date >= '%s' and date <= '%s'", beginDate, endDate);
				if (channel != null && channel.length() > 0) {
					statisticsSql += " and channel = '" + channel +"'";
				} else if (platform != null && platform.length() > 0) {
					statisticsSql += " and platform = '" + platform +"'";
				} else {
					statisticsSql += " and platform = '' and channel = ''";
				}				
				
				statement = DBManager.getInstance().createStatement(game);
				ResultSet resultSet = statement.executeQuery(statisticsSql);
				while (resultSet.next()) {
					statisticsInfo.add(resultSet.getString(1));
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
		}
		return statisticsInfo;
	}
	
	/**
	 * 获取历史分析数据
	 * 
	 * @param game
	 * @param platform
	 * @param channel
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static OperationData fetchServerChannelInfo(String game, String platform, String server, String channel, String date) {
		OperationData operationData = new OperationData(game);
		if (game != null && game.length() > 0 && server != null && server.length() > 0) {
			// 参数校正
			if (channel == null) {
				channel = "";
			}
			if (platform == null) {
				platform = "";
			}
						
			// 参数校正
			if (date == null) {
				date = HawkTime.getDateString();
			}
			
			operationData.platform = platform;
			operationData.channel = channel;
			operationData.date = date;
			calcDailyData(game, platform, server, channel, date, operationData);
		}
		return operationData;
	}
	
	/**
	 * 获取实时运营数据
	 * 
	 * @param game
	 * @param platform
	 * @param channel
	 * @param date
	 * @param saveStatistics
	 * @return
	 */
	public static OperationData fetchOperationInfos(String game, String platform, String channel, String date, int dataType, boolean saveStatistics) {
		OperationData operationData = new OperationData(game);
		if (game != null && game.length() > 0) {
			// 参数校正
			if (platform == null) {
				platform = "";
			}
			
			// 参数校正
			if (channel == null) {
				channel = "";
			}
			
			// 参数校正
			if (date == null) {
				date = HawkTime.getDateString();
			}
			
			operationData.platform = platform;
			operationData.channel = channel;
			operationData.date = date;
			boolean isExist = AnalyserManager.getInstance().getStatisticsData(operationData);
			
			// 计算当日数据
			if (saveStatistics || !isExist || date.equals(HawkTime.getDateString())) {
				calcDailyData(game, platform, "", channel, date, operationData);
			}

			// 计算留存数据
			if ((dataType & DATE_TYPE_RETENTION) > 0) {
				calcRetentionData(game, platform, channel, date, operationData, true);
			}
			
			// 计算ltv数据
			if ((dataType & DATE_TYPE_LTV) > 0) {
				calcLtvData(game, platform, channel, date, operationData, true);
			}
			
			// 存储数据
			if (saveStatistics) {
				AnalyserManager.getInstance().createOrUpdateStatisticsData(operationData);
			}
		}
		return operationData;
	}
	
	public static boolean calcDailyData(String game, String platform, String server, String channel, String date, OperationData operationData) {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = DBManager.getInstance().createStatement(game);
			
			// 总用户
			{
				String totalUserSql = "select count(puid) from puid where date <= '" + date + "'";
				if (platform != null && platform.length() > 0) {
					totalUserSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					totalUserSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					totalUserSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(totalUserSql);
				resultSet = statement.executeQuery(totalUserSql);
				if (resultSet.next()) {
					operationData.totalUser = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 新增用户
			{
				String newUserSql = "select count(puid) from puid where date = '" + date + "'";
				if (platform != null && platform.length() > 0) {
					newUserSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					newUserSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					newUserSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(newUserSql);
				resultSet = statement.executeQuery(newUserSql);
				if (resultSet.next()) {
					operationData.newUser = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 总设备
			{
				String totalDeviceSql = "select count(device) from device where date <= '" + date + "'";
				if (platform != null && platform.length() > 0) {
					totalDeviceSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					totalDeviceSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					totalDeviceSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(totalDeviceSql);
				resultSet = statement.executeQuery(totalDeviceSql);
				if (resultSet.next()) {
					operationData.totalDevice = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 新增设备
			{
				String newDeviceSql = "select count(device) from device where date = '" + date + "'";
				if (platform != null && platform.length() > 0) {
					newDeviceSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					newDeviceSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					newDeviceSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(newDeviceSql);
				resultSet = statement.executeQuery(newDeviceSql);
				if (resultSet.next()) {
					operationData.newDevice = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// DAU
			{
				String userLoginSql = "select count(distinct puid) from login where game = '" + game + "'";
				if (platform != null && platform.length() > 0) {
					userLoginSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					userLoginSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					userLoginSql += " and channel = '" + channel +"'";
				}
				userLoginSql += String.format(" and date = '%s'", date);
				HawkLog.logPrintln(userLoginSql);
				resultSet = statement.executeQuery(userLoginSql);
				if (resultSet.next()) {
					operationData.userLogin = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// DAU(device)
			{
				String deviceLoginSql = "select count(distinct device) from login where game = '" + game + "'";
				if (platform != null && platform.length() > 0) {
					deviceLoginSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					deviceLoginSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					deviceLoginSql += " and channel = '" + channel +"'";
				}
				deviceLoginSql += String.format(" and date = '%s'", date);
				HawkLog.logPrintln(deviceLoginSql);
				resultSet = statement.executeQuery(deviceLoginSql);
				if (resultSet.next()) {
					operationData.deviceLogin = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 总充值玩家数
			{
				String totalPayUserSql = "select count(distinct puid) from recharge where date <= '" + date + "'";
				if (platform != null && platform.length() > 0) {
					totalPayUserSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					totalPayUserSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					totalPayUserSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(totalPayUserSql);
				resultSet = statement.executeQuery(totalPayUserSql);
				if (resultSet.next()) {
					operationData.totalPayUser = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 当日充值玩家数
			{
				String payUserSql = "select count(distinct puid) from recharge where date = '" + date + "'";
				if (platform != null && platform.length() > 0) {
					payUserSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					payUserSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					payUserSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(payUserSql);
				resultSet = statement.executeQuery(payUserSql);
				if (resultSet.next()) {
					operationData.payUser = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 总充值设备数
			{
				String totalPayDeviceSql = "select count(device) from recharge where date <= '" + date + "'";
				if (platform != null && platform.length() > 0) {
					totalPayDeviceSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					totalPayDeviceSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					totalPayDeviceSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(totalPayDeviceSql);
				resultSet = statement.executeQuery(totalPayDeviceSql);
				if (resultSet.next()) {
					operationData.totalPayDevice = Integer.valueOf(resultSet.getString(1));
				}
			}
						
			// 当日充值设备数
			{
				String payDeviceSql = "select count(distinct device) from recharge where date = '" + date + "'";
				if (platform != null && platform.length() > 0) {
					payDeviceSql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					payDeviceSql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					payDeviceSql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(payDeviceSql);
				resultSet = statement.executeQuery(payDeviceSql);
				if (resultSet.next()) {
					operationData.payDevice = Integer.valueOf(resultSet.getString(1));
				}
			}
			
			// 总充值额
			{
				String totalPayMoneySql = "select sum(pay) from recharge where date <= '" + date + "'";
				if (platform != null && platform.length() > 0) {
					totalPayMoneySql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					totalPayMoneySql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					totalPayMoneySql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(totalPayMoneySql);
				resultSet = statement.executeQuery(totalPayMoneySql);
				if (resultSet.next()) {
					operationData.totalPayMoney = Integer.valueOf(resultSet.getInt(1));
				}
			}
			
			// 当日充值额
			{
				String payMoneySql = "select sum(pay) from recharge where date = '" + date + "'";
				if (platform != null && platform.length() > 0) {
					payMoneySql += " and platform = '" + platform +"'";
				}
				if (server != null && server.length() > 0) {
					payMoneySql += " and server = '" + server +"'";
				}
				if (channel != null && channel.length() > 0) {
					payMoneySql += " and channel = '" + channel +"'";
				}
				HawkLog.logPrintln(payMoneySql);
				resultSet = statement.executeQuery(payMoneySql);
				if (resultSet.next()) {
					operationData.payMoney = Integer.valueOf(resultSet.getInt(1));
				}
			}
			
			// 充值指标计算
			{
				if (operationData.userLogin > 0) {
					operationData.ARPU = (float)operationData.payMoney / (float)operationData.userLogin;
				}
				
				if (operationData.deviceLogin > 0) {
					operationData.ARPD = (float)operationData.payMoney / (float)operationData.deviceLogin;
				}
				
				if (operationData.payUser > 0) {
					operationData.ARPPU = (float)operationData.payMoney / (float)operationData.payUser;
				}
				
				if (operationData.userLogin > 0) {
					operationData.PayRate = (float)operationData.payUser / (float)operationData.userLogin;
				}
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	public static boolean calcLtvData(String game, String platform, String channel, String date, OperationData operationData, boolean forceAll) {
		Statement statement = null;
		try {
			Calendar calendar = HawkTime.stringToCalendar(date);
			
			statement = DBManager.getInstance().createStatement(game);
			operationData.LTV2 = calcLTV(statement, game, platform, channel, calendar, operationData.newUser, 2);
			operationData.LTV3 = calcLTV(statement, game, platform, channel, calendar, operationData.newUser, 3);
			operationData.LTV7 = calcLTV(statement, game, platform, channel, calendar, operationData.newUser, 7);
			operationData.LTV14 = calcLTV(statement, game, platform, channel, calendar, operationData.newUser, 14);
			operationData.LTV30 = calcLTV(statement, game, platform, channel, calendar, operationData.newUser, 30);
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
	
	public static boolean calcRetentionData(String game, String platform, String channel, String date, OperationData operationData, boolean forceAll) {
		Statement statement = null;
		try {
			Calendar calendar = HawkTime.stringToCalendar(date);
			
			statement = DBManager.getInstance().createStatement(game);
			operationData.userRetention2 = calcUserRetention(statement, game, platform, channel, calendar, operationData.newUser, 2);
			operationData.userRetention3 = calcUserRetention(statement, game, platform, channel, calendar, operationData.newUser, 3);
			operationData.userRetention7 = calcUserRetention(statement, game, platform, channel, calendar, operationData.newUser, 7);
			operationData.userRetention14 = calcUserRetention(statement, game, platform, channel, calendar, operationData.newUser, 14);
			operationData.userRetention30 = calcUserRetention(statement, game, platform, channel, calendar, operationData.newUser, 30);
			
			operationData.deviceRetention2 = calcDeviceRetention(statement, game, platform, channel, calendar, operationData.newDevice, 2);
			operationData.deviceRetention3 = calcDeviceRetention(statement, game, platform, channel, calendar, operationData.newDevice, 3);
			operationData.deviceRetention7 = calcDeviceRetention(statement, game, platform, channel, calendar, operationData.newDevice, 7);
			operationData.deviceRetention14 = calcDeviceRetention(statement, game, platform, channel, calendar, operationData.newDevice, 14);
			operationData.deviceRetention30 = calcDeviceRetention(statement, game, platform, channel, calendar, operationData.newDevice, 30);
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

	public static float calcLTV(Statement statement, String game, String platform, String channel, Calendar calendar, int newUser, int day) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(calendar.getTime());
			
			Calendar ltvCalendar = HawkTime.getCalendar();
			ltvCalendar.setTimeInMillis(calendar.getTimeInMillis());
			ltvCalendar.add(Calendar.DAY_OF_YEAR, day);
			String ltvDay = sdf.format(ltvCalendar.getTime());
			
			String ltvSql = String.format("select sum(pay) from recharge where date >= '%s' and date < '%s'", date, ltvDay);
			if (platform != null && platform.length() > 0) {
				ltvSql += " and platform = '" + platform +"'";
			}
			if (channel != null && channel.length() > 0) {
				ltvSql += " and channel = '" + channel +"'";
			}
			ltvSql += " and (select count(puid) from register where register.date = '" + date +"' and register.puid = recharge.puid limit 1) > 0";
			
			HawkLog.logPrintln(ltvSql);
			ResultSet resultSet = statement.executeQuery(ltvSql);
			if (resultSet.next()) {
				int ltvPayMoney = resultSet.getInt(1);
				if (newUser > 0) {
					return (float)ltvPayMoney / (float)newUser;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	public static float calcUserRetention(Statement statement, String game, String platform, String channel, Calendar calendar, int newUser, int day) {
		try {
			ResultSet resultSet = null;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(calendar.getTime());
			Calendar targetCalendar = HawkTime.getCalendar();
			targetCalendar.setTimeInMillis(calendar.getTimeInMillis());
			targetCalendar.add(Calendar.DAY_OF_YEAR, day-1);
			String targetDay = sdf.format(targetCalendar.getTime());
			
			String retentionSql = "select count(distinct puid) from login where date = '" + targetDay + "'";
			if (platform != null && platform.length() > 0) {
				retentionSql += " and platform = '" + platform +"'";
			}
			if (channel != null && channel.length() > 0) {
				retentionSql += " and channel = '" + channel +"'";
			}
			retentionSql += String.format(" and (select count(puid) from register where register.date = '%s' and register.puid = login.puid limit 1) > 0", date);
			
			HawkLog.logPrintln(retentionSql);
			resultSet = statement.executeQuery(retentionSql);
			if (resultSet.next()) {
				int loginCount = resultSet.getInt(1);
				if (newUser > 0) {
					return (float)loginCount / (float)newUser;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	public static float calcDeviceRetention(Statement statement, String game, String platform, String channel, Calendar calendar, int newDevice, int day) {
		try {
			ResultSet resultSet = null;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(calendar.getTime());
			
			Calendar targetCalendar = HawkTime.getCalendar();
			targetCalendar.setTimeInMillis(calendar.getTimeInMillis());
			targetCalendar.add(Calendar.DAY_OF_YEAR, day-1);
			String targetDay = sdf.format(targetCalendar.getTime());
			
			String retentionSql = "select count(distinct device) from login where date = '" + targetDay + "'";
			if (retentionSql != null && platform.length() > 0) {
				retentionSql += " and platform = '" + platform +"'";
			}
			if (channel != null && channel.length() > 0) {
				retentionSql += " and channel = '" + channel +"'";
			}
			retentionSql += String.format(" and (select count(device) from device where device.date = '%s' and device.device = login.device  limit 1) > 0", date);
			
			HawkLog.logPrintln(retentionSql);
			resultSet = statement.executeQuery(retentionSql);
			if (resultSet.next()) {
				int loginCount = resultSet.getInt(1);
				if (newDevice > 0) {
					return (float)loginCount / (float)newDevice;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
}
