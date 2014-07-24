package org.hawk.app.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.cache.HawkCache;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

/**
 * app中使用的任务管理对象
 * 
 * @author hawk
 * 
 */
public class HawkTaskManager {
	/**
	 * 更新型任务
	 */
	public static final int TASK_TICK = 1;
	/**
	 * 消息型任务
	 */
	public static final int TASK_MSG = 2;
	/**
	 * 协议型任务
	 */
	public static final int TASK_PROTO = 3;
	/**
	 * 任务缓存数组
	 */
	private Map<Integer, HawkCache> taskCaches;

	/**
	 * 单例使用
	 */
	static HawkTaskManager instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkTaskManager getInstance() {
		if (instance == null) {
			instance = new HawkTaskManager();
		}
		return instance;
	}

	private HawkTaskManager() {
		taskCaches = new HashMap<Integer, HawkCache>();

		taskCaches.put(TASK_MSG, new HawkCache(new HawkMsgTask(HawkXID.nullXid(), null)));
		taskCaches.put(TASK_PROTO, new HawkCache(new HawkProtoTask(HawkXID.nullXid(), null)));
	}

	/**
	 * 创建更新任务的统一出口, 任务无变动, 直接用常驻对象, 不需要缓存
	 * 
	 * @param xid
	 * @return
	 */
	public HawkTickTask createTickTask() {
		HawkTickTask task = null;
		if (taskCaches != null && taskCaches.containsKey(TASK_TICK)) {
			task = taskCaches.get(TASK_TICK).create();
		} else {
			task = new HawkTickTask();
		}
		return task;
	}

	/**
	 * 创建协议任务的统一出口
	 * 
	 * @param xid
	 * @param sid
	 * @param protocol
	 * @return
	 */
	public HawkProtoTask createProtoTask(HawkXID xid, HawkProtocol protocol) {
		HawkProtoTask task = null;
		if (taskCaches != null && taskCaches.containsKey(TASK_PROTO)) {
			task = taskCaches.get(TASK_PROTO).create();
			task.setParam(xid, protocol);
		} else {
			task = new HawkProtoTask(xid, protocol);
		}
		return task;
	}

	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param xid
	 * @param msg
	 * @return
	 */
	public HawkMsgTask createMsgTask(HawkXID xid, HawkMsg msg) {
		HawkMsgTask task = null;
		if (taskCaches != null && taskCaches.containsKey(TASK_MSG)) {
			task = taskCaches.get(TASK_MSG).create();
			task.setParam(xid, msg);
		} else {
			task = new HawkMsgTask(xid, msg);
		}
		return task;
	}

	/**
	 * 创建消息任务的统一出口
	 * 
	 * @param xidList
	 * @param msg
	 * @return
	 */
	public HawkMsgTask createMsgTask(List<HawkXID> xidList, HawkMsg msg) {
		HawkMsgTask task = null;
		if (taskCaches != null && taskCaches.containsKey(TASK_MSG)) {
			task = taskCaches.get(TASK_MSG).create();
			task.setParam(xidList, msg);
		} else {
			task = new HawkMsgTask(xidList, msg);
		}
		return task;
	}

	/**
	 * 释放任务
	 * 
	 * @param task
	 */
	public boolean releaseTask(HawkTask task) {
		if (taskCaches != null && taskCaches.containsKey(task.getTaskType())) {
			taskCaches.get(task.getTaskType()).release(task);
			return true;
		}
		return false;
	}
}
