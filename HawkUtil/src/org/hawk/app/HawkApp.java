package org.hawk.app;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.hawk.app.task.HawkTaskManager;
import org.hawk.app.task.HawkTickTask;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.HawkNetManager;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkShutdownHook;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScriptManager;
import org.hawk.service.HawkServiceManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.timer.HawkTimerManager;
import org.hawk.util.HawkTickable;
import org.hawk.xid.HawkXID;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

/**
 * 应用层封装
 * 
 * @author hawk
 */
public abstract class HawkApp extends HawkAppObj {
	/**
	 * 单例使用
	 */
	protected static HawkApp instance;
	/**
	 * 工作路径
	 */
	protected String workPath;
	/**
	 * 当前时间(毫秒, 用于初略的逻辑时间计算)
	 */
	protected long currentTime;
	/**
	 * 更新对象列表
	 */
	protected Map<HawkTickable, HawkTickable> tickables;
	/**
	 * 是否在运行中
	 */
	protected volatile boolean running;
	/**
	 * 是否退出循环
	 */
	protected volatile boolean breakLoop;
	/**
	 * 应用配置对象
	 */
	protected HawkAppCfg appCfg;
	/**
	 * 消息逻辑线程池
	 */
	protected HawkThreadPool msgExecutor;
	/**
	 * 任务逻辑线程池
	 */
	protected HawkThreadPool taskExecutor;
	/**
	 * 线程更新任务表
	 */
	protected Map<Integer, HawkTickTask> threadTickTasks;
	/**
	 * tick时使用xid线程分类表
	 */
	protected Map<Integer, List<HawkXID>> threadTickXids;
	/**
	 * 对象管理器
	 */
	protected Map<Integer, HawkObjManager<HawkXID, HawkAppObj>> objMans;
	/**
	 * 当前活跃会话列表
	 */
	protected Set<HawkSession> activeSessions;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkApp getInstance() {
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	public HawkApp(HawkXID xid) {
		super(xid);
		
		if (instance != null) {
			throw new RuntimeException("app instance exist");
		}
		instance = this;
		Thread.currentThread().setName("AppMain");
		
		// 初始化工作目录
		workPath = System.getProperty("user.dir") + File.separator;
		
		// 初始化系统对象
		tickables = new ConcurrentHashMap<HawkTickable, HawkTickable>();
		activeSessions = new ConcurrentHashSet<HawkSession>();
		threadTickTasks = new HashMap<Integer, HawkTickTask>();
		objMans = new HashMap<Integer, HawkObjManager<HawkXID, HawkAppObj>>();
	}

	/**
	 * 初始化框架
	 * 
	 * @param appCfg
	 * @return
	 */
	public boolean init(HawkAppCfg appCfg) {
		this.appCfg = appCfg;
		if (this.appCfg == null) {
			return false;
		}
		
		// 设置打印输出标记
		HawkLog.enableConsole(appCfg.console);
		// 设置系统时间偏移
		HawkTime.setMsOffset(appCfg.calendarOffset);
		// 初始化系统时间
		currentTime = HawkTime.getMillisecond();
		//打印系统信息
		HawkOSOperator.printOsEnv();
		// 开启关闭钩子
		HawkShutdownHook.getInstance().install();
		// 定时器管理器初始化
		HawkTimerManager.getInstance().init(false);
		
		// 脚本初始化
		if (appCfg.scriptXml != null && appCfg.scriptXml.length() > 0) {
			if (!HawkScriptManager.getInstance().init(appCfg.scriptXml)) {
				return false;
			}
		}

		// 添加库加载目录
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");
		
		// 初始化zmq管理器
		HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);
				
		// 初始化配置
		if (appCfg.configPackages != null && appCfg.configPackages.length() > 0) {
			if (!HawkConfigManager.getInstance().init(appCfg.configPackages)) {
				return false;
			}
		}

		// 初始化service管理对象
		HawkServiceManager.getInstance().init(appCfg.servicePath);

		// 初始化数据库连接
		if (appCfg.dbHbmXml != null && appCfg.dbConnUrl != null && appCfg.dbUserName != null && appCfg.dbPassWord != null) {
			if (!HawkDBManager.getInstance().init(appCfg.dbHbmXml, appCfg.dbConnUrl, appCfg.dbUserName, appCfg.dbPassWord, appCfg.entityPackages)) {
				return false;
			}

			// 开启数据库异步落地
			if (appCfg.dbAsyncPeriod > 0) {
				HawkDBManager.getInstance().startAsyncThread(appCfg.dbAsyncPeriod);
			}
		}

		// 开启消息线程池
		if (msgExecutor == null && appCfg.threadNum > 0) {
			msgExecutor = new HawkThreadPool("MsgExecutor");
			if (!msgExecutor.initPool(appCfg.threadNum) || !msgExecutor.start()) {
				HawkLog.errPrintln(String.format("init msgExecutor failed, threadNum: %d", appCfg.threadNum));
				return false;
			}
		}

		// 开启任务线程池
		if (taskExecutor == null) {
			taskExecutor = new HawkThreadPool("TaskExecutor");
			int threadNum = appCfg.threadNum > 0 ? appCfg.threadNum : Runtime.getRuntime().availableProcessors();
			if (!taskExecutor.initPool(threadNum) || !taskExecutor.start()) {
				HawkLog.errPrintln(String.format("init taskExecutor failed, threadNum: %d", threadNum));
				return false;
			}
		}

		// 初始化网络
		if (!HawkNetManager.getInstance().initFromAppCfg(appCfg)) {
			HawkLog.errPrintln("init network failed, port: " + appCfg.acceptorPort);
			return false;
		}

		return true;
	}

