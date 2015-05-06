package org.hawk.app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.util.ConcurrentHashSet;
import org.hawk.app.task.HawkMsgTask;
import org.hawk.app.task.HawkProtoTask;
import org.hawk.app.task.HawkTickTask;
import org.hawk.cache.HawkCache;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.intercept.HawkInterceptHandler;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.net.HawkNetManager;
import org.hawk.net.HawkNetStatistics;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkShutdownHook;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScriptManager;
import org.hawk.service.HawkServiceManager;
import org.hawk.shell.HawkShellExecutor;
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
	 * 循环退出状态
	 */
	private static int LOOP_BREAK = 0x0001;
	/**
	 * 循环关闭状态
	 */
	private static int LOOP_CLOSED = 0x0002;
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
	protected Set<HawkTickable> tickables;
	/**
	 * 是否在运行中
	 */
	protected volatile boolean running;
	/**
	 * 是否退出循环
	 */
	protected volatile int loopState;
	/**
	 * 上次清理对象事件
	 */
	protected long lastRemoveObjTime;
	/**
	 * 是否允许执行shell
	 */
	protected boolean shellEnable;
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
	 * 对象管理器
	 */
	protected Map<Integer, HawkObjManager<HawkXID, HawkAppObj>> objMans;
	/**
	 * 当前活跃会话列表
	 */
	protected Set<HawkSession> activeSessions;
	/**
	 * 对象id列表
	 */
	protected Collection<HawkXID> objXidList;
	/**
	 * 对象列表
	 */
	protected Collection<HawkAppObj> appObjList;
	/**
	 * tick时使用xid线程分类表
	 */
	protected Map<Integer, List<HawkXID>> threadTickXids;
	/**
	 * 拦截器
	 */
	protected Map<String, HawkInterceptHandler> interceptMap;
	
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
		loopState = LOOP_CLOSED;
		shellEnable = true;
		
		// 初始化系统对象
		appCfg = new HawkAppCfg();
		tickables = new ConcurrentHashSet<HawkTickable>();
		activeSessions = new ConcurrentHashSet<HawkSession>();
		interceptMap = new ConcurrentHashMap<String, HawkInterceptHandler>();
		objMans = new TreeMap<Integer, HawkObjManager<HawkXID, HawkAppObj>>();
		objXidList = new LinkedList<HawkXID>();
		appObjList = new LinkedList<HawkAppObj>();
		lastRemoveObjTime = HawkTime.getMillisecond();
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

		// 添加库加载目录		
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/hawk");
		new File(".").list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (dir.isDirectory() && name.endsWith("lib")) {
					HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/" + name);
					return true;
				}
				return false;
			}
		});
		
		try {
			// 初始化
			System.loadLibrary("hawk");
			if (!HawkNativeApi.initHawk()) {
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 设置打印输出标记
		HawkLog.enableConsole(appCfg.console);
		// 设置系统时间偏移
		HawkTime.setMsOffset(appCfg.calendarOffset);
		// 初始化系统时间
		currentTime = HawkTime.getMillisecond();
		// 打印系统信息
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

		// 对象缓存
		if (appCfg.objCache) {
			HawkProtocol.setCache(new HawkCache(HawkProtocol.valueOf()));
			HawkProtoTask.setCache(new HawkCache(HawkProtoTask.valueOf()));
		}
		
		// 初始化zmq管理器
		HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);

		// 添加网络统计到更新列表
		addTickable(HawkNetStatistics.getInstance());
		
		// 初始化配置
		if (appCfg.configPackages != null && appCfg.configPackages.length() > 0) {
			if (!HawkConfigManager.getInstance().init(appCfg.configPackages)) {
				System.err.println("----------------------------------------------------------------------");
				System.err.println("-------------config crashed, take weapon to fuck designer-------------");
				System.err.println("----------------------------------------------------------------------");
				return false;
			}
		}

		// 初始化service管理对象
		if (appCfg.servicePath != null && appCfg.servicePath.length() > 0) {
			if (!HawkServiceManager.getInstance().init(appCfg.servicePath)) {
				return false;
			}
		}
		
		// 开启消息线程池
		if (msgExecutor == null && appCfg.threadNum > 0 && appCfg.isMsgTaskMode()) {
			msgExecutor = new HawkThreadPool("MsgExecutor");
			if (!msgExecutor.initPool(appCfg.threadNum) || !msgExecutor.start()) {
				HawkLog.errPrintln(String.format("init msgExecutor failed, threadNum: %d", appCfg.threadNum));
				return false;
			}
			HawkLog.logPrintln(String.format("start msgExecutor, threadNum: %d", appCfg.threadNum));
		}

		// 开启任务线程池
		int taskThreadNum = appCfg.taskThreads;
		if (taskThreadNum <= 0) {
			taskThreadNum = appCfg.threadNum;
		}
		if (taskExecutor == null && taskThreadNum > 0) {
			taskExecutor = new HawkThreadPool("TaskExecutor");
			if (!taskExecutor.initPool(taskThreadNum) || !taskExecutor.start()) {
				HawkLog.errPrintln(String.format("init taskExecutor failed, threadNum: %d", taskThreadNum));
				return false;
			}
			HawkLog.logPrintln(String.format("start taskExecutor, threadNum: %d", taskThreadNum));
		}

		// 初始化数据库连接
		if (appCfg.dbHbmXml != null && appCfg.dbConnUrl != null && appCfg.dbUserName != null && appCfg.dbPassWord != null) {
			if (!HawkDBManager.getInstance().init(appCfg.dbHbmXml, appCfg.dbConnUrl, appCfg.dbUserName, appCfg.dbPassWord, appCfg.entityPackages)) {
				return false;
			}

			// 开启数据库异步落地
			if (appCfg.dbAsyncPeriod > 0) {
				int dbThreadNum = appCfg.dbThreads;
				if (dbThreadNum <= 0) {
					dbThreadNum = appCfg.threadNum;
				}
				
				if (dbThreadNum > 0) {
					HawkDBManager.getInstance().startAsyncThread(appCfg.dbAsyncPeriod, dbThreadNum);
					HawkLog.logPrintln(String.format("start dbExecutor, threadNum: %d", dbThreadNum));
				}
			}
		}
		
		// 自动脚本运行
		HawkScriptManager.getInstance().autoRunScript();
		
		return true;
	}

	/**
	 * 开启网络
	 * 
	 * @return
	 */
	public boolean startNetwork() {
		if (appCfg.acceptorPort > 0) {
			if (!HawkNetManager.getInstance().initFromAppCfg(appCfg)) {
				HawkLog.errPrintln("init network failed, port: " + appCfg.acceptorPort);
				return false;
			}
			
			HawkLog.logPrintln("start network, port: " + appCfg.acceptorPort);
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
	 * 设置应用配置对象
	 * 
	 * @return
	 */
	public void setAppCfg(HawkAppCfg appCfg) {
		this.appCfg = appCfg;
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
		loopState |= LOOP_BREAK;
		return true;
	}

	/**
	 * 开启或关闭shell命令执行权限
	 * 
	 * @param enable
	 */
	public void enableShell(boolean enable) {
		this.shellEnable = enable;
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
		
		if (taskExecutor != null) {
			return taskExecutor.getThreadNum();
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
	 * 获取tickable的集合
	 * @return
	 */
	public Set<HawkTickable> getTickables() {
		return tickables;
	}
	
	/**
	 * 添加可tick对象
	 * 
	 * @param tickable
	 */
	public void addTickable(HawkTickable tickable) {
		tickables.add(tickable);
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
	 * 移除tick对象
	 * 
	 * @param tickable
	 */
	public void removeTickable(String name) {
		Iterator<HawkTickable> iterator = tickables.iterator();
		while (iterator.hasNext()) {
			try {
				HawkTickable tickable = iterator.next();
				if (tickable != null && tickable.getName().equals(name)) {
					iterator.remove();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 清空tick对象
	 */
	public void clearTickable() {
		tickables.clear();
	}
	
	/**
	 * 添加拦截器
	 * @param name
	 * @param handler
	 */
	public void addInterceptHandler(String name, HawkInterceptHandler handler) {
		interceptMap.put(name, handler);
	}
	
	/**
	 * 获取拦截器
	 * @param name
	 * @param handler
	 */
	public HawkInterceptHandler getInterceptHandler(String name) {
		return interceptMap.get(name);
	}
	
	/**
	 * 移除拦截器
	 * @param name
	 */
	public void removeInterceptHandler(String name) {
		interceptMap.remove(name);
	}
	
	/**
	 * 清除拦截器
	 */
	public void clearInterceptHandler() {
		interceptMap.clear();
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
	 * 设置上次对象移除时间
	 * @param lastRemoveObjTime
	 */
	public void setLastRemoveObjTime(long lastRemoveObjTime) {
		this.lastRemoveObjTime = lastRemoveObjTime;
	}
	
	/**
	 * 启动服务
	 * 
	 * @return
	 */
	public boolean run() {
		if (!running) {
			// 检测网络是否开启
			if (appCfg.acceptorPort > 0 && HawkNetManager.getInstance().getAcceptor() == null) {
				if (!startNetwork()) {
					return false;
				}
			}
			
			// 设置状态
			running = true;
			loopState = 0;
			
			HawkLog.logPrintln("server running......");
			while (running && (loopState & LOOP_BREAK) == 0) {
				currentTime = HawkTime.getMillisecond();

				// 逻辑帧更新
				try {
					onTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				HawkOSOperator.osSleep(appCfg.tickPeriod);
			}			
			onClosed();
			running = false;
			HawkLog.logPrintln("hawk main loop exit");
			return true;
		}
		return false;
	}

	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {
		// 更新检测
		if (!HawkNativeApi.tickHawk()) {
			return false;
		}
		
		// tick对象的更新
		for (HawkTickable tickable : tickables) {
			if (tickable.isTickable()) {
				tickable.onTick();
			}
		}
		
		// 对象管理器的更新(每小时一个周期)
		if (currentTime - lastRemoveObjTime >= 3600000) {
			lastRemoveObjTime = currentTime;
			int removeCount = 0;
			for (Entry<Integer, HawkObjManager<HawkXID, HawkAppObj>> entry : objMans.entrySet()) {
				HawkObjManager<HawkXID, HawkAppObj> objMan = entry.getValue();
				if (objMan != null && objMan.getObjTimeout() > 0) {
					// 清理超时对象
					List<HawkAppObj> removeAppObjs = objMan.removeTimeoutObj(currentTime);
					if (removeAppObjs != null) {
						for (HawkAppObj appObj : removeAppObjs) {
							onRemoveTimeoutObj(appObj);
						}
						removeCount = removeAppObjs.size();
					}
					HawkLog.logPrintln(String.format("app remove timeout obj, manager: %d, count: %d", entry.getKey(), removeCount));
				}
			}
		}
		
		// 对象更新
		for (Entry<Integer, HawkObjManager<HawkXID, HawkAppObj>> entry : objMans.entrySet()) {
			HawkObjManager<HawkXID, HawkAppObj> objMan = entry.getValue();
			if (objMan != null) {
				if (appCfg.isMsgTaskMode()) {
					objXidList.clear();
					if (objMan.collectObjKey(objXidList, null) > 0) {
						postTick(objXidList);
					}
				} else {
					appObjList.clear();
					objMan.collectObjValue(appObjList, null);
					for (HawkAppObj appObj : appObjList) {
						if (appObj != this) {
							appObj.onTick();
						}
					}
				}
			}
		}
		
		return super.onTick();
	}

	/**
	 * 移除超时应用对象
	 * @param appObj
	 */
	protected void onRemoveTimeoutObj(HawkAppObj appObj) {
	}

	/**
	 * 处理shell命令, 不可手动调用, 由脚本管理器调用
	 * 
	 * @param params
	 */
	public String onShellCommand(String cmd, long timeout) {
		if (shellEnable && cmd != null && cmd.length() > 0) {
			String result = HawkShellExecutor.execute(cmd, timeout);
			HawkLog.logPrintln("shell command: " + cmd + "\r\n" + result);
			return result;
		}
		return null;
	}
	
	/**
	 * 程序被关闭时的回调
	 */
	public void onShutdown() {
		breakLoop();
		
		// 等待循环状态
		while ((loopState & LOOP_CLOSED) != LOOP_CLOSED) {
			HawkOSOperator.sleep();
		}
	}

	/**
	 * 应用程序退出时回调
	 */
	protected void onClosed() {
		try {
			try {
				// 关闭网络
				HawkNetManager.getInstance().close();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 停止定时器管理器
				HawkTimerManager.getInstance().stop();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 停止数据库
				HawkDBManager.getInstance().stop();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 等待消息线程结束
				if (msgExecutor != null) {
					msgExecutor.close(true);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 等待任务线程池结束
				if (taskExecutor != null) {
					taskExecutor.close(true);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 关闭脚本管理器
				HawkScriptManager.getInstance().close();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			try {
				// 关闭数据库
				HawkDBManager.getInstance().close();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
		} finally {
			// 设置关闭状态
			loopState |= LOOP_CLOSED;
		}
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
			objMan = new HawkObjManager<HawkXID, HawkAppObj>(appCfg.isMsgTaskMode());
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
	public HawkObjBase<HawkXID, HawkAppObj> createObj(HawkXID xid) {
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
				HawkObjBase<HawkXID, HawkAppObj> objBase = objMan.allocObject(xid, appObj);
				return objBase;
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
	protected HawkAppObj onCreateObj(HawkXID xid) {
		return null;
	}

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
	public boolean removeObj(HawkXID xid) {
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
		if (running && task != null && taskExecutor != null) {
			return taskExecutor.addTask(task);
		}
		return false;
	}

	/**
	 * 投递通用型任务到线程池处理
	 * 
	 * @param task
	 * @return
	 */
	public boolean postCommonTask(HawkTask task, int threadIdx) {
		if (running && task != null && taskExecutor != null) {
			return taskExecutor.addTask(task, Math.abs(threadIdx), false);
		}
		return false;
	}
	
	/**
	 * 投递任务到消息线程组
	 * 
	 * @param task
	 * @return
	 * @throws Exception 
	 */
	public boolean postMsgTask(HawkMsgTask task) {
		if (running && task != null) {
			int threadIdx = task.getXid().getHashThread(getThreadNum());
			return postMsgTask(task, threadIdx);
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
	protected boolean postMsgTask(HawkTask task, int threadIdx) {
		if (running && task != null) {
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
			if (appCfg.isMsgTaskMode()) {
				int threadIdx = xid.getHashThread(getThreadNum());
				return postMsgTask(HawkProtoTask.valueOf(xid, protocol), threadIdx);
			} else {
				return dispatchProto(xid, protocol);
			}
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
			if (appCfg.isMsgTaskMode()) {
				int threadIdx = msg.getTarget().getHashThread(getThreadNum());
				
				if (HawkApp.getInstance().getAppCfg().isDebug()) {
					HawkLog.logPrintln(String.format("post message: %d, target: %s, thread: %d", msg.getMsg(), msg.getTarget().toString(), threadIdx));
				}
				
				return postMsgTask(HawkMsgTask.valueOf(msg.getTarget(), msg), threadIdx);
			} else {
				return dispatchMsg(msg.getTarget(), msg);
			}
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
			if (appCfg.isMsgTaskMode()) {
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
					postMsgTask(HawkMsgTask.valueOf(entry.getValue(), msg), entry.getKey());
				}
			} else {
				for (HawkXID xid : xidList) {
					dispatchMsg(xid, msg);
				}
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
			if (appCfg.isMsgTaskMode()) {
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
						if (entry.getValue().size() > 0) {
							// 不存在即创建
							postMsgTask(HawkTickTask.valueOf(entry.getValue()), entry.getKey());
						}
					}
				}
			} else {
				for (HawkXID xid : xidList) {
					if (!xid.equals(this.objXid)) {
						dispatchTick(xid);
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
	 * 协议广播
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean broadcastProtocol(HawkProtocol protocol) {
		for (HawkSession session : activeSessions) {
			try {
				session.sendProtocol(protocol);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
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
					long beginTimeMs = HawkTime.getMillisecond();
					try {
						if (objBase.isObjValid()) {
							objBase.setVisitTime(currentTime);
							HawkInterceptHandler interceptHandler = getInterceptHandler(objBase.getImpl().getClass().getName());
							if (interceptHandler != null && interceptHandler.onProtocol(objBase.getImpl(), protocol)) {
								return true;
							}
							return objBase.getImpl().onProtocol(protocol);
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					} finally {
						objBase.unlockObj();
						long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
						if (costTimeMs > HawkApp.getInstance().getAppCfg().getProtoTimeout()) {
							HawkLog.logPrintln("protocol timeout, protocol: " + protocol.getType() + ", costtime: " + costTimeMs);
						}
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
	 * @throws Exception 
	 */
	public boolean dispatchMsg(HawkXID xid, HawkMsg msg) {
		if (xid != null && msg != null) {
			if (xid.isValid()) {
				if (HawkApp.getInstance().getAppCfg().isDebug()) {
					HawkLog.logPrintln(String.format("dispatch message: %d, target: %s", msg.getMsg(), xid.toString()));
				}
				
				HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
				if (objBase != null) {
					long beginTimeMs = HawkTime.getMillisecond();
					try {
						if (objBase.isObjValid()) {
							HawkInterceptHandler interceptHandler = getInterceptHandler(objBase.getImpl().getClass().getName());
							if (interceptHandler != null && interceptHandler.onMessage(objBase.getImpl(), msg)) {
								return true;
							}
							return objBase.getImpl().onMessage(msg);
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					} finally {
						objBase.unlockObj();
						long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
						if (costTimeMs > HawkApp.getInstance().getAppCfg().getProtoTimeout()) {
							HawkLog.logPrintln("message timeout, msg: " + msg.getMsg() + ", costtime: " + costTimeMs);
						}
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
	 * @throws Exception 
	 */
	public boolean dispatchTick(HawkXID xid) {
		if (xid != null) {
			if (xid.isValid()) {
				HawkObjBase<HawkXID, HawkAppObj> objBase = lockObject(xid);
				if (objBase != null) {
					try {
						if (objBase.isObjValid()) {
							HawkInterceptHandler interceptHandler = getInterceptHandler(objBase.getImpl().getClass().getName());
							if (interceptHandler != null && interceptHandler.onTick(objBase.getImpl())) {
								return true;
							}
							return objBase.getImpl().onTick();
						}
					} catch (Exception e) {
						HawkException.catchException(e);
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
			if (appCfg.isMsgTaskMode()) {
				return postProtocol(session.getAppObject().getXid(), protocol);
			} else {
				session.getAppObject().onProtocol(protocol);
				return true;
			}
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

	/**
	 * 检测是否成功
	 * 
	 * @return
	 */
	public boolean checkConfigData() {
		return true;
	}

	/**
	 * 报告异常信息(主要通过邮件)
	 * 
	 * @param e
	 */
	public void reportException(Exception e) {
	}
}
