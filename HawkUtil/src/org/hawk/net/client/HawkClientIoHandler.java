package org.hawk.net.client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.hawk.net.HawkNetManager;
import org.hawk.net.HawkSession;
import org.hawk.os.HawkException;

/**
 * 客户端io处理器
 * 
 * @author hawk
 */
public class HawkClientIoHandler extends IoHandlerAdapter {
	/**
	 * 开启回调
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// 读写通道无操作进入空闲状态
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, HawkNetManager.getInstance().getSessionIdleTime());
		try {
			HawkClientSession clientSession = (HawkClientSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (clientSession != null) {
				clientSession.onOpened();
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
			HawkClientSession clientSession = (HawkClientSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (clientSession != null) {
				clientSession.onReceived(message);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 空闲回调
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// 空闲即关闭会话
		if (session.getConfig().getBothIdleTimeInMillis() > 0) {
			session.close(false);
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
			HawkClientSession clientSession = (HawkClientSession) session.getAttribute(HawkSession.SESSION_ATTR);
			if (clientSession != null) {
				clientSession.onClosed();
			}
		} catch (Exception e) {
		}
	}
}