	/**
	 * 获取工作目录
	 * 
	 * @return
	 */
	public String getWorkPath() {
		return workPath;
	}

	/**
	 * 获取当前系统时间
	 * 
	 * @return
	 */
	public long getCurrentTime() {
		return currentTime;
	}

	/**
	 * 获取应用配置对象
	 * 
	 * @return
	 */
	public HawkAppCfg getAppCfg() {
		return appCfg;
	}

	/**
	 * 是否运行状态
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 通知退出循环
	 * 
	 * @return
	 */
	public boolean breakLoop() {
		breakLoop = true;
		return true;
	}

	/**
	 * 线程池线程数目
	 * 
	 * @return
	 */
	public int getThreadNum() {
		if (msgExecutor != null) {
			return msgExecutor.getThreadNum();
		}
		return 0;
	}

	/**
	 * 获取线程id列表
	 * 
	 * @return
	 */
	public Collection<Long> getThreadIds() {
		List<Long> threadIds = new LinkedList<Long>();
		for (int i = 0; msgExecutor != null && i < msgExecutor.getThreadNum(); i++) {
			threadIds.add(msgExecutor.getThreadId(i));
		}
		return threadIds;
	}

	/**
	 * 获取活跃会话集合
	 * 
	 * @return
	 */
	public Set<HawkSession> getActiveSessions() {
		return activeSessions;
	}

	/**
	 * 添加可tick对象
	 * 
	 * @param tickable
	 */
	public void addTickable(HawkTickable tickable) {
		tickables.put(tickable, tickable);
	}

	/**
	 * 移除tick对象
	 * 
	 * @param tickable
	 */
	public void removeTickable(HawkTickable tickable) {
		tickables.remove(tickable);
	}

	/**
	 * 是否为debug模式
	 * 
	 * @return
	 */
	public boolean isDebug() {
		return appCfg.isDebug;
	}

	/**
	 * 启动服务
	 * 
	 * @return
	 */
	public boolean run() {
		if (!running) {
			running = true;
			while (running && !breakLoop) {
				currentTime = HawkTime.getMillisecond();

				// 逻辑帧更新
				onTick();

				HawkOSOperator.osSleep(appCfg.tickPeriod);
			}

			running = false;
			onClosed();
			return true;
		}
		return false;
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		for (Entry<HawkTickable, HawkTickable> entry : tickables.entrySet()) {
			HawkTickable tickable = entry.getKey();
			if (tickable.isTickable()) {
				tickable.onTick();
			}
		}
		return super.onTick();
	}

