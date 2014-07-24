package com.hawk.collector.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkTickable;

import com.hawk.collector.CollectorServices;

/**
 * 数据库管理器
 * 
 * @author hawk
 */
public class DBManager extends HawkTickable {
	/**
	 * 数据库主机地址
	 */
	String dbHost;
	/**
	 * 数据库用户名
	 */
	String dbUser;
	/**
	 * 数据库登陆密码
	 */
	String dbPwd;
	/**
	 * 上次保活时间
	 */
	long lastHeartBeat = 0;
	/**
	 * 数据库连接
	 */
	Connection dbConnection = null;
	/**
	 * 保持连接
	 */
	Statement dbStatement = null;
	/**
	 * 数据库管理器单例对象
	 */
	static DBManager instance;

	/**
	 * 获取数据库管理器单例对象
	 * 
	 * @return
	 */
	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	/**
	 * 函数
	 */
	private DBManager() {
		CollectorServices.getInstance().addTickable(this);
	}

	/**
	 * 初始化数据库连接
	 * 
	 * @param dbHost
	 * @param dbUser
	 * @param dbPwd
	 * @return
	 */
	public boolean init(String dbHost, String dbUser, String dbPwd) {
		this.dbHost = dbHost;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		return doConnect();
	}

	/**
	 * 进行连接
	 * 
	 * @return
	 */
	private boolean doConnect() {
		try {
			// 加载驱动程序
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				HawkException.catchException(e);
				return false;
			}

			dbConnection = DriverManager.getConnection(dbHost, dbUser, dbPwd);
			dbStatement = dbConnection.prepareStatement("");
			lastHeartBeat = HawkTime.getMillisecond();
		} catch (SQLException e) {
			HawkException.catchException(e);
			return false;
		}
		return dbConnection != null;
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public Connection getConnection() {
		return dbConnection;
	}

	/**
	 * 创建statement
	 * 
	 * @return
	 */
	public Statement createStatement() {
		Statement statement = null;
		try {
			statement = dbConnection.prepareStatement("");
		} catch (SQLException e) {
			HawkException.catchException(e);
		}
		return statement;
	}

	/**
	 * 直接执行sql语句
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public boolean executeSql(String sql) throws SQLException {
		Statement statement = null;
		try {
			statement = DBManager.getInstance().createStatement();
			statement.executeUpdate(sql);
			return true;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	@Override
	public void onTick() {
		try {
			// mysql 连接保持活跃(30s)
			if (HawkTime.getMillisecond() - lastHeartBeat >= 30000) {
				dbStatement.execute("select 1;");
				lastHeartBeat = HawkTime.getMillisecond();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
