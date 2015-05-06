package org.hawk.net;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppCfg;
import org.hawk.codec.HawkDecoder;
import org.hawk.codec.HawkEncoder;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.net.client.HawkClientIoHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScriptManager;

import com.sun.net.httpserver.HttpServer;

/**
 * 网络管理器
 * 
 * @author hawk
 */
public class HawkNetManager {
	/**
	 * 会话缓冲区大小
	 */
	private int sessionBufSize = 4096;
	/**
	 * 最大会话数限制
	 */
	private int sessionMaxSize = 0;
	/**
	 * 会话空闲超时
	 */
	private int sessionIdleTime = 0;
	/**
	 * 协议频率
	 */
	private int sessionPPS = 5;
	/**
	 * 是否允许加密
	 */
	private boolean enableEncryption = false;
	/**
	 * 内嵌http服务器
	 */
	private HttpServer httpServer = null;
	/**
	 * 网络接收器
	 */
	private NioSocketAcceptor acceptor;
	/**
	 * 连接器
	 */
	private IoConnector connector;
	/**
	 * io处理器
	 */
	private HawkIoHandler ioHandler;
	/**
	 * ip白名单
	 */
	private Map<String, String> whiteIptables;
	/**
	 * ip黑名单
	 */
	private Map<String, String> blackIptables;