	/**
	 * 程序被关闭时的回调
	 */
	public void onShutdown() {
		breakLoop();
	}

	/**
	 * 应用程序退出时回调
	 */
	protected void onClosed() {
		// 停止定时器管理器
		HawkTimerManager.getInstance().stop();
		
		// 关闭脚本管理器
		HawkScriptManager.getInstance().close();

		// 关闭网络
		HawkNetManager.getInstance().close();

		// 等待消息线程结束
		if (msgExecutor != null) {
			msgExecutor.close(true);
		}

		// 等待任务线程池结束
		if (taskExecutor != null) {
			taskExecutor.close(true);
		}

		// 关闭数据库, 数据落地
		HawkDBManager.getInstance().close();
	}

	/**
	 * 获取指定类型的管理器
	 * 
	 * @param type
	 * @return
	 */
	public HawkObjManager<HawkXID, HawkAppObj> getObjMan(int type) {
		return objMans.get(type);
	}

	/**
	 * 注册创建对象管理器
	 * 
	 * @param type
	 * @return
	 */
	protected HawkObjManager<HawkXID, HawkAppObj> createObjMan(int type) {
		HawkObjManager<HawkXID, HawkAppObj> objMan = getObjMan(type);
		if (objMan == null) {
			objMan = new HawkObjManager<HawkXID, HawkAppObj>();
			objMans.put(type, objMan);
		}
		return objMan;
	}

	/**
	 * 创建对象通用接口
	 * 
	 * @param xid
	 * @param sid
	 * @return
	 */
	public HawkAppObj createObj(HawkXID xid) {
		if (xid.isValid()) {
			// 获取管理器
			HawkObjManager<HawkXID, HawkAppObj> objMan = getObjMan(xid.getType());
			if (objMan == null) {
				HawkLog.errPrintln("objMan type nonentity: " + xid.getType());
				return null;
			}

			// 创建应用层对象
			HawkAppObj appObj = onCreateObj(xid);
			if (appObj != null) {
				// 添加到管理器容器
				if (objMan.allocObject(xid, appObj) != null) {
					return appObj;
				}
			}
		}
		HawkLog.errPrintln("create obj failed: " + xid);
		return null;
	}

	/**
	 * 应用层创建对象, 应用层必须重写此函数
	 * 
	 * @param xid
	 * @return
	 */
	protected abstract HawkAppObj onCreateObj(HawkXID xid);

	/**
	 * 查询指定id对象, 非线程安全
	 * 
	 * @param xid
	 * @return
	 */
	public HawkObjBase<HawkXID, HawkAppObj> queryObject(HawkXID xid) {
		if (xid != null && xid.isValid()) {
			HawkObjManager<HawkXID, HawkAppObj> objMan = objMans.get(xid.getType());
			if (objMan != null) {
				return objMan.queryObject(xid);
			}
		}
		return null;
	}

	/**
	 * 查询唯一id对象, 必须把返回对象进行unlockObj操作以避免不解锁
	 * 
	 * @param xid
	 * @return
	 */
	public HawkObjBase<HawkXID, HawkAppObj> lockObject(HawkXID xid) {
		if (xid != null && xid.isValid()) {
			HawkObjManager<HawkXID, HawkAppObj> objMan = objMans.get(xid.getType());
			if (objMan != null) {
				HawkObjBase<HawkXID, HawkAppObj> objBase = objMan.queryObject(xid);
				if (objBase != null) {
					objBase.lockObj();
					return objBase;
				}
			}
		}
		return null;
	}

