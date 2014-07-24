package org.hawk.app;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.hawk.msg.HawkMsg;
import org.hawk.net.HawkSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

/**
 * 应用程序对象
 * 
 * @author hawk
 */
public class HawkAppObj {
	/**
	 * 应用对象唯一标识
	 */
	protected HawkXID objXid;
	/**
	 * 会话对象
	 */
	protected HawkSession session;
	/**
	 * 应用对象支持的模块对象
	 */
	protected Map<Integer, HawkObjModule> objModules;

	/**
	 * 应用对象构造
	 * 
	 * @param xid
	 */
	public HawkAppObj(HawkXID xid) {
		objModules = new TreeMap<Integer, HawkObjModule>();
		setXid(xid);
	}

	/**
	 * 获取对象Id
	 * 
	 * @return
	 */
	public HawkXID getXid() {
		return objXid;
	}

	/**
	 * 设置对象Id
	 * 
	 * @param xid
	 */
	public void setXid(HawkXID xid) {
		this.objXid = xid;
	}

	/**
	 * 获取对象所对应的会话
	 * 
	 * @return
	 */
	public HawkSession getSession() {
		return session;
	}

	/**
	 * 设置对象所对应的会话
	 * 
	 * @param session
	 */
	public void setSession(HawkSession session) {
		this.session = session;
	}

	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol) {
		if (session != null && session.isActive()) {
			return session.sendProtocol(protocol);
		}
		return false;
	}

	/**
	 * 获取指定Id的功能模块
	 * 
	 * @param moduleId
	 * @return
	 */
	public HawkObjModule getModule(int moduleId) {
		return objModules.get(moduleId);
	}

	/**
	 * 注册功能模块
	 * 
	 * @param moduleId
	 * @param objModule
	 */
	public void registerModule(int moduleId, HawkObjModule objModule) {
		objModules.put(moduleId, objModule);
	}

	/**
	 * 更新, 子类在处理自身逻辑后需要调用父类接口
	 * 
	 * @return
	 */
	public boolean onTick() {
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			entry.getValue().onTick();
		}
		return true;
	}

	/**
	 * 消息响应, 子类在处理自身逻辑后需要调用父类接口
	 * 
	 * @param msg
	 * @return
	 */
	public boolean onMessage(HawkMsg msg) {
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			if (entry.getValue().onMessage(msg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 协议响应, 子类在处理自身逻辑后需要调用父类接口
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean onProtocol(HawkProtocol protocol) {
		for (Entry<Integer, HawkObjModule> entry : objModules.entrySet()) {
			if (entry.getValue().onProtocol(protocol)) {
				return true;
			}
		}
		return false;
	}
}