	/**
	 * 实例对象
	 */
	private static HawkNetManager instance;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static HawkNetManager getInstance() {
		if (instance == null) {
			instance = new HawkNetManager();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private HawkNetManager() {
		whiteIptables = new ConcurrentHashMap<String, String>();
		blackIptables = new ConcurrentHashMap<String, String>();
	}

	/**
	 * 初始化网络, 开启接收器
	 * 
	 * @param port
	 * @return
	 */
	public boolean init(int port, int ioFilterChain, boolean asHttp) {
		if (asHttp) {
			return setupHttpServer(port, ioFilterChain);
		}
		
		return init(port, ioFilterChain, null);
	}
	
	/**
	 * 初始化网络, 开启接收器
	 * 
	 * @param port
	 * @return
	 */
	public boolean init(int port, int ioFilterChain, IoHandler ioHandler) {
		try {
			if (port <= 0) {
				return false;
			}
			
			if (acceptor == null) {
				// 服务端的实例
				acceptor = new NioSocketAcceptor();
				// 地址重用
				acceptor.getSessionConfig().setReuseAddress(true);
				acceptor.getSessionConfig().setSoLinger(0);
				// 设置读取数据的缓冲区大小
				acceptor.getSessionConfig().setReadBufferSize(HawkNetManager.getInstance().getSessionBufSize());
				// 读写通道无操作进入空闲状态
				acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, HawkNetManager.getInstance().getSessionIdleTime());
				// 设置编码器&解码器
				acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(HawkEncoder.class, HawkDecoder.class));
				// 添加IoFilterChain线程池
				if (ioFilterChain > 0) {
					OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(ioFilterChain);
					acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executor));
				}
				
				// 设置服务端的handler
				if(ioHandler != null) {
					acceptor.setHandler(ioHandler);
				} else {
					if (this.ioHandler == null) {
						this.ioHandler = new HawkIoHandler();
					}
					acceptor.setHandler(this.ioHandler);
				}
				
	
				// 绑定ip
				acceptor.bind(new InetSocketAddress(port));
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 开启http服务器
	 * 
	 * @param port
	 * @param poolSize
	 * @return
	 */
	public boolean setupHttpServer(int port, int poolSize) {
		try {
			httpServer = HttpServer.create(new InetSocketAddress(port), 0);
			httpServer.setExecutor(Executors.newFixedThreadPool(poolSize));
			httpServer.createContext("/protocol", new HawkHttpHandler());
			httpServer.start();
			
			HawkLog.logPrintln("Http Server [" + "0.0.0.0:" + port + "/protocol] Start OK.");
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 从appCfg的配置初始化数据
	 * 
	 * @param appCfg
	 */
	public boolean initFromAppCfg(HawkAppCfg appCfg) {
		// 检测
		if (!HawkNativeApi.checkHawk()) {
			return false;
		}
		
		if (appCfg.getSessionMaxSize() > 0) {
			setSessionMaxSize(appCfg.getSessionMaxSize());
		}

		setEnableEncryption(appCfg.isSessionEncryption());

		if (appCfg.getSessionBuffSize() > 0) {
			setSessionBufSize(appCfg.getSessionBuffSize());
		}

		if (appCfg.getSessionIdleTime() > 0) {
			setSessionIdleTime(appCfg.getSessionIdleTime());
		}

		if (appCfg.getSessionPPS() > 0) {
			setSessionPPS(appCfg.getSessionPPS());
		}

		return init(appCfg.getAcceptorPort(), appCfg.getIoFilterChain(), appCfg.isHttpServer());
	}

	/**
	 * 关闭网络
	 */
	public void close() {
		if (acceptor != null) {
			try {
				acceptor.unbind();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		if (connector != null) {
			try {
				connector.dispose();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 关闭所有会话
		closeAllSession();
	}

	/**
	 * 关闭所有会话
	 */
	public void closeAllSession() {
		// 关闭所有会话
		Set<HawkSession> sessions = HawkApp.getInstance().getActiveSessions();
		for (HawkSession session : sessions) {
			try {
				session.close(true);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 获取会话空闲超时时间
	 * 
	 * @return
	 */
	public int getSessionIdleTime() {
		return sessionIdleTime;
	}

	/**
	 * 获取io接收器
	 * 
	 * @return
	 */
	public NioSocketAcceptor getAcceptor() {
		return acceptor;
	}
	
	/**
	 * 获取io处理句柄对象
	 * 
	 * @return
	 */
	public HawkIoHandler getIoHandler() {
		return ioHandler;
	}

	/**
	 * 设置会话空闲超时时间
	 * 
	 * @param sessionIdleTime
	 */
	public void setSessionIdleTime(int timeout) {
		this.sessionIdleTime = timeout;
	}

	/**
	 * 获取会话缓冲区大小
	 * 
	 * @return
	 */
	public int getSessionBufSize() {
		return sessionBufSize;
	}

	/**
	 * 设置会话缓冲区大小
	 * 
	 * @param sessionBufSize
	 */
	public void setSessionBufSize(int sessionBufSize) {
		this.sessionBufSize = sessionBufSize;
	}

	/**
	 * 获取协议频率
	 * 
	 * @return
	 */
	public int getSessionPPS() {
		return sessionPPS;
	}

	/**
	 * 设置协议频率
	 * 
	 * @param protocolCPS
	 */
	public void setSessionPPS(int pps) {
		this.sessionPPS = pps;
	}

	/**
	 * 获取最大会话数
	 * 
	 * @return
	 */
	public int getSessionMaxSize() {
		return sessionMaxSize;
	}

	/**
	 * 设置最大会话数
	 * 
	 * @param sessionMaxSize
	 */
	public void setSessionMaxSize(int sessionMaxSize) {
		this.sessionMaxSize = sessionMaxSize;
	}

	/**
	 * 设置会话加解密
	 * 
	 * @param enable
	 */
	public void setEnableEncryption(boolean enable) {
		this.enableEncryption = enable;
	}

	/**
	 * 检测会话是否开启加解密
	 * 
	 * @return
	 */
	public boolean enableEncryption() {
		return this.enableEncryption;
	}

	/**
	 * 获取客户端连接器
	 * 
	 * @return
	 */
	public synchronized IoConnector getConnector() {
		if (connector == null) {
			connector = new NioSocketConnector();
			// 添加IoFilterChain线程池
			OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);
			connector.getFilterChain().addLast("threadPool", new ExecutorFilter(executor));

			connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(HawkEncoder.class, HawkDecoder.class));
			connector.setHandler(new HawkClientIoHandler());
		}
		return connector;
	}

	/**
	 * 添加ip白名单
	 * 
	 * @param ip
	 */
	public void addWhiteIp(String ip) {
		whiteIptables.put(ip, ip);
	}

	/**
	 * 移除ip白名单
	 * 
	 * @param ip
	 */
	public void removeWhiteIp(String ip) {
		whiteIptables.remove(ip);
	}

	/**
	 * 添加ip黑名单
	 * 
	 * @param ip
	 */
	public void addBlackIp(String ip) {
		blackIptables.put(ip, ip);
	}

	/**
	 * 移除ip黑名单
	 * 
	 * @param ip
	 */
	public void removeBlackIp(String ip) {
		whiteIptables.remove(ip);
	}

	/**
	 * 白名单检测, 是否在白名单列表
	 * 
	 * @param ip
	 * @return
	 */
	public boolean checkWhiteIptables(String ip) {
		if (whiteIptables.size() <= 0 || whiteIptables.containsKey(ip) || "127.0.0.1".equals(ip)) {
			return true;
		}
		return false;
	}

	/**
	 * 黑名单检测, 是否在黑名单列表
	 * 
	 * @param ip
	 * @return
	 */
	public boolean checkBlackIptables(String ip) {
		if (!"127.0.0.1".equals(ip) && blackIptables.containsKey(ip)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 系统协议处理
	 * 
	 * @param protocol
	 * @return
	 */
	protected boolean onSysProtocol(HawkProtocol protocol) {
		if (protocol.getType() == 0) {
			if (HawkScriptManager.getInstance().onSysProtocol(protocol)) {
				return true;
			}
		}
		return false;
	}
}
