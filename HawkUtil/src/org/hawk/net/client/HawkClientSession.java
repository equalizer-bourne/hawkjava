package org.hawk.net.client;

import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.hawk.cryption.HawkDecryption;
import org.hawk.cryption.HawkEncryption;
import org.hawk.log.HawkLog;
import org.hawk.net.HawkNetManager;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

/**
 * 客户端会话
 * 
 * @author hawk
 */
public abstract class HawkClientSession {
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
	 * 加密对象
	 */
	protected HawkEncryption encryption;
	/**
	 * 解密对象
	 */
	protected HawkDecryption decryption;

	/**
	 * 会话构造
	 */
	public HawkClientSession() {
	}

	/**
	 * 初始化客户端会话
	 * 
	 * @param ip
	 * @param port
	 * @param timeoutMs
	 * @return
	 */
	public boolean connect(String ip, int port, int timeoutMs) {
		try {
			IoConnector connector = HawkNetManager.getInstance().getConnector();

			connector.setConnectTimeoutMillis(timeoutMs);
			ConnectFuture future = connector.connect(new InetSocketAddress(ip, port));
			future.awaitUninterruptibly();
			session = future.getSession();
			if (session != null) {
				// 设置读取数据的缓冲区大小
				session.getConfig().setReadBufferSize(HawkNetManager.getInstance().getSessionBufSize());
				// 加解密组件
				if (HawkNetManager.getInstance().enableEncryption()) {
					setEncryption(new HawkEncryption());
					setDecryption(new HawkDecryption());
				}
				this.active = true;
				// 绑定本地会话对象
				this.session.setAttribute(HawkSession.SESSION_ATTR, this);
				// 创建缓冲区
				inBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
				outBuffer = IoBuffer.allocate(HawkNetManager.getInstance().getSessionBufSize()).setAutoExpand(true);
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
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
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		if (isActive() && protocol != null) {
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
	 * 发送数据
	 * 
	 * @param message
	 * @return
	 */
	public boolean sendMessage(Object message) {
		if (isActive() && message != null) {
			try {
				session.write(message);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		HawkLog.errPrintln(String.format("send message failed, active: %s", active?"true":"false"));
		return false;
	}
	
	/**
	 * 会话接收到消息, 外部可重载进行处理
	 * 
	 * @param message
	 */
	protected abstract boolean onReceived(Object message);

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
		this.encryption = null;
		this.decryption = null;
	}

	/**
	 * 连接已打开
	 */
	public void onOpened() {
	}

	/**
	 * 解码错误回调
	 */
	public void onDecodeError() {
		close();
	}

	/**
	 * 会话被关闭
	 */
	protected void onClosed() {
		if (this.session != null) {
			// 清理属性
			this.session.setAttribute(HawkSession.SESSION_ATTR, null);
			// 清理会话数据
			clear();
		}
	}
}
