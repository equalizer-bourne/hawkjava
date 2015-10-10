package org.hawk.net.mq;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

/**
 * 封装的框架会话对象
 * 
 * @author hawk
 */
public class HawkMQSession {
	/**
	 * 默认缓冲区大小
	 */
	public static final int BUFFER_SIZE = 8192;
	/**
	 * 空闲超时时间
	 */
	public static final int IDLE_TIMEOUT = 60000;
	/**
	 * 会话空闲时间
	 */
	public static final String SESSION_ATTR = "session";
	
	/**
	 * 会话标识
	 */
	protected String identify;
	/**
	 * 输入io缓冲区
	 */
	protected IoBuffer inBuffer;
	/**
	 * 输出io缓冲区
	 */
	protected IoBuffer outBuffer;
	/**
	 * 实际会话对象
	 */
	protected IoSession session;
	/**
	 * 所属服务器
	 */
	protected HawkMQServer mqServer;

	/**
	 * 会话构造
	 */
	public HawkMQSession() {
		// 创建tcp会话需要的缓冲区
		this.inBuffer = IoBuffer.allocate(BUFFER_SIZE).setAutoExpand(true);
		this.outBuffer = IoBuffer.allocate(BUFFER_SIZE).setAutoExpand(true);
	}

	/**
	 * 获取会话的ip地址
	 * 
	 * @return
	 */
	public String getIpAddr() {
		if (session != null) {
			synchronized (session) {
				return session.getRemoteAddress().toString().split(":")[0].substring(1);
			}
		}
		return "";
	}
	
	/**
	 * 获取会话标识
	 * 
	 * @return
	 */
	public String getIdentify() {
		return identify;
	}

	/**
	 * 设置会话标识
	 * 
	 * @param identify
	 */
	public void setIdentify(String identify) {
		this.identify = identify;
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
	 * 关闭会话
	 * 
	 * @param immediately
	 * @return
	 */
	public boolean close(boolean immediately) {
		// 参数true表示立即关闭, false表示等待写操作完成
		if (session != null) {
			session.close(immediately);
			session = null;
		}
		return true;
	}
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		try {
			if (session != null) {
				session.write(protocol);
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 * @return
	 */
	public boolean sendMessage(Object message) {
		try {
			if (session != null) {
				session.write(message);
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 会话开启
	 * 
	 * @param session
	 */
	protected boolean onOpened(HawkMQServer mqServer, IoSession session) {
		if (this.session == null) {
			// 设置读取数据的缓冲区大小
			session.getConfig().setReadBufferSize(BUFFER_SIZE);
			// 绑定本地会话对象
			session.setAttribute(HawkMQSession.SESSION_ATTR, this);
			// 设置连接超时
			session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE_TIMEOUT);
			// 保存会话
			this.session = session;
			this.mqServer = mqServer;
			// 通知会话开启
			return mqServer.onSessionOpened(this);
		}
		return false;
	}

	/**
	 * 会话接收到消息
	 * 
	 * @param message
	 */
	protected boolean onReceived(Object message) {
		if (message instanceof HawkProtocol) {
			HawkProtocol protocol = (HawkProtocol) message;
			return mqServer.onSessionProtocol(this, protocol);
		}
		return false;
	}

	/**
	 * 会话空闲
	 */
	protected boolean onIdle(IdleStatus status) {
		// 心跳停止, 关闭会话
		if (session.getConfig().getBothIdleTimeInMillis() > 0 && status.equals(IdleStatus.READER_IDLE)) {
			HawkLog.logPrintln(String.format("session idle closed, ipaddr: %s, status: %s", getIpAddr(), status.toString()));
			close(false);
		}
		return true;
	}
	
	/**
	 * 会话被关闭
	 */
	protected boolean onClosed() {
		if (session != null) {
			mqServer.onSessionClosed(this);
			session = null;
		}
		return true;
	}
}
