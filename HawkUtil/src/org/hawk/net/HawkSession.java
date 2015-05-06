package org.hawk.net;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.cryption.HawkDecryption;
import org.hawk.cryption.HawkEncryption;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.security.HawkSecurity;

import com.google.gson.JsonObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.sun.net.httpserver.HttpExchange;

/**
 * 封装的框架会话对象
 * 
 * @author hawk
 */
public class HawkSession {
	/**
	 * 会话空闲时间
	 */
	public static final String SESSION_ATTR = "session";
	/**
	 * 二进制web会话模式
	 */
	public static final int BINARY_WEB_SESSION = 0x01;
	/**
	 * json文本会话模式
	 */
	public static final int JSON_WEB_SESSION = 0x02;
	
	/**
	 * 是否活跃会话
	 */
	protected boolean active;
	/**
	 * 输入io缓冲区
	 */
	protected IoBuffer inBuffer;
	/**
	 * 输出io缓冲区
	 */
	protected IoBuffer outBuffer;
	/**
	 * 输出缓冲锁
	 */
	protected Lock sessionLock;
	/**
	 * 安全对象
	 */
	protected HawkSecurity security;
	/**
	 * 加密对象
	 */
	protected HawkEncryption encryption;
	/**
	 * 解密对象
	 */
	protected HawkDecryption decryption;
	/**
	 * 会话应用对象
	 */
	protected HawkAppObj appObj;
	/**
	 * 对应ip地址
	 */
	protected String ipAddr;
	/**
	 * webSession模式(>0表示是webSession)
	 */
	protected int webSessionMode;
	/**
	 * websocket协议
	 */
	protected String webSocketProtocol;
	/**
	 * 访问源
	 */
	protected String webSocketOrigin;
	/**
	 * 实际会话对象
	 */
	protected IoSession session;
	/**
	 * http会话对象
	 */
	protected HttpExchange httpExchange;
	/**
	 * 协议基数号
	 */
	protected int protocolOrder;
	/**
	 * 用户数据
	 */
	protected Object userObject;
	
	/**
	 * 会话构造
	 */
	public HawkSession() {
		active = false;
		ipAddr = "0.0.0.0";
		webSessionMode = 0;
		webSocketProtocol = "";
		webSocketOrigin = "";
		protocolOrder = 0;
	}

	/**
	 * 判断是否活跃会话
	 * 
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * 是否为web会话
	 * 
	 * @return
	 */
	public boolean isWebSession() {
		return webSessionMode > 0;
	}
	
	/**
	 * 设置为web会话
	 * 
	 * @return
	 */
	public void setWebSession(boolean isWebSession) {
		if (webSessionMode <= 0) {
			webSessionMode = BINARY_WEB_SESSION;
		}
	}
	
	/**
	 * 判断是否为文本web会话模式
	 * 
	 * @return
	 */
	public boolean isJsonWebSession() {
		return (webSessionMode & JSON_WEB_SESSION) > 0;
	}
	
	/**
	 * 设置web会话模式
	 * 
	 * @param mode
	 */
	public void setWebSessionMode(int mode) {
		this.webSessionMode = mode;
	}
	
	/**
	 * 获取websocket协议类型
	 * @return
	 */
	public String getWebSocketProtocol() {
		return webSocketProtocol;
	}
	
	/**
	 * 设置websocket协议类型
	 * @param webSocketProtocol
	 */
	public void setWebSocketProtocol(String webSocketProtocol) {
		this.webSocketProtocol = webSocketProtocol;
	}
	
	/**
	 * 获取websocket源
	 * @return
	 */
	public String getWebSocketOrigin() {
		return webSocketOrigin;
	}
	
	/**
	 * 设置websocket协议类型
	 * @param webSocketOrigin
	 */
	public void setWebSocketOrigin(String webSocketOrigin) {
		this.webSocketOrigin = webSocketOrigin;
	}
	
	/**
	 * 获取会话实体
	 * 
	 * @return
	 */
	public IoSession getIoSession() {
		return this.session;
	}
	
	/**
	 * 获取真实的http会话
	 * 
	 * @return
	 */
	public HttpExchange getHttpExchange() {
		return httpExchange;
	}
	
