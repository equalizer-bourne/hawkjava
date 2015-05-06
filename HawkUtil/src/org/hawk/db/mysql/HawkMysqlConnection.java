package org.hawk.db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

public class HawkMysqlConnection {
	/**
	 * 数据库连接路径
	 */
	private String  dbHost;
	/**
	 * 用户名
	 */
	private String  dbUser;
	/**
	 * 密码
	 */
	private String  dbPwd;
	/**
	 * 上次保活时间
	 */
	private long lastHeartBeat;
	/**
	 * 持有的连接connection
	 */
	private Connection connection;
	/**
	 * 执行sql的statement
	 */
	private Statement  defaultStatement;

	/**
	 * 构造函数
	 */
	protected HawkMysqlConnection() {
		lastHeartBeat = HawkTime.getMillisecond();
	}
	
	/**
	 * 初始化连接
	 * 
	 * @return
	 */
	protected boolean init(String dbHost, String dbUser, String dbPwd) {
		this.dbHost = dbHost;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		return checkConnection();
	}
	
	/**
	 * 数据库连接
	 * 
	 * @return
	 */
	protected synchronized boolean checkConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				if (connection != null) {
					HawkLog.logPrintln(String.format("mysql reconnect, dbHost: %s, dbUser: %s, dbPwd: %s", dbHost, dbUser, dbPwd));
				}
				
				connection = DriverManager.getConnection(dbHost, dbUser, dbPwd);
				if (connection != null) {
					defaultStatement = connection.createStatement();
					return defaultStatement != null;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 保存连接活跃
	 */
	protected void keepAlive() {
		try {
			// mysql 连接保持活跃(60s)
			if (defaultStatement != null && HawkTime.getMillisecond() - lastHeartBeat >= 60000) {
				// 重连
				checkConnection();
				
				defaultStatement.execute("select 1;");
				lastHeartBeat = HawkTime.getMillisecond();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 判断会话是否有效
	 * 
	 * @return
	 */
	public boolean isValid() {
		try {
			return connection != null && !connection.isClosed() && defaultStatement != null;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 获取db地址
	 * 
	 * @return
	 */
	public String getDbHost() {
		return dbHost;
	}

	/**
	 * 获取db地址
	 * 
	 * @return
	 */
	public String getDbUser() {
		return dbUser;
	}

	/**
	 * 获取db用户密码
	 * 
	 * @return
	 */
	public String getDbPwd() {
		return dbPwd;
	}
	
	/**
	 * 创建statement
	 * 
	 * @return
	 */
	protected synchronized Statement createStatement() {
		Statement statement = null;
		try {
			if (connection != null) {
				checkConnection();
				statement = connection.prepareStatement("");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return statement;
	}
	
	/**
	 * 创建statement
	 * 
	 * @return
	 */
	protected synchronized Statement prepareStatement(String sql) {
		Statement statement = null;
		try {
			if (connection != null) {
				checkConnection();
				statement = connection.prepareStatement("");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return statement;
	}
}
