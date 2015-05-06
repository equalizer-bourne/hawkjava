package org.hawk.app;

import org.hawk.config.HawkConfigBase;

/**
 * 应用配置类
 * 
 * @author hawk
 * 
 */
public class HawkAppCfg extends HawkConfigBase {
	/**
	 * 管理员
	 */
	protected final String admin;
	/**
	 * 是否允许控制台打印
	 */
	protected final boolean console;
	/**
	 * 调试模式
	 */
	protected final boolean isDebug;
	/**
	 * 消息任务模式
	 */
	protected final boolean msgTaskMode;
	/**
	 * 对象缓存
	 */
	protected final boolean objCache;
	/**
	 * 帧更新周期
	 */
	protected final int tickPeriod;
	/**
	 * 逻辑线程数
	 */
	protected final int threadNum;
	/**
	 * 线程数
	 */
	protected final int taskThreads;
	/**
	 * 逻辑线程数
	 */
	protected final int dbThreads;
	/**
	 * 系统时间差异
	 */
	protected final long calendarOffset;
	/**
	 * 主机ip地址
	 */
	protected final String hostIp;
	/**
	 * 网络接收器端口
	 */
	protected final int acceptorPort;
	/**
	 * 是否为websocket
	 */
	protected final boolean isWebSocket;
	/**
	 * 是否为http服务器
	 */
	protected final boolean isHttpServer;
	/**
	 * 会话缓冲区大小
	 */
	protected final int sessionBuffSize;
	/**
	 * 会话空闲超时
	 */
	protected final int sessionIdleTime;
	/**
	 * 会话协议频率
	 */
	protected final int sessionPPS;
	/**
	 * 会话加密
	 */
	protected final boolean sessionEncryption;
	/**
	 * 会话数限制
	 */
	protected final int sessionMaxSize;
	/**
	 * 接收协议最大字节数
	 */
	protected final int recvProtoMaxSize;
	/**
	 * mina的ioFilter线程数, 用了逻辑处理, 不采用系统的msg调度模式
	 */
	protected final int ioFilterChain;
	/**
	 * 数据库连接hbm配置
	 */
	protected final String dbHbmXml;
	/**
	 * 数据库连接地址
	 */
	protected final String dbConnUrl;
	/**
	 * 数据库连接用户名
	 */
	protected final String dbUserName;
	/**
	 * 数据库连接密码
	 */
	protected final String dbPassWord;
	/**
	 * 数据库实体包路径
	 */
	protected final String entityPackages;
	/**
	 * 数据库异步落地线程周期
	 */
	protected final int dbAsyncPeriod;
	/**
	 * 脚本配置文件
	 */
	protected final String scriptXml;
	/**
	 * 配置文件包名
	 */
	protected final String configPackages;
	/**
	 * sercice服务jar包路径
	 */
	protected final String servicePath;
	/**
	 * 协议处理超时时间
	 */
	protected final long protoTimeout;
	/**
	 * 数据库操作超时时间
	 */
	protected final long dbOpTimeout;
	/**
	 * 通知邮箱地址
	 */
	protected final String emailUser;
	/**
	 * 邮箱密码
	 */
	protected final String emailPwd;
	
	public HawkAppCfg() {
		admin = "hawk";
		console = true;
		isDebug = true;
		objCache = false;
		msgTaskMode = true;
		tickPeriod = 50;
		threadNum = 4;
		taskThreads = 0;
		dbThreads = 0;
		calendarOffset = 0;
		hostIp = "";
		acceptorPort = 0;
		isWebSocket = false;
		isHttpServer = false;
		sessionBuffSize = 4096;
		sessionIdleTime = 0;
		sessionPPS = 0;
		sessionEncryption = false;
		sessionMaxSize = 0;
		recvProtoMaxSize = 0;
		ioFilterChain = 0;
		protoTimeout = 100;
		dbOpTimeout = 50;
		dbHbmXml = null;
		dbConnUrl = null;
		dbUserName = null;
		dbPassWord = null;
		entityPackages = null;
		dbAsyncPeriod = 600000;
		scriptXml = null;
		configPackages = null;
		servicePath = null;
		emailUser = "hawk_exception@163.com";
		emailPwd = "hawk.dai";
	}
	
	public String getAdmin() {
		return admin;
	}
	
	public boolean isConsole() {
		return console;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public boolean isMsgTaskMode() {
		return msgTaskMode;
	}
	
	public int getTickPeriod() {
		return tickPeriod;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public int getTaskThreads() {
		return taskThreads;
	}
	
	public int getDbThreads() {
		return dbThreads;
	}
	
	public long getCalendarOffset() {
		return calendarOffset;
	}

	public String getHostIp() {
		return hostIp;
	}
	
	public int getAcceptorPort() {
		return acceptorPort;
	}
	
	public int getSessionBuffSize() {
		return sessionBuffSize;
	}

	public int getSessionIdleTime() {
		return sessionIdleTime;
	}

	public int getSessionPPS() {
		return sessionPPS;
	}

	public boolean isSessionEncryption() {
		return sessionEncryption;
	}

	public int getSessionMaxSize() {
		return sessionMaxSize;
	}

	public int getRecvProtoMaxSize() {
		return recvProtoMaxSize;
	}
	
	public int getIoFilterChain() {
		return ioFilterChain;
	}

	public long getProtoTimeout() {
		return protoTimeout;
	}
	
	public long getDbOpTimeout() {
		return dbOpTimeout;
	}
	
	public String getDbHbmXml() {
		return dbHbmXml;
	}

	public String getDbConnUrl() {
		return dbConnUrl;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public String getDbPassWord() {
		return dbPassWord;
	}
	
	public String getEntityPackages() {
		return entityPackages;
	}

	public int getDbAsyncPeriod() {
		return dbAsyncPeriod;
	}

	public String getScriptXml() {
		return scriptXml;
	}

	public String getConfigPackages() {
		return configPackages;
	}

	public String getServicePath() {
		return servicePath;
	}

	public boolean isObjCache() {
		return objCache;
	}
	
	public boolean isWebSocket() {
		return isWebSocket;
	}

	public boolean isHttpServer() {
		return isHttpServer;
	}
	
	public String getEmailUser() {
		return emailUser;
	}

	public String getEmailPwd() {
		return emailPwd;
	}
}
