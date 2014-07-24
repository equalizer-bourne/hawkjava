package org.hawk.app.task;

import org.hawk.app.HawkApp;
import org.hawk.cache.HawkCacheObj;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

/**
 * 协议类任务
 * 
 * @author hawk
 * 
 */
public class HawkProtoTask extends HawkTask {
	/**
	 * 对象id
	 */
	HawkXID xid;
	/**
	 * 协议对象
	 */
	HawkProtocol protocol;

	/**
	 * 构造函数
	 * 
	 * @param xid
	 * @param sid
	 * @param protocol
	 */
	protected HawkProtoTask(HawkXID xid, HawkProtocol protocol) {
		super(HawkTaskManager.TASK_PROTO);

		setParam(xid, protocol);
	}

	/**
	 * 设置任务参数
	 * 
	 * @param xid
	 * @param sid
	 * @param protocol
	 */
	public void setParam(HawkXID xid, HawkProtocol protocol) {
		this.xid = xid;
		this.protocol = protocol;
	}

	/**
	 * 执行协议任务
	 */
	@Override
	protected int run() {
		try {
			if (xid != null && xid.isValid() && protocol != null) {
				boolean dispatchOK = HawkApp.getInstance().dispatchProto(xid, protocol);
				if (!dispatchOK) {
					HawkLog.errPrintln("dispatch protocol failed, protocolId: " + protocol.getType());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 清理重置任务
	 */
	@Override
	protected boolean clear() {
		protocol = null;

		// 缓存线程任务
		HawkTaskManager.getInstance().releaseTask(this);
		return super.clear();
	}

	/**
	 * 克隆创建对象
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkProtoTask(HawkXID.nullXid(), null);
	}
}