	/**
	 * 获取输入缓冲
	 * 
	 * @return
	 */
	public IoBuffer getInBuffer() {
		return inBuffer;
	}

	/**
	 * 获取输出缓冲
	 * 
	 * @return
	 */
	public IoBuffer getOutBuffer() {
		return outBuffer;
	}

	/**
	 * 获取安全对象
	 */
	public HawkSecurity getSecurity() {
		return this.security;
	}

	/**
	 * 设置安全对象
	 */
	public void setSecurity(HawkSecurity security) {
		this.security = security;
	}

	/**
	 * 锁定会话
	 */
	public void lock() {
		sessionLock.lock();
	}
	
	/**
	 * 解锁会话
	 */
	public void unlock() {
		sessionLock.unlock();
	}
	
	/**
	 * 获取加密组件
	 * 
	 * @return
	 */
	public HawkEncryption getEncryption() {
		return encryption;
	}

	/**
	 * 设置加密组件
	 * 
	 * @param encryption
	 */
	public void setEncryption(HawkEncryption encryption) {
		this.encryption = encryption;
	}

	/**
	 * 获取解密组件
	 * 
	 * @return
	 */
	public HawkDecryption getDecryption() {
		return decryption;
	}

	/**
	 * 设置解密组件
	 * 
	 * @param decryption
	 */
	public void setDecryption(HawkDecryption decryption) {
		this.decryption = decryption;
	}

	/**
	 * 设置协议序列号
	 * 
	 * @param protocolOrder
	 */
	public void setProtocolOrder(int protocolOrder) {
		this.protocolOrder = protocolOrder;
	}
	
	/**
	 * 获取当前的协议id号
	 * 
	 * @return
	 */
	public int getProtocolOrder() {
		return protocolOrder;
	}
	
	/**
	 * 获取应用对象
	 * 
	 * @return
	 */
	public HawkAppObj getAppObject() {
		return this.appObj;
	}

	/**
	 * 设置应用对象
	 * 
	 * @param object
	 */
	public void setAppObject(HawkAppObj appObj) {
		this.appObj = appObj;
		/* 在应用层替换
		if (this.appObj != null) {
			this.appObj.setSession(this);
		}
		*/
 	}

	/**
	 * 获取用户数据对象
	 * 
	 * @return
	 */
	public Object getUserObject() {
		return userObject;
	}
	
	/**
	 * 设置用户数据
	 * 
	 * @param userObject
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * 获取会话的ip地址
	 * 
	 * @return
	 */
	public String getIpAddr() {
		return ipAddr;
	}

	/**
	 * 发送协议(type 必须大于0)
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		if (isActive() && protocol != null) {
			try {
				// 文本模式的websession发送协议
				if (isWebSession() && isJsonWebSession()) {
					String pbJson = JsonFormat.printToString(protocol.getBuilder().build());
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("type", protocol.getType());
					jsonObject.addProperty("protocol", pbJson);
					return sendMessage(jsonObject.toString());
				}
				
				// 回复http协议
				if (httpExchange != null) {
					return false;
				}
				
				// 二进制模式协议发送
				session.write(protocol);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (protocol != null) {
			HawkLog.errPrintln(String.format("send protocol failed, active: %s, protocol: %d", active?"true":"false", protocol.getType()));
		}
		return false;
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	public boolean sendMessage(Object message) {
		if (isActive() && message != null) {
			try {
				if (session != null) {
					session.write(message);
					return true;
				} else if (httpExchange != null) {
					HawkOSOperator.sendHttpResponse(httpExchange, message.toString());
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		HawkLog.errPrintln(String.format("send message failed, active: %s", active?"true":"false"));
		return false;
	}
	
	/**
	 * 关闭会话
	 * 
	 * @return
	 */
	public synchronized boolean close(boolean immediately) {
		if (this.session != null) {
			if (HawkApp.getInstance().getAppCfg().isDebug() && ipAddr != null) {
				HawkLog.logPrintln(String.format("session be closing, ipaddr: %s", ipAddr));
			}
			
			// 参数true表示立即关闭, false表示等待写操作完成
			this.session.close(immediately);
		}
		
		// 重置属性
		this.protocolOrder = 0;
		this.active = false;			
		return true;
	}

