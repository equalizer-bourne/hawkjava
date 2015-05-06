package com.hawk.collector.zmq;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.HawkTickable;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.hawk.collector.CollectorServices;
import com.hawk.collector.handler.report.ReportDataHandler;
import com.hawk.collector.handler.report.ReportGoldInfoHandler;
import com.hawk.collector.handler.report.ReportLoginHandler;
import com.hawk.collector.handler.report.ReportRechargeHandler;
import com.hawk.collector.handler.report.ReportRegisterHandler;
import com.hawk.collector.handler.report.ReportServerInfoHandler;
import com.hawk.collector.handler.system.CreateGameRequestHandler;

public class CollectorZmqServer extends HawkTickable {
	/**
	 * 服务地址
	 */
	private String zmqAddr;
	/**
	 * 服务zmq
	 */
	private HawkZmq serviceZmq;
	/**
	 * 接收数据缓冲区
	 */
	private byte[] bytes = null;
	/**
	 * 线程池
	 */
	private HawkThreadPool threadPool;
	/**
	 * 数据库管理器单例对象
	 */
	static CollectorZmqServer instance;

	/**
	 * 获取数据库管理器单例对象
	 * 
	 * @return
	 */
	public static CollectorZmqServer getInstance() {
		if (instance == null) {
			instance = new CollectorZmqServer();
		}
		return instance;
	}

	/**
	 * 函数
	 */
	private CollectorZmqServer() {
	}

	/**
	 * 获取名字
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * 解析zmq投递参数
	 * 
	 * @param params
	 * @return
	 */
	public static Map<String, String> parseZmqParam(String params) {
		Map<String, String> paramMap = new HashMap<String, String>();
		try {
			if (params != null && params.length() > 0) {
				params = URLDecoder.decode(params, "UTF-8");
				HawkLog.logPrintln("ZmqReport: " + params);
				if (params != null) {
					String[] querys = params.split("&");
					for (String query : querys) {
						String[] pair = query.split("=");
						if (pair.length == 2) {
							paramMap.put(pair[0], pair[1]);
						}
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return paramMap;
	}
	
	/**
	 * 开启服务
	 */
	public boolean setup(String addr, int pool) {
		// 创建通用缓冲区
		bytes = new byte[1024 * 1024 * 1024];
		CollectorServices.getInstance().addTickable(this);

		this.zmqAddr = addr;
		serviceZmq = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.PULL);
		if (!serviceZmq.bind(addr)) {
			HawkLog.logPrintln("service zmq bind failed......");
			return false;
		}
		
		if (pool > 0) {
			threadPool = new HawkThreadPool(getClass().getSimpleName());
			if (!threadPool.initPool(pool)) {
				return false;
			}
			
			if (!threadPool.start()) {
				return false;
			}
			
			HawkLog.logPrintln("zmq service thread pool running......");
		}
		return true;
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		try {
			if (serviceZmq != null) {
				HawkZmqManager.getInstance().closeZmq(serviceZmq);
				serviceZmq = null;
			}
			
			if (threadPool != null) {
				threadPool.close(true);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取服务地址
	 * 
	 * @return
	 */
	public String getAddr() {
		return zmqAddr;
	}
	
	/**
	 * 获取服务端口
	 * 
	 * @return
	 */
	public int getPort() {
		if (zmqAddr != null && zmqAddr.length() > 0) {
			int pos = zmqAddr.lastIndexOf(":");
			if (pos > 0) {
				String port = zmqAddr.substring(pos + 1, zmqAddr.length());
				return Integer.valueOf(port);
			}
		}
		return 0;
	}
	/**
	 * 帧更新事件
	 */
	@Override
	public void onTick() {
		while (serviceZmq.pollEvent(HawkZmq.HZMQ_EVENT_READ, 0) > 0) {
			int recvSize = serviceZmq.recv(bytes, 0);
			if (recvSize > 0) {
				final String reportPath = new String(bytes, 0, recvSize);
				if (serviceZmq.isWaitRecv()) {
					recvSize = serviceZmq.recv(bytes, 0);
					if (recvSize > 0) {
						final String params = new String(bytes, 0, recvSize);
						if (threadPool == null) {
							doReport(reportPath, parseZmqParam(params));
						} else {
							threadPool.addTask(new HawkTask(true) {
								@Override
								protected int run() {
									doReport(reportPath, parseZmqParam(params));
									return 0;
								}
							});
						}
					}
				}
			}
		}
	}

	/**
	 * 数据上报操作
	 * @param reportPath
	 * @param params
	 */
	private void doReport(String reportPath, Map<String, String> params) {
		try {
			if (reportPath.equals("/report_register")) {
				ReportRegisterHandler.doReport(params);
			} else if (reportPath.equals("/report_login")) {
				ReportLoginHandler.doReport(params);
			} else if (reportPath.equals("/report_recharge")) {
				ReportRechargeHandler.doReport(params);
			} else if (reportPath.equals("/report_gold")) {
				ReportGoldInfoHandler.doReport(params);
			} else if (reportPath.equals("/report_server")) {
				ReportServerInfoHandler.doReport(params, "");
			} else if (reportPath.equals("/report_data")) {
				ReportDataHandler.doReport(params);
			} else if (reportPath.equals("/create_game")) {
				CreateGameRequestHandler.createGame(params);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
