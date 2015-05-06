package org.hawk.db.mysql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.util.HawkTickable;

public class HawkMysqlSession extends HawkTickable {
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
	 * 持有的连接池
	 */
	private List<HawkMysqlConnection> connections;
	
	/**
	 * 构造函数
	 */
	public HawkMysqlSession() {
		connections = new LinkedList<HawkMysqlConnection>();
		if (HawkApp.getInstance() != null) {
			HawkApp.getInstance().addTickable(this);
		}
	}

	/**
	 * 初始化连接
	 * 
	 * @return
	 */
	public boolean init(String dbHost, String dbUser, String dbPwd, int poolSize) {
		try {
			this.dbHost = dbHost;
			this.dbUser = dbUser;
			this.dbPwd = dbPwd;
			
			for (int i=0; i<poolSize; i++) {
				HawkMysqlConnection connection = new HawkMysqlConnection();
				if (!connection.init(dbHost, dbUser, dbPwd)) {
					return false;
				}
				connections.add(connection);
			}
			return true;
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
	 * 判断会话是否有效
	 * 
	 * @return
	 */
	public boolean isValid() {
		for (HawkMysqlConnection connection : connections) {
			if (connection != null && connection.isValid()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 创建statement
	 * 
	 * @return
	 */
	public synchronized Statement createStatement() {
		Statement statement = null;
		try {
			int threadIdx = (int) (HawkOSOperator.getThreadId() % connections.size());
			HawkMysqlConnection connection = connections.get(threadIdx);
			statement = connection.createStatement();
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
	public synchronized Statement prepareStatement(String sql) {
		Statement statement = null;
		try {
			int threadIdx = (int) (HawkOSOperator.getThreadId() % connections.size());
			HawkMysqlConnection connection = connections.get(threadIdx);
			statement = connection.prepareStatement(sql);
		} catch (Exception e) {
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
	public int executeSql(String sql) {
		Statement statement = null;
		try {
			statement = createStatement();
			int rowCount = statement.executeUpdate(sql);
			return rowCount;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		return 0;
	}
	
	/**
	 * 获取名字
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * 帧更新
	 */
	@Override
	public void onTick() {
		for (HawkMysqlConnection connection : connections) {
			if (connection != null) {
				connection.keepAlive();
			}
		}
	}
}