	/**
	 * 清空对象
	 */
	protected synchronized boolean clear() {
		this.active = false;
		this.inBuffer = null;
		this.outBuffer = null;
		this.security = null;
		this.encryption = null;
		this.decryption = null;
		this.appObj = null;
		this.session = null;
		this.httpExchange = null;
		return true;
	}

	/**
	 * 会话开启
	 * 
	 * @param session
	 */
	public boolean onOpened(IoSession session) {
		this.session = (IoSession) session;
		
		// 创建tcp会话需要的数据
		sessionLock = new ReentrantLock();
		inBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
		outBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
		
		// 设置读取数据的缓冲区大小
		this.session.getConfig().setReadBufferSize(HawkNetManager.getInstance().getSessionBufSize());
		// 读写通道无操作进入空闲状态
		if (!HawkApp.getInstance().getAppCfg().isDebug()) {
			this.session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, HawkNetManager.getInstance().getSessionIdleTime());
		}
		
		// 加解密组件
		if (HawkNetManager.getInstance().enableEncryption()) {
			setEncryption(new HawkEncryption());
			setDecryption(new HawkDecryption());
		}

		// 绑定本地会话对象
		this.session.setAttribute(HawkSession.SESSION_ATTR, this);
		// 设置激活状态
		this.active = true;
		
		try {
			// 获取ip地址
			this.ipAddr = this.session.getRemoteAddress().toString().split(":")[0].substring(1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 通知会话开启
		return HawkApp.getInstance().onSessionOpened(this);
	}

	/**
	 * 会话开启
	 * 
	 * @param httpExchange
	 */
	public boolean onOpened(HttpExchange httpExchange) {
		this.httpExchange = httpExchange;
		
		// 创建tcp会话需要的数据
		sessionLock = new ReentrantLock();
		inBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
		outBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
		
		// 设置激活状态
		this.active = true;
		
		try {
			// 获取ip地址
			this.ipAddr = httpExchange.getRemoteAddress().toString().split(":")[0].substring(1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// 通知会话开启
		return HawkApp.getInstance().onSessionOpened(this);
	}
	
	/**
	 * 会话接收到消息
	 * 
	 * @param message
	 */
	public boolean onReceived(Object message) {
		if (message instanceof HawkProtocol) {
			HawkProtocol protocol = (HawkProtocol) message;
			
			// 协议字节大小控制
			if (HawkApp.getInstance().getAppCfg().getRecvProtoMaxSize() > 0 && 
				protocol.getSize() > HawkApp.getInstance().getAppCfg().getRecvProtoMaxSize()) {
				HawkLog.logPrintln(String.format("session protocol size overflow, ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
				close(false);
				return false;
			}
				
			// 会话安全校验
			if (security != null && !security.update(this, protocol)) {
				HawkLog.logPrintln(String.format("session security check closed, ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
				close(false);
				return false;
			}

			// 协议log
			if (HawkApp.getInstance().getAppCfg().isDebug()) {
				HawkLog.logPrintln(String.format("session protocol , ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
			}
			
			// 协议是否处理
			if (!HawkApp.getInstance().onSessionProtocol(this, protocol)) {
				// 协议为被注册处理器处理, 直接视为无效协议
				HawkLog.logPrintln(String.format("session protocol handle closed, ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
				close(false);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 解码错误回调
	 */
	public boolean onDecodeFailed() {
		HawkLog.logPrintln(String.format("session protocol decode error, ipaddr: %s", getIpAddr()));
		close(false);
		return true;
	}

	/**
	 * 会话空闲
	 */
	public boolean onIdle(IdleStatus status) {
		// 心跳停止, 关闭会话
		if (this.session != null && session.getConfig().getBothIdleTimeInMillis() > 0) {
			HawkLog.logPrintln(String.format("session idle closed, ipaddr: %s, status: %s", getIpAddr(), status.toString()));
			close(false);
		}
		return true;
	}

	/**
	 * 会话被关闭
	 */
	public boolean onClosed() {
		if (this.session != null) {
			HawkApp.getInstance().onSessionClosed(this);
			if (HawkApp.getInstance().isRunning()) {
				// 清理属性
				this.session.setAttribute(HawkSession.SESSION_ATTR, null);
				// 清理会话数据
				clear();
			}
		}
		return true;
	}
}
