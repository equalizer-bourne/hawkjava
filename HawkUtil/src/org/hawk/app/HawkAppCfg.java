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
	 * 游戏名
	 */
	protected final String gameId;
	/**
	 * 平台名
	 */
	protected final String platform;
	/**
	 * 服务器id
	 */
	protected final int serverId;
	/**
	 * 网络接收器端口
	 */
	protected final int acceptorPort;
	/**
	 * 脚本使用端口
	 */
	protected final int  scriptPort;
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
	 * 调试模式
	 */
	protected final boolean isDebug;
	/**
	 * 是否为websocket
	 */
	protected final boolean isWebSocket;
	/**
	 * 是否为http服务器
	 */
	protected final boolean isHttpServer;
	/**
	 * 系统时间差异
	 */
	protected final long calendarOffset;
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
	 * 数据库异步落地线程周期
	 */
	protected final int dbAsyncPeriod;
	/**
	 * 脚本配置文件
	 */
	protected final String scriptXml;
	/**
	 * 数据库实体包路径
	 */
	protected final String entityPackages;
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
	 * cdk地址
	 */
	protected final String cdkHost;
	/**
	 * cdk超时
	 */
	protected final int cdkTimeout;
	/**
	 * 数据上报地址
	 */
	protected final String reportHost;
	/**
	 * 数据上报超时
	 */
	protected final int reportTimeout;
	/**
	 * 订单服务地址
	 */
	protected final String orderAddr;
	/**
	 * 运维服务地址
	 */
	protected final String opsAgentAddr;
	/**
	 * 聊天服务地址
	 */
	protected final String chatAddr;
	
	public HawkAppCfg() {
		admin = "hawk";
		gameId = "";
		platform = "";
		serverId = 0;
		isDebug = true;
		tickPeriod = 50;
		threadNum = 4;
		taskThreads = 0;
		dbThreads = 0;
		calendarOffset = 0;
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
		scriptPort = 0;
		configPackages = null;
		servicePath = null;
		cdkHost = "";
		cdkTimeout = 1000;
		reportHost = "";
		reportTimeout = 1000;
		orderAddr = "";
		opsAgentAddr = "";
		chatAddr = "";
	}
	
	public String getAdmin() {
		return admin;
	}
	
	public String getGameId() {
		return gameId;
	}

	public String getPlatform() {
		return platform;
	}

	public int getServerId() {
		return serverId;
	}

	public boolean isDebug() {
		return isDebug;
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
	
	public int getScriptPort() {
		return scriptPort;
	}

	public String getConfigPackages() {
		return configPackages;
	}

	public String getServicePath() {
		return servicePath;
	}

	public boolean isWebSocket() {
		return isWebSocket;
	}

	public boolean isHttpServer() {
		return isHttpServer;
	}
	
	public String getCdkHost() {
		return cdkHost;
	}

	public int getCdkTimeout() {
		return cdkTimeout;
	}

	public String getReportHost() {
		return reportHost;
	}

	public int getReportTimeout() {
		return reportTimeout;
	}

	public String getOrderAddr() {
		return orderAddr;
	}

	public String getOpsAgentAddr() {
		return opsAgentAddr;
	}
	
	public String getChatAddr() {
		return chatAddr;
	}
}