	/**
	 * 销毁对象
	 * 
	 * @param xid
	 * @return
	 */
	public boolean deleteObj(HawkXID xid) {
		if (xid.isValid()) {
			// 获取管理器
			HawkObjManager<HawkXID, HawkAppObj> objMan = getObjMan(xid.getType());
			if (objMan != null) {
				objMan.freeObject(xid);
				return true;
			}
		}
		return false;
	}

	/**
	 * 投递通用型任务到线程池处理
	 * 
	 * @param task
	 * @return
	 */
	public boolean postCommonTask(HawkTask task) {
		if (running && task != null) {
			return taskExecutor.addTask(task);
		}
		return false;
	}

	/**
	 * 投递任务到消息线程组
	 * 
	 * @param task
	 * @param threadIdx
	 * @return
	 */
	public boolean postMsgTask(HawkTask task, int threadIdx) {
		if (running && task != null && msgExecutor != null) {
			return msgExecutor.addTask(task, Math.abs(threadIdx), false);
		}
		return false;
	}

	/**
	 * 接收到协议后投递到应用
	 * 
	 * @param xid
	 * @param protocol
	 * @return
	 */
	public boolean postProtocol(HawkXID xid, HawkProtocol protocol) {
		if (running && xid != null && protocol != null) {
			int threadIdx = xid.getHashThread(getThreadNum());
			return postMsgTask(HawkTaskManager.getInstance().createProtoTask(xid, protocol), threadIdx);
		}
		return false;
	}

	/**
	 * 向特定对象投递消息
	 * 
	 * @param xid
	 * @param msg
	 * @return
	 */
	public boolean postMsg(HawkXID xid, HawkMsg msg) {
		if (running && xid != null && msg != null) {
			msg.setTarget(xid);
			return postMsg(msg);
		}
		return false;
	}

	/**
	 * 直接投递消息
	 * 
	 * @param msg
	 * @return
	 */
	public boolean postMsg(HawkMsg msg) {
		if (running && msg != null) {
			int threadIdx = msg.getTarget().getHashThread(getThreadNum());
			return postMsgTask(HawkTaskManager.getInstance().createMsgTask(msg.getTarget(), msg), threadIdx);
		}
		return false;
	}

	/**
	 * 群发消息
	 * 
	 * @param xidList
	 * @param msg
	 * @return
	 */
	public boolean postMsg(Collection<HawkXID> xidList, HawkMsg msg) {
		if (running && xidList != null && xidList.size() > 0 && msg != null) {
			Map<Integer, List<HawkXID>> threadXidMap = new HashMap<Integer, List<HawkXID>>();
			// 计算xid列表所属线程
			for (HawkXID xid : xidList) {
				int threadIdx = xid.getHashThread(getThreadNum());
				List<HawkXID> threadXidList = threadXidMap.get(threadIdx);
				if (threadXidList == null) {
					threadXidList = new LinkedList<HawkXID>();
					threadXidMap.put(threadIdx, threadXidList);
				}
				threadXidList.add(xid);
			}

			// 按线程投递消息
			for (Map.Entry<Integer, List<HawkXID>> entry : threadXidMap.entrySet()) {
				postMsgTask(HawkTaskManager.getInstance().createMsgTask(entry.getValue(), msg), entry.getKey());
			}
			return true;
		}
		return false;
	}

