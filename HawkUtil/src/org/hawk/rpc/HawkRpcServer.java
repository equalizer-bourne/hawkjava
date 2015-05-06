package org.hawk.rpc;

import java.util.Set;

import org.apache.mina.util.ConcurrentHashSet;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.util.HawkTickable;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.hawk.zmq.HawkZmqProxy;
import org.zeromq.ZMQ;

public class HawkRpcServer extends HawkTickable implements Runnable {
	/**
	 * 路由端
	 */
	HawkZmq router;
	/**
	 * 经销端
	 */
	HawkZmq dealer;
	/**
	 * 桥接对象
	 */
	HawkZmqProxy proxy;
	/**
	 * 工作者数量
	 */
	Set<HawkRpcWorker> workers;
	/**
	 * 代理线程
	 */
	Thread proxyThread;
	/**
	 * 运行状况
	 */
	volatile boolean running;
	/**
	 * 事件超时
	 */
	static int eventTimeout = 5;

	/**
	 * 初始化
	 * 
	 * @param routerAddr
	 * @param dealerAddr
	 * @return
	 */
	public boolean init(String routerAddr, String dealerAddr, boolean threadMode) {
		router = HawkZmqManager.getInstance().createZmq(ZMQ.ROUTER);
		if (!router.bind(routerAddr)) {
			HawkLog.errPrintln("rpc server bind router failed, addr: " + routerAddr);
			return false;
		}

		dealer = HawkZmqManager.getInstance().createZmq(ZMQ.DEALER);
		if (!dealer.bind(dealerAddr)) {
			HawkLog.errPrintln("rpc server bind dealer failed, addr: " + dealerAddr);
			return false;
		}

		proxy = new HawkZmqProxy(router, dealer, true);
		HawkLog.logPrintln(String.format("rpc server init success, router: %s, dealer: %s", routerAddr, dealerAddr));

		// 开启服务线程
		running = true;
		if (threadMode) {
			proxyThread = new Thread(this);
			proxyThread.start();
		}

		return true;
	}

	/**
	 * 关闭服务
	 */
	public void close() {
		running = false;
		if (proxyThread != null) {
			try {
				proxyThread.join();
			} catch (InterruptedException e) {
				HawkException.catchException(e);
			}
		}

		HawkZmqManager.getInstance().closeZmq(router);
		HawkZmqManager.getInstance().closeZmq(dealer);

		proxy.clear();
		router = null;
		dealer = null;
		proxy = null;
	}

	/**
	 * 新增工作者
	 * 
	 * @param worker
	 */
	public void addWorker(HawkRpcWorker worker) {
		if (workers == null) {
			workers = new ConcurrentHashSet<HawkRpcWorker>();
		}
		workers.add(worker);
	}

	/**
	 * 更新状态
	 */
	protected void update(int timeout) {
		HawkZmqManager.getInstance().proxyZmq(proxy, timeout, true);

		// 工作者处理信息
		if (workers != null) {
			for (HawkRpcWorker worker : workers) {
				if (worker.hasRequest(0)) {
					worker.response();
				}
			}
		}
	}

	/**
	 * 更新事件
	 */
	@Override
	public void onTick() {
		if (proxyThread == null) {
			update(0);
		}
	}

	/**
	 * 线程运行
	 */
	@Override
	public void run() {
		while (running) {
			update(eventTimeout);
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
