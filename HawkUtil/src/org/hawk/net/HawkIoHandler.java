package org.hawk.net;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.hawk.log.HawkLog;

/**
 * mina对应的io处理句柄
 * 
 * @author hawk
 */
public class HawkIoHandler extends IoHandlerAdapter {
	/**
	 * 活跃会话数
	 */
	AtomicInteger activeSession = new AtomicInteger();

	/**
	 * ip用途定义
	 * 
	 * @author hawk
	 */
	public static class IpUsage {
		/**
		 * ip白名单
		 */
		public static int WHITE_IPTABLES = 1;
		/**
		 * ip黑名单
		 */
		public static int BLACK_IPTABLES = 1;
	}

	/**
	 * 用途
	 */
	protected int ipUsage = 0;

	/**
	 * 默认构造
	 */
	public HawkIoHandler() {
	}

	/**
	 * 带用途handler构造
	 * 
	 * @param usage
	 */
	public HawkIoHandler(int ipUsage) {
		this.ipUsage = ipUsage;
	}

	/**
	 * 设置用途
	 * 
	 * @param usage
	 */
	public void setIpUsage(int ipUsage) {
		ipUsage = this.ipUsage;
	}

	/**
	 * 会话创建
	 */
	@Override
	public void sessionCreated(IoSession session) {
	}

	/**
	 * 开启回调
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// 白名单校验
		if ((ipUsage & IpUsage.WHITE_IPTABLES) != 0) {
			String ip = session.getRemoteAddress().toString().split(":")[0].substring(1);
			if (!HawkNetManager.getInstance().checkWhiteIptables(ip)) {
				HawkLog.logPrintln(String.format("session closed by white iptables, ipaddr: %s", ip));
				session.close(false);
				return;
			}
		}

		// 黑名单校验
		if ((ipUsage & IpUsage.BLACK_IPTABLES) != 0) {
			String ip = session.getRemoteAddress().toString().split(":")[0].substring(1);
			if (HawkNetManager.getInstance().checkBlackIptables(ip)) {
				HawkLog.logPrintln(String.format("session closed by black iptables, ipaddr: %s", ip));
				session.close(false);
				return;
			}
		}

		try {
			HawkSession peerSession = new HawkSession();
			if (peerSession != null) {
				if (!peerSession.onOpened(session)) {
					session.close(false);
					return;
				}

				// 最大协议数控制
				if (HawkNetManager.getInstance().getSessionMaxSize() > 0 && activeSession.get() >= HawkNetManager.getInstance().getSessionMaxSize()) {
					session.close(false);
					return;
				}

				activeSession.incrementAndGet();

				String ipaddr = session.getRemoteAddress().toString().split(":")[0].substring(1);
				HawkLog.debugPrintln(String.format("session opened, ipaddr: %s, total: %d", ipaddr, activeSession.get()));
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
			HawkSession peerSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (peerSession != null) {
				peerSession.onReceived(message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 消息发送成功
	 */
	@Override
	public void messageSent(IoSession session, Object message) {
	}

	/**
	 * 空闲回调
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		try {
			HawkSession peerSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (peerSession != null) {
				peerSession.onIdle(status);
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
		activeSession.decrementAndGet();

		String ipaddr = session.getRemoteAddress().toString().split(":")[0].substring(1);
		HawkLog.debugPrintln(String.format("session closed, ipaddr: %s, total: %d", ipaddr, activeSession.get()));

		try {
			HawkSession peerSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (peerSession != null) {
				peerSession.onClosed();
			}
		} catch (Exception e) {
		}
	}
}
