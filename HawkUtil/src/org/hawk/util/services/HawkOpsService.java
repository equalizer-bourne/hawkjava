package org.hawk.util.services;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkTickable;
import org.hawk.util.services.helper.HawkOpsServerInfo;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

public class HawkOpsService extends HawkTickable {
	/**
	 * 默认同步时间周期
	 */
	public final static int SYNC_PERIOD = 15000;

	/**
	 * 上次更新事件
	 */
	private long lastTickTime = 0;
	/**
	 * zmq对象
	 */
	private HawkZmq agentZmq = null;
	/**
	 * 服务器信息
	 */
	private HawkOpsServerInfo serverInfo = null;

	/**
	 * 实例对象
	 */
	private static HawkOpsService instance = null;

	/**
	 * 获取全局实例对象
	 * 
	 * @return
	 */
	public static HawkOpsService getInstance() {
		if (instance == null) {
			instance = new HawkOpsService();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private HawkOpsService() {
	}

	/**
	 * 初始化聊天终端服务
	 * 
	 * @param addr
	 * @return
	 */
	public boolean init(String addr, HawkOpsServerInfo serverInfo) {
		if (agentZmq == null) {
			agentZmq = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.PUSH);
			if (!agentZmq.connect(addr)) {
				HawkLog.logPrintln("ops service init failed: " + addr);
				agentZmq = null;
				return false;
			}
			this.serverInfo = serverInfo;
			HawkLog.logPrintln("ops service init success, addr: " + addr);
			HawkApp.getInstance().addTickable(this);
		}
		return true;
	}

	@Override
	public void onTick() {
		try {
			long curTime = HawkTime.getMillisecond();
			if (curTime > lastTickTime + SYNC_PERIOD) {
				lastTickTime = curTime;
				if (agentZmq != null && serverInfo != null) {
					agentZmq.send(serverInfo.toString().getBytes(), 0);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
