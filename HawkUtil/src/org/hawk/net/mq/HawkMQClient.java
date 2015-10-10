package org.hawk.net.mq;

import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

public abstract class HawkMQClient {
	class MQIoHandler extends IoHandlerAdapter {
		/**
		 * 开启回调
		 */
		@Override
		public void sessionOpened(IoSession session) throws Exception {
			try {
				HawkMQClient mqClient = (HawkMQClient) session.getAttribute(HawkMQSession.SESSION_ATTR);
				if (mqClient != null) {
					mqClient.onOpened();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		/**
		 * 消息接收回调
		 */
		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			try {
				HawkMQClient mqClient = (HawkMQClient) session.getAttribute(HawkMQSession.SESSION_ATTR);
				if (mqClient != null) {
					mqClient.onReceived(message);
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 异常回调
		 */
		@Override
		public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
			// 异常即关闭会话
			try {
				session.close(false);
			} catch (Exception e) {
			}
		}

		/**
		 * 关闭回调
		 */
		@Override
		public void sessionClosed(IoSession session) throws Exception {
			try {
				HawkMQClient mqClient = (HawkMQClient) session.getAttribute(HawkSession.SESSION_ATTR);
				if (mqClient != null) {
					mqClient.onClosed();
				}
			} catch (Exception e) {
			}
		}
	}
	
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
	 * 连接器
	 */
	protected IoConnector connector;
	/**
	 * 服务器地址
	 */
	protected InetSocketAddress address;
	
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
	 * 会话是否有效
	 * @return
	 */
	public boolean isValid() {
		return session != null;
	}
	
	/**
	 * 获取会话实体
	 * 
	 * @return
	 */
	public IoSession getIoSession() {
		return session;
	}

	/**
	 * 返回连接器
	 * 
	 * @return
	 */
	public IoConnector getConnector() {
		return connector;
	}
	
	/**
	 * 返回连接地址
	 * 
	 * @return
	 */
	public InetSocketAddress getAddress() {
		return address;
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
			// 创建连接地址
			address = new InetSocketAddress(ip, port);
			
			// 创建缓冲区
			inBuffer = IoBuffer.allocate(HawkMQSession.BUFFER_SIZE).setAutoExpand(true);
			outBuffer = IoBuffer.allocate(HawkMQSession.BUFFER_SIZE).setAutoExpand(true);
			
			// 创建连接器
			connector = new NioSocketConnector();
			OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);
			connector.getFilterChain().addLast("threadPool", new ExecutorFilter(executor));
			connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(HawkMQEncoder.class, HawkMQDecoder.class));
			connector.setHandler(new MQIoHandler());
			tryReconnect(timeoutMs);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 检测会话
	 * 
	 * @return
	 */
	public boolean tryReconnect(int timeoutMs) {
		if (!isValid()) {
			try {
				connector.setConnectTimeoutMillis(timeoutMs);
				// 连接到服务器
				ConnectFuture future = connector.connect(address);
				future.awaitUninterruptibly();
				session = future.getSession();
				if (session != null) {
					// 设置读取数据的缓冲区大小
					session.getConfig().setReadBufferSize(HawkMQSession.BUFFER_SIZE);
					// 绑定本地会话对象
					session.setAttribute(HawkMQSession.SESSION_ATTR, this);
				}
			} catch (Exception e) {
			}
		}
		return session != null;
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
	 * 发送数据
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
	 * 连接已打开
	 */
	protected void onOpened() {
		
	}

	/**
	 * 会话接收到消息, 外部需重载进行处理
	 * 
	 * @param message
	 */
	protected abstract boolean onReceived(Object message);
	
	
	/**
	 * 会话被关闭, 由系统内部调用
	 */
	protected void onClosed() {
		session = null;
	}
}
