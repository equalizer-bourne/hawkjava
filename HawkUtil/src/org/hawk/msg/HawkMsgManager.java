package org.hawk.msg;

import org.hawk.cache.HawkCache;
import org.hawk.xid.HawkXID;

/**
 * 消息管理器
 * 
 * @author hawk
 * 
 */
public class HawkMsgManager {
	/**
	 * 消息缓存
	 */
	private HawkCache msgCache;
	/**
	 * 单例使用
	 */
	static HawkMsgManager instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkMsgManager getInstance() {
		if (instance == null) {
			instance = new HawkMsgManager();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private HawkMsgManager() {
		msgCache = new HawkCache(new HawkMsg(0));
	}

	/**
	 * 创建消息对象
	 * 
	 * @param msg
	 * @return
	 */
	public HawkMsg create(int msg) {
		HawkMsg hawkMsg = null;
		if (msgCache != null) {
			hawkMsg = msgCache.create();
			hawkMsg.setMsg(msg);
		} else {
			hawkMsg = new HawkMsg(msg);
		}
		return hawkMsg;
	}

	/**
	 * 创建消息对象
	 * 
	 * @param msg
	 * @return
	 */
	public HawkMsg create(int msg, HawkXID target) {
		HawkMsg hawkMsg = null;
		if (msgCache != null) {
			hawkMsg = msgCache.create();
			hawkMsg.setMsg(msg);
			hawkMsg.setTarget(target);
		} else {
			hawkMsg = new HawkMsg(msg);
			hawkMsg.setTarget(target);
		}
		return hawkMsg;
	}

	/**
	 * 创建消息对象
	 * 
	 * @param msg
	 * @return
	 */
	public HawkMsg create(int msg, HawkXID target, HawkXID source) {
		HawkMsg hawkMsg = null;
		if (msgCache != null) {
			hawkMsg = msgCache.create();
			hawkMsg.setMsg(msg);
			hawkMsg.setTarget(target);
			hawkMsg.setSource(source);
		} else {
			hawkMsg = new HawkMsg(msg);
			hawkMsg.setTarget(target);
			hawkMsg.setSource(source);
		}
		return hawkMsg;
	}

	/**
	 * 释放消息到缓存
	 * 
	 * @param msg
	 */
	public void release(HawkMsg msg) {
		if (msgCache != null) {
			msgCache.release(msg);
		}
	}
}
