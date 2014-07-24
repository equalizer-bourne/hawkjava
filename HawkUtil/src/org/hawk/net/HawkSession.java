package org.hawk.net;

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
import org.hawk.security.HawkSecurity;

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
	 * 是否活跃会话
	 */
	protected boolean active;
	/**
	 * 实际会话对象
	 */
	protected IoSession session;
	/**
	 * 输入io缓冲区
	 */
	protected IoBuffer inBuffer;
	/**
	 * 输出io缓冲区
	 */
	protected IoBuffer outBuffer;
	/**
	 * 安全对象
	 */
	private HawkSecurity security;
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
	private HawkAppObj appObj;

	/**
	 * 会话构造
	 */
	public HawkSession() {
		active = false;
		inBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
		outBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
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
	 * 获取会话实体
	 * 
	 * @return
	 */
	public IoSession getIoSession() {
		return this.session;
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
		
		if (this.appObj != null) {
			this.appObj.setSession(this);
		}
 	}

	/**
	 * 获取会话的ip地址
	 * 
	 * @return
	 */
	public String getIpAddr() {
		return session.getRemoteAddress().toString().split(":")[0].substring(1);
	}

	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		if (isActive()) {
			try {
				session.write(protocol);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 关闭会话
	 * 
	 * @return
	 */
	public boolean close() {
		if (this.session != null) {
			// 参数true表示立即关闭, false表示等待写操作完成
			this.session.close(false);
			this.active = false;
			return true;
		}
		return false;
	}

	/**
	 * 清空对象
	 */
	protected void clear() {
		this.active = false;
		this.session = null;
		this.inBuffer = null;
		this.outBuffer = null;
		this.security = null;
		this.encryption = null;
		this.decryption = null;
		this.appObj = null;
	}

	/**
	 * 会话开启
	 * 
	 * @param session
	 */
	public boolean onOpened(IoSession session) {
		// 设置读取数据的缓冲区大小
		session.getConfig().setReadBufferSize(HawkNetManager.getInstance().getSessionBufSize());
		// 读写通道无操作进入空闲状态
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, HawkNetManager.getInstance().getSessionIdleTime());
		// 加解密组件
		if (HawkNetManager.getInstance().enableEncryption()) {
			setEncryption(new HawkEncryption());
			setDecryption(new HawkDecryption());
		}

		// 初始化会话绑定
		this.session = session;
		this.active = true;
		// 绑定本地会话对象
		this.session.setAttribute(HawkSession.SESSION_ATTR, this);

		// 通知会话开启
		return HawkApp.getInstance().onSessionOpened(this);
	}

	/**
	 * 会话接收到消息
	 * 
	 * @param message
	 */
	public void onReceived(Object message) {
		if (message instanceof HawkProtocol) {
			HawkProtocol protocol = (HawkProtocol) message;
			// 会话安全校验
			if (security != null && !security.update(this, protocol)) {
				HawkLog.logPrintln(String.format("session security check closed, ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
				close();
				return;
			}

			// 协议是否处理
			if (!HawkApp.getInstance().onSessionProtocol(this, protocol)) {
				// 协议为被注册处理器处理, 直接视为无效协议
				HawkLog.logPrintln(String.format("session protocol handle closed, ipaddr: %s, protocol: %d", getIpAddr(), protocol.getType()));
				close();
				return;
			}
		}
	}

	/**
	 * 解码错误回调
	 */
	public void onDecodeError() {
		HawkLog.logPrintln(String.format("session protocol decode error, ipaddr: %s", getIpAddr()));
		close();
	}

	/**
	 * 会话空闲
	 */
	public void onIdle(IdleStatus status) {
		// 心跳停止, 关闭会话
		if (session.getConfig().getBothIdleTimeInMillis() > 0) {
			HawkLog.logPrintln(String.format("session idle closed, ipaddr: %s, status: %s", getIpAddr(), status.toString()));
			close();
		}
	}

	/**
	 * 会话被关闭
	 */
	public void onClosed() {
		if (this.session != null) {
			// 通知会话关闭
			HawkApp.getInstance().onSessionClosed(this);
			// 清理属性
			this.session.setAttribute(HawkSession.SESSION_ATTR, null);
			// 清理会话数据
			clear();
		}
	}
}