	/**
	 * 提交更新, 只会在主线程调用
	 * 
	 * @param xidList
	 * @return
	 */
	public boolean postTick(Collection<HawkXID> xidList) {
		if (running && xidList != null && xidList.size() > 0) {
			// 先创建线程tick表
			if (threadTickXids == null) {
				threadTickXids = new HashMap<Integer, List<HawkXID>>();
				for (int i = 0; i < getThreadNum(); i++) {
					threadTickXids.put(i, new LinkedList<HawkXID>());
				}
			} else {
				for (int i = 0; i < getThreadNum(); i++) {
					threadTickXids.get(i).clear();
				}
			}

			if (xidList != null && xidList.size() > 0) {
				// 计算xid列表所属线程
				for (HawkXID xid : xidList) {
					int threadIdx = xid.getHashThread(getThreadNum());
					// app对象本身不参与线程tick更新计算, 本身的tick在主线程执行
					if (!xid.equals(this.objXid)) {
						threadTickXids.get(threadIdx).add(xid);
					}
				}

				// 按线程投递消息
				for (Map.Entry<Integer, List<HawkXID>> entry : threadTickXids.entrySet()) {
					// 不存在即创建
					HawkTickTask task = threadTickTasks.get(entry.getKey());
					if (task == null) {
						task = HawkTaskManager.getInstance().createTickTask();
						threadTickTasks.put(entry.getKey(), task);
					}

					// 取出线程任务投递
					if (task != null && task.lock()) {
						task.resetXids(entry.getValue());
						postMsgTask(task, entry.getKey());
					}
				}
			}
		}
		return true;
	}

	/**
	 * 广播消息
	 * 
	 * @param msg
	 * @param xidList
	 * @return
	 */
	public boolean broadcastMsg(HawkMsg msg, Collection<HawkXID> xidList) {
		if (running && msg != null && xidList != null) {
			return postMsg(xidList, msg);
		}
		return false;
	}

	/**
	 * 广播消息
	 * 
	 * @param msg
	 * @param objMan
	 * @return
	 */
	public boolean broadcastMsg(HawkMsg msg, HawkObjManager<HawkXID, HawkAppObj> objMan) {
		if (msg != null && objMan != null) {
			List<HawkXID> xidList = new LinkedList<HawkXID>();
			objMan.collectObjKey(xidList, null);
			return postMsg(xidList, msg);
		}
		return false;
	}

	/**
	 * 分发协议
	 * 
	 * @param xid
	 * @param protocol
	 * @return
	 */
	public boolean dispatchProto(HawkXID xid, HawkProtocol protocol) {
		if (xid != null && protocol != null) {
			if (xid.isValid()) {
				HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
				if (objBase != null) {
					try {
						if (objBase.isObjValid()) {
							return objBase.getImpl().onProtocol(protocol);
						}
					} finally {
						objBase.unlockObj();
					}
				}
			} else {
				return onProtocol(protocol);
			}
		}
		return false;
	}

	/**
	 * 分发消息
	 * 
	 * @param xid
	 * @param msg
	 * @return
	 */
	public boolean dispatchMsg(HawkXID xid, HawkMsg msg) {
		if (xid != null && msg != null) {
			if (xid.isValid()) {
				HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
				if (objBase != null) {
					try {
						if (objBase.isObjValid()) {
							return objBase.getImpl().onMessage(msg);
						}
					} finally {
						objBase.unlockObj();
					}
				}
			} else {
				return onMessage(msg);
			}
		}
		return false;
	}

	/**
	 * 分发更新事件
	 * 
	 * @param xid
	 * @return
	 */
	public boolean dispatchTick(HawkXID xid) {
		if (xid != null) {
			if (xid.isValid()) {
				HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
				if (objBase != null) {
					try {
						if (objBase.isObjValid()) {
							return objBase.getImpl().onTick();
						}
					} finally {
						objBase.unlockObj();
					}
				}
			} else {
				return onTick();
			}
		}
		return false;
	}

	/**
	 * 会话开启回调
	 * 
	 * @param session
	 */
	public boolean onSessionOpened(HawkSession session) {
		activeSessions.add(session);
		return true;
	}

	/**
	 * 会话协议回调, 由IO线程直接调用, 非线程安全
	 * 
	 * @param session
	 * @param protocol
	 * @return
	 */
	public boolean onSessionProtocol(HawkSession session, HawkProtocol protocol) {
		if (running && session != null && protocol != null && session.getAppObject() != null) {
			return postProtocol(session.getAppObject().getXid(), protocol);
		}
		return false;
	}

	/**
	 * 会话关闭回调
	 * 
	 * @param session
	 */
	public void onSessionClosed(HawkSession session) {
		activeSessions.remove(session);
	}
}
