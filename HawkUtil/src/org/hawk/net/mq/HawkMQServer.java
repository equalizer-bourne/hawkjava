package org.hawk.net.mq;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

public class HawkMQServer {
	/**
	 * Io处理器
	 * 
	 * @author hawk
	 */
	class MQIoHandler extends IoHandlerAdapter {
		/**
		 * 所属服务器
		 */
		protected HawkMQServer mqServer;

		/**
		 * 默认构造
		 */
		public MQIoHandler(HawkMQServer mqServer) {
			this.mqServer = mqServer;
		}

		/**
		 * 会话创建
		 */
		@Override
		public void sessionCreated(IoSession session) {
			try {
				HawkMQSession mqSession = new HawkMQSession();
				if (!mqSession.onOpened(mqServer, session)) {
					session.close(false);
					return;
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 消息接收回调
		 */
		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			try {
				HawkMQSession mqSession = (HawkMQSession) session.getAttribute(HawkMQSession.SESSION_ATTR);
				if (mqSession != null && !mqSession.onReceived(message)) {
					session.close(true);
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 空闲回调
		 */
		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			try {
				HawkMQSession mqSession = (HawkMQSession) session.getAttribute(HawkMQSession.SESSION_ATTR);
				if (mqSession != null) {
					mqSession.onIdle(status);
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 异常回调
		 */
		@Override
		public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
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
				HawkMQSession mqSession = (HawkMQSession) session.getAttribute(HawkMQSession.SESSION_ATTR);
				if (mqSession != null) {
					mqSession.onClosed();
				}
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 网络接收器
	 */
	private NioSocketAcceptor acceptor;
	/**
	 * 会话队列
	 */
	private BlockingQueue<HawkMQSession> sessionQueue;
	
	/**
	 * 构造函数
	 */
	public HawkMQServer() {
	}
	
	/**
	 * 初始化网络, 开启接收器
	 * 
	 * @param port
	 * @return
	 */
	public boolean init(int port, int ioFilterChain) {
		try {
			if (acceptor == null) {
				// 服务端的实例
				acceptor = new NioSocketAcceptor();
				sessionQueue = new LinkedBlockingQueue<HawkMQSession>();
				// 地址重用
				acceptor.getSessionConfig().setReuseAddress(true);
				acceptor.getSessionConfig().setSoLinger(0);
				// 设置服务端的handler
				acceptor.setHandler(new MQIoHandler(this));
				// 设置读取数据的缓冲区大小
				acceptor.getSessionConfig().setReadBufferSize(HawkMQSession.BUFFER_SIZE);
				// 设置编码器&解码器
				acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(HawkMQEncoder.class, HawkMQDecoder.class));
				// 添加IoFilterChain线程池
				if (ioFilterChain > 0) {
					OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(ioFilterChain);
					acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executor));
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
	 * 会话开启回调
	 * 
	 * @param session
	 */
	protected boolean onSessionOpened(HawkMQSession session) {
		sessionQueue.add(session);
		return true;
	}

	/**
	 * 会话协议回调, 由IO线程直接调用, 非线程安全
	 * 
	 * @param session
	 * @param protocol
	 * @return
	 */
	protected boolean onSessionProtocol(HawkMQSession session, HawkProtocol protocol) {
		return true;
	}
	
	/**
	 * 会话关闭回调
	 * 
	 * @param session
	 */
	protected boolean onSessionClosed(HawkMQSession session) {
		sessionQueue.remove(session);
		return true;
	}
}
