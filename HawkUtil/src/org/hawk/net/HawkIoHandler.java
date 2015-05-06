package org.hawk.net;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

/**
 * mina对应的io处理句柄
 * 
 * @author hawk
 */
public class HawkIoHandler extends IoHandlerAdapter {
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
		this.ipUsage = ipUsage;
	}

	/**
	 * 会话创建
	 */
	@Override
	public void sessionCreated(IoSession session) {
		HawkNetStatistics.getInstance().onSessionCreated();
		// 获取ip信息
		String ipaddr = "0.0.0.0";
		try {
			ipaddr = session.getRemoteAddress().toString().split(":")[0].substring(1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 白名单校验
		if ((ipUsage & IpUsage.WHITE_IPTABLES) != 0) {
			if (!HawkNetManager.getInstance().checkWhiteIptables(ipaddr)) {
				HawkLog.logPrintln(String.format("session closed by white iptables, ipaddr: %s", ipaddr));
				session.close(false);
				return;
			}
		}

		// 黑名单校验
		if ((ipUsage & IpUsage.BLACK_IPTABLES) != 0) {
			if (HawkNetManager.getInstance().checkBlackIptables(ipaddr)) {
				HawkLog.logPrintln(String.format("session closed by black iptables, ipaddr: %s", ipaddr));
				session.close(false);
				return;
			}
		}

		try {
			HawkSession hawkSession = new HawkSession();
			if (hawkSession != null) {
				if (!hawkSession.onOpened(session)) {
					session.close(false);
					return;
				}

				// 最大会话数控制
				if (HawkNetManager.getInstance().getSessionMaxSize() > 0) {
					int curSession = HawkNetStatistics.getInstance().getCurSession();
					if (curSession >= HawkNetManager.getInstance().getSessionMaxSize()) {
						HawkLog.errPrintln(String.format("session maxsize limit, ipaddr: %s, total: %d", ipaddr, curSession));
						session.close(false);
						return;
					}
				}

				if (HawkApp.getInstance().getAppCfg().isDebug()) {
					HawkLog.logPrintln(String.format("session opened, ipaddr: %s, total: %d", ipaddr, HawkNetStatistics.getInstance().getCurSession()));
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 开启回调
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
	}

	/**
	 * 消息接收回调
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		try {
			HawkSession hawkSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (hawkSession != null) {
				// 系统协议提前处理
				if (message instanceof HawkProtocol) {
					HawkProtocol protocol = (HawkProtocol) message;
					if (HawkNetManager.getInstance().onSysProtocol(protocol)) {
						return;
					}
				}
				
				hawkSession.onReceived(message);
				// 通知接收到协议对象
				HawkNetStatistics.getInstance().onRecvProto();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 消息发送成功
	 */
	@Override
	public void messageSent(IoSession session, Object message) {
		// 通知已发送协议对象
		HawkNetStatistics.getInstance().onSendProto();
	}

	/**
	 * 空闲回调
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		try {
			HawkSession hawkSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (hawkSession != null) {
				hawkSession.onIdle(status);
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
		HawkNetStatistics.getInstance().onSessionClosed();
		
		if (HawkApp.getInstance().getAppCfg().isDebug()) {
			String ipaddr = session.getRemoteAddress().toString().split(":")[0].substring(1);
			HawkLog.logPrintln(String.format("session closed, ipaddr: %s, total: %d", ipaddr, HawkNetStatistics.getInstance().getCurSession()));
		}
		
		try {
			HawkSession hawkSession = (HawkSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (hawkSession != null) {
				hawkSession.onClosed();
			}
		} catch (Exception e) {
		}
	}
}
