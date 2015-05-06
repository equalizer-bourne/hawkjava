package org.hawk.util.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppCfg;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScriptManager;
import org.hawk.util.HawkTickable;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据上报服务
 * 
 * @author hawk
 */
public class HawkReportService extends HawkTickable {
	/**
	 * 调试日志对象
	 */
	static Logger reportLogger = LoggerFactory.getLogger("Report");

	/**
	 * 钻石类型定义
	 */
	public static int REPORT_GOLD_TYPE_PAY = 1;
	public static int REPORT_GOLD_TYPE_SYS = 2;
	
	/**
	 * 钻石改变类型(1: 增加, 2: 减少)
	 */
	public static int REPORT_GOLD_CHANGE_INC = 1;
	public static int REPORT_GOLD_CHANGE_DEC = 2;	

	/**
	 * 上报注册数据
	 * 
	 * @author hawk
	 */
	public static class RegisterData {
		public String puid;
		public String device;
		public int playerId;
		public String time;

		public RegisterData() {
		}

		public RegisterData(String puid, String device, int playerId, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.time = time;
		}
	}

	/**
	 * 上报登陆数据
	 * 
	 * @author hawk
	 */
	public static class LoginData {
		public String puid;
		public String device;
		public int playerId;
		public int period;
		public String time;

		public LoginData() {
		}

		public LoginData(String puid, String device, int playerId, int period, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.period = period;
			this.time = time;
		}
	}

	/**
	 * 上报充值数据
	 * 
	 * @author hawk
	 */
	public static class RechargeData {
		public String puid;
		public String device;
		public int playerId;
		public String playerName;
		public int playerLevel;
		public String orderId;
		public String productId;
		public int payMoney;
		public String currency;
		public String time;

		public RechargeData() {
			this.currency = "RMB";
			this.productId = "0";
			this.time = HawkTime.getTimeString();
		}

		public RechargeData(String puid, String device, int playerId, String playerName, int playerLevel, String orderId, String productId, int payMoney, String currency, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.playerName = playerName;
			this.playerLevel = playerLevel;
			this.orderId = orderId;
			this.productId = productId;
			this.payMoney = payMoney;
			this.currency = currency;
			this.time = time;
		}

		public void setPuid(String puid) {
			this.puid = puid;
		}

		public void setDevice(String device) {
			this.device = device;
		}

		public void setPlayerId(int playerId) {
			this.playerId = playerId;
		}

		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}

		public void setPlayerLevel(int playerLevel) {
			this.playerLevel = playerLevel;
		}

		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}
		
		public void setPayMoney(int payMoney) {
			this.payMoney = payMoney;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public void setTime(String time) {
			this.time = time;
		}
	}

	/**
	 * 上报钻石数据
	 * 
	 * @author hawk
	 */
	public static class GoldData {
		public String puid;
		public String device;
		public int playerId;
		public int playerLevel;
		public int changeType;
		public String changeAction;
		public int goldType;
		public int gold;
		public String time;

		public GoldData() {
			this.time = HawkTime.getTimeString();
		}

		public GoldData(String puid, String device, int playerId, int playerLevel, int changeType, String changeAction, int goldType, int gold, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.playerLevel = playerLevel;
			this.changeType = changeType;
			this.changeAction = changeAction;
			this.goldType = goldType;
			this.gold = gold;
			this.time = time;
		}

		public void setPuid(String puid) {
			this.puid = puid;
		}

		public void setDevice(String device) {
			this.device = device;
		}

		public void setPlayerId(int playerId) {
			this.playerId = playerId;
		}

		public void setPlayerLevel(int playerLevel) {
			this.playerLevel = playerLevel;
		}

		public void setChangeType(int changeType) {
			this.changeType = changeType;
		}

		public void setChangeAction(String changeAction) {
			this.changeAction = changeAction;
		}

		public void setGoldType(int goldType) {
			this.goldType = goldType;
		}

		public void setGold(int gold) {
			this.gold = gold;
		}

		public void setTime(String time) {
			this.time = time;
		}
	}

	/**
	 * 上报新手指引数据
	 * 
	 * @author hawk
	 */
	public static class TutorialData {
		public String puid;
		public String device;
		public int playerId;
		public int playerLevel;
		public int step;
		public String args;
		public String time;

		public TutorialData() {
			this.time = HawkTime.getTimeString();
		}

		public TutorialData(String puid, String device, int playerId, int playerLevel, int step, String args, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.playerLevel = playerLevel;
			this.step = step;
			this.args = args;
			this.time = time;
		}

		public void setPuid(String puid) {
			this.puid = puid;
		}

		public void setDevice(String device) {
			this.device = device;
		}

		public void setPlayerId(int playerId) {
			this.playerId = playerId;
		}

		public void setPlayerLevel(int playerLevel) {
			this.playerLevel = playerLevel;
		}

		public void setStep(int step) {
			this.step = step;
		}

		public void setArgs(String args) {
			this.args = args;
		}

		public void setTime(String time) {
			this.time = time;
		}
	}
	
	/**
	 * 上报充值数据
	 * 
	 * @author hawk
	 */
	public static class ServerData {
		public String ip;
		public int listenPort;
		public int scriptPort;
		public String dbUrl;
		public String dbUser;
		public String dbPwd;

		public ServerData() {
			this.ip = "";
			this.listenPort = 9595;
			this.scriptPort = 9595;
		}

		public ServerData(String ip, int listenPort, int scriptPort, String dbUrl, String dbUser, String dbPwd) {
			this.ip = ip;
			this.listenPort = listenPort;
			this.scriptPort = scriptPort;
			this.dbUrl = dbUrl;
			this.dbUser = dbUser;
			this.dbPwd = dbPwd;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getListenPort() {
			return listenPort;
		}

		public void setListenPort(int listenPort) {
			this.listenPort = listenPort;
		}

		public int getScriptPort() {
			return scriptPort;
		}

		public void setScriptPort(int scriptPort) {
			this.scriptPort = scriptPort;
		}

		public String getDbUrl() {
			return dbUrl;
		}

		public void setDbUrl(String dbUrl) {
			this.dbUrl = dbUrl;
		}

		public String getDbUser() {
			return dbUser;
		}

		public void setDbUser(String dbUser) {
			this.dbUser = dbUser;
		}

		public String getDbPwd() {
			return dbPwd;
		}

		public void setDbPwd(String dbPwd) {
			this.dbPwd = dbPwd;
		}
	}

	/**
	 * 上报通用数据
	 * 
	 * @author hawk
	 */
	public static class CommonData {
		public String puid;
		public String device;
		public int playerId;
		public String time;
		public List<String> args;

		public CommonData() {
		}

		public CommonData(String puid, String device, int playerId, String time) {
			this.puid = puid;
			this.device = device;
			this.playerId = playerId;
			this.time = time;
		}

		public void setArgs(String... args) {
			if (args != null) {
				if (this.args == null) {
					this.args = new ArrayList<String>(args.length);
				}

				for (String arg : args) {
					if (arg.length() > 0) {
						this.args.add(arg);
					}
				}
			}
		}
	}

	private static final String registerPath = "/report_register";
	private static final String loginPath = "/report_login";
	private static final String rechargePath = "/report_recharge";
	private static final String goldPath = "/report_gold";
	private static final String tutorialPath = "/report_tutorial";
	private static final String serverPath = "/report_server";
	private static final String commonPath = "/report_data";
	private static final String fetchIpPath = "/fetch_myip";
	
	// 所有的query都能添加token作为服务器校验令牌
	private static final String rechargeQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playername=%s&playerlevel=%d&orderid=%s&productid=%s&pay=%d&currency=%s&time=%s";
	private static final String goldQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playerlevel=%d&changetype=%d&changeaction=%s&goldtype=%d&gold=%d&time=%s";
	private static final String tutorialQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&playerlevel=%d&step=%d&args=%s&time=%s";
	private static final String registerQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&time=%s";
	private static final String loginQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&period=%d&time=%s";
	private static final String serverQuery = "game=%s&platform=%s&server=%s&ip=%s&folder=%s&listen_port=%d&script_port=%d&dburl=%s&dbuser=%s&dbpwd=%s";
	private static final String commonQuery = "game=%s&platform=%s&server=%s&puid=%s&device=%s&playerid=%d&time=%s";

	/**
	 * 服务器信息
	 */
	private String myHostIp = "";
	private String gameName = "";
	private String platform = "";
	private String serverId = "";
	private int retryTimes = 1;
	private String token = "";
	
	/**
	 * http对象
	 */
	private String serviceHost = "";
	private HttpClient httpClient = null;
	private GetMethod getMethod = null;

	/**
	 * zmq对象
	 */
	private HawkZmq reportZmq = null;

	/**
	 * 汇报数据
	 */
	private Lock reportLock = null;
	List<Object> reportDatas = null;

	/**
	 * 实例对象
	 */
	private static HawkReportService instance = null;

	/**
	 * 获取全局实例对象
	 * 
	 * @return
	 */
	public static HawkReportService getInstance() {
		if (instance == null) {
			instance = new HawkReportService();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private HawkReportService() {
		httpClient = null;
		getMethod = null;
		reportLock = new ReentrantLock();
		reportDatas = new LinkedList<Object>();

		if (HawkApp.getInstance() != null) {
			HawkApp.getInstance().addTickable(this);
		}
	}


	/**
	 * 初始化cdk服务
	 * 
	 * @return
	 */
	public boolean install(String gameName, String platform, String serverId, String host, int timeout) {
		return install(gameName, platform, serverId, host, timeout, HawkApp.getInstance().getAppCfg());
	}
	
	/**
	 * 初始化cdk服务
	 * 
	 * @return
	 */
	public boolean install(String gameName, String platform, String serverId, String host, int timeout, HawkAppCfg appCfg) {
		try {
			this.gameName = gameName;
			this.platform = platform;
			this.serverId = serverId;
			this.serviceHost = host;

			// 可重复调用
			HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);
			
			if (host.indexOf("tcp://") >= 0) {
				if (!createReportZmq(host)) {
					return false;
				}
			} else {
				if (httpClient == null) {
					httpClient = new HttpClient();
					httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
					httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
				}

				if (getMethod == null) {
					getMethod = new GetMethod(host);
				}

				if (appCfg == null) {
					appCfg = HawkApp.getInstance().getAppCfg();
				}

				if (appCfg != null) {
					initInnerService(appCfg);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		if (!isValid()) {
			HawkLog.errPrintln("install report service failed.");
			return false;
		}

		return true;
	}

	/**
	 * 初始化zmq并上报服务器信息
	 */
	protected boolean initInnerService(HawkAppCfg appCfg) {
		int reportZmqPort = 0;
		myHostIp = appCfg.getHostIp();
		String reportInfo = fetchReportInfo();
		try {
			if (reportInfo != null && reportInfo.length() > 0) {
				reportLogger.info("report service info: " + reportInfo);

				JSONObject jsonObject = JSONObject.fromObject(reportInfo);
				if (jsonObject.containsKey("myIp")) {
					myHostIp = (String) jsonObject.get("myIp");
				}

				if (jsonObject.containsKey("zmqPort")) {
					reportZmqPort = (Integer) jsonObject.get("zmqPort");
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 创建zmq连接
		if (reportZmqPort > 0) {
			try {
				String zmqHost = serviceHost.toLowerCase().replace("http:", "tcp:");
				int pos = zmqHost.indexOf(":", 6);
				if (pos > 0) {
					zmqHost = zmqHost.substring(0, pos + 1);
					zmqHost += reportZmqPort;
				}

				if (createReportZmq(zmqHost)) {
					reportLogger.info("create report zmq service success: " + zmqHost);
				} else {
					reportLogger.info("create report zmq service failed: " + zmqHost);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 上报服务器信息
		int scriptHttpPort = 0;
		if (HawkScriptManager.getInstance() != null && HawkScriptManager.getInstance().getScriptConfig() != null) {
			scriptHttpPort = HawkScriptManager.getInstance().getScriptConfig().getHttpPort();
		}
		
		String userDir = System.getProperty("user.dir");
		userDir = userDir.replace('/', '+').replace('\\', '+');
		String queryParam = String.format(serverQuery, gameName, platform, serverId, myHostIp, userDir,
				appCfg.getAcceptorPort(), scriptHttpPort, 
				appCfg.getDbConnUrl(), appCfg.getDbUserName(), appCfg.getDbPassWord());

		try {
			queryParam = URLEncoder.encode(queryParam, "UTF-8");
			getMethod.setPath(serverPath);
			getMethod.setQueryString(queryParam);
			httpClient.executeMethod(getMethod);
			reportLogger.info("report server info success: " + serverPath + "?" + queryParam);
		} catch (Exception e) {
			reportLogger.info("report server info failed: " + serverPath + "?" + queryParam);
		}

		return true;
	}

	/**
	 * 创建zmq对象
	 * 
	 * @param addr
	 * @return
	 */
	protected boolean createReportZmq(String addr) {
		if (reportZmq == null) {
			reportZmq = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.PUSH);
			if (!reportZmq.connect(addr)) {
				reportZmq = null;
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取上报服务器信息
	 * 
	 * @return
	 */
	protected String fetchReportInfo() {
		try {
			getMethod.setPath(fetchIpPath);
			int status = httpClient.executeMethod(getMethod);
			if (status == HttpStatus.SC_OK) {
				return new String(getMethod.getResponseBody());
			}
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 设置校验令牌
	 * 
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * 上报服务是否有效
	 * 
	 * @return
	 */
	public boolean isValid() {
		if (reportZmq == null && (httpClient == null || getMethod == null)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取重试次数
	 * 
	 * @return
	 */
	public int getRetryTimes() {
		return retryTimes;
	}

	/**
	 * 设置重试次数
	 * 
	 * @param retryTimes
	 */
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	/**
	 * 执行http请求
	 * 
	 * @param path
	 * @param params
	 * @return
	 */
	public synchronized int executeMethod(String path, String params) {
		if (token != null && token.length() > 0) {
			try {
				params += URLEncoder.encode("&token=" + token, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}
		}
		
		if (reportZmq != null) {
			try {
				params = URLDecoder.decode(params, "UTF-8");
				if (!reportZmq.send(path.getBytes(), HawkZmq.HZMQ_SNDMORE)) {
					return -1;
				}

				if (!reportZmq.send(params.getBytes(), 0)) {
					return -1;
				}

				return 0;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else if (getMethod != null) {
			getMethod.setPath(path);
			if (params != null && params.length() > 0) {
				getMethod.setQueryString(params);
			}

			for (int i = 0; i < retryTimes; i++) {
				try {
					return httpClient.executeMethod(getMethod);
				} catch (Exception e) {
					HawkException.catchException(e);
					HawkOSOperator.sleep();
				}
			}
		}
		return -1;
	}

	/**
	 * 注册统计
	 * 
	 * @param registerData
	 */
	public void report(RegisterData registerData) {
		reportLock.lock();
		try {
			reportDatas.add(registerData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 登陆统计
	 * 
	 * @param loginData
	 */
	public void report(LoginData loginData) {
		reportLock.lock();
		try {
			reportDatas.add(loginData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 充值统计
	 * 
	 * @param rechargeData
	 */
	public void report(RechargeData rechargeData) {
		reportLock.lock();
		try {
			reportDatas.add(rechargeData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 钻石统计
	 * 
	 * @param goldData
	 */
	public void report(GoldData goldData) {
		reportLock.lock();
		try {
			reportDatas.add(goldData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 新手指引统计
	 * 
	 * @param goldData
	 */
	public void report(TutorialData tutorialData) {
		reportLock.lock();
		try {
			reportDatas.add(tutorialData);
		} finally {
			reportLock.unlock();
		}
	}
	
	/**
	 * 服务器信息
	 * 
	 * @param serverData
	 */
	public void report(ServerData serverData) {
		reportLock.lock();
		try {
			// 修正ip信息
			if (myHostIp != null && myHostIp.length() > 0) {
				serverData.ip = myHostIp;
			}
			reportDatas.add(serverData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 通用统计
	 * 
	 * @param commonData
	 */
	public void report(CommonData commonData) {
		reportLock.lock();
		try {
			reportDatas.add(commonData);
		} finally {
			reportLock.unlock();
		}
	}

	/**
	 * 上报数据
	 * 
	 * @param registerData
	 * @return
	 */
	private boolean doReport(RegisterData registerData) {
		if (isValid()) {
			try {
				String queryParam = String.format(registerQuery, gameName, platform, serverId, registerData.puid, 
						registerData.device, registerData.playerId, 
						(registerData.time == null || registerData.time.length() <= 0) ? HawkTime.getTimeString() : registerData.time);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + registerPath + "?" + queryParam);

				int status = executeMethod(registerPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 上报数据
	 * 
	 * @param loginData
	 * @return
	 */
	private boolean doReport(LoginData loginData) {
		if (isValid()) {
			try {
				String queryParam = String.format(loginQuery, gameName, platform, serverId, loginData.puid, loginData.device, 
						loginData.playerId, loginData.period, 
						(loginData.time == null || loginData.time.length() <= 0) ? HawkTime.getTimeString() : loginData.time);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + loginPath + "?" + queryParam);

				int status = executeMethod(loginPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 上报数据
	 * 
	 * @param rechargeData
	 * @return
	 */
	private boolean doReport(RechargeData rechargeData) {
		if (isValid()) {
			try {
				String queryParam = String.format(rechargeQuery, gameName, platform, serverId, rechargeData.puid, 
						rechargeData.device, rechargeData.playerId, rechargeData.playerName, rechargeData.playerLevel, 
						rechargeData.orderId, rechargeData.productId, rechargeData.payMoney, rechargeData.currency, 
						(rechargeData.time == null || rechargeData.time.length() <= 0) ? HawkTime.getTimeString() : rechargeData.time);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + rechargePath + "?" + queryParam);

				int status = executeMethod(rechargePath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 上报数据
	 * 
	 * @param goldData
	 * @return
	 */
	private boolean doReport(GoldData goldData) {
		if (isValid()) {
			try {
				String queryParam = String.format(goldQuery, gameName, platform, serverId, goldData.puid, goldData.device, 
						goldData.playerId, goldData.playerLevel, goldData.changeType, goldData.changeAction, 
						goldData.goldType, goldData.gold, 
						(goldData.time == null || goldData.time.length() <= 0) ? HawkTime.getTimeString() : goldData.time);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + goldPath + "?" + queryParam);

				int status = executeMethod(goldPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 上报数据
	 * 
	 * @param goldData
	 * @return
	 */
	private boolean doReport(TutorialData tutorialData) {
		if (isValid()) {
			try {
				String queryParam = String.format(tutorialQuery, gameName, platform, serverId, tutorialData.puid, tutorialData.device, 
						tutorialData.playerId, tutorialData.playerLevel, tutorialData.step, tutorialData.args, 
						(tutorialData.time == null || tutorialData.time.length() <= 0) ? HawkTime.getTimeString() : tutorialData.time);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + tutorialPath + "?" + queryParam);

				int status = executeMethod(tutorialPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}
	
	/**
	 * 上报数据
	 * 
	 * @param serverData
	 * @return
	 */
	private boolean doReport(ServerData serverData) {
		if (isValid()) {
			try {
				String queryParam = String.format(serverQuery, gameName, platform, serverId, serverData.ip, serverData.listenPort, 
						serverData.scriptPort, serverData.dbUrl, serverData.dbUser, serverData.dbPwd);

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + serverPath + "?" + queryParam);

				int status = executeMethod(serverPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 上报数据
	 * 
	 * @param commonData
	 * @return
	 */
	private boolean doReport(CommonData commonData) {
		if (isValid()) {
			try {
				String queryParam = String.format(commonQuery, gameName, platform, serverId, 
						commonData.puid, commonData.device, commonData.playerId, 
						(commonData.time == null || commonData.time.length() <= 0) ? HawkTime.getTimeString() : commonData.time);

				if (commonData.args != null) {
					for (int i = 0; i < commonData.args.size(); i++) {
						queryParam += "&arg" + (i + 1) + "=" + commonData.args.get(i);
					}
				}

				queryParam = URLEncoder.encode(queryParam, "UTF-8");

				reportLogger.info("report: " + commonPath + "?" + queryParam);

				int status = executeMethod(commonPath, queryParam);
				if (status == HttpStatus.SC_OK) {
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 帧更新上报数据
	 */
	@Override
	public void onTick() {
		if (reportDatas.size() > 0) {
			// 取出队列首个上报数据对象
			Object reportData = null;
			reportLock.lock();
			try {
				reportData = reportDatas.remove(0);
			} finally {
				reportLock.unlock();
			}

			// 数据上报操作
			try {
				if (gameName.length() > 0 && platform.length() > 0) {
					if (reportData instanceof RegisterData) {
						doReport((RegisterData) reportData);
					} else if (reportData instanceof LoginData) {
						doReport((LoginData) reportData);
					} else if (reportData instanceof RechargeData) {
						doReport((RechargeData) reportData);
					} else if (reportData instanceof GoldData) {
						doReport((GoldData) reportData);
					} else if (reportData instanceof TutorialData) {
						doReport((TutorialData) reportData);
					} else if (reportData instanceof CommonData) {
						doReport((CommonData) reportData);
					} else if (reportData instanceof ServerData) {
						doReport((ServerData) reportData);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);

				// 上报失败重新放回
				if (!(reportData instanceof ServerData)) {
					reportLock.lock();
					try {
						reportDatas.add(reportData);
					} finally {
						reportLock.unlock();
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
