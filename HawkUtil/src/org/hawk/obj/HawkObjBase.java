package org.hawk.obj;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对象管理基础类
 * 
 * @author hawk
 * 
 * @param <ObjKey>
 * @param <ObjType>
 */
public class HawkObjBase<ObjKey, ObjType> {
	/**
	 * 对象键值
	 */
	private ObjKey key;
	/**
	 * 对象实体
	 */
	private ObjType impl;
	/**
	 * 容器锁
	 */
	private Lock lock;
	/**
	 * 对象标记状态
	 */
	private int flag;

	/**
	 * 对象状态枚举定义
	 * 
	 * @author hawk
	 * 
	 */
	private enum ObjFlag {
		OBJ_NONE, OBJ_ACTIVE, OBJ_LOCKED
	};

	/**
	 * 构造函数
	 */
	protected HawkObjBase() {
		lock = new ReentrantLock();
	}

	/**
	 * 获取对象键值
	 * 
	 * @return
	 */
	public ObjKey getObjKey() {
		return key;
	}

	/**
	 * 设置实体对象, 仅ObjManager可调用
	 * 
	 * @param key
	 * @param impl
	 */
	protected void setImpl(ObjKey key, ObjType impl) {
		this.key = key;
		this.impl = impl;
		if (this.impl != null) {
			setObjFlag(ObjFlag.OBJ_ACTIVE.ordinal());
		}
	}

	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public ObjType getImpl() {
		return impl;
	}

	/**
	 * 获取模板对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTmpl() {
		if (impl != null) {
			return (T) impl;
		}
		return null;
	}

	/**
	 * 释放实体对象, 仅ObjManager可调用
	 */
	protected void freeObj() {
		impl = null;
		clearObjFlag(ObjFlag.OBJ_ACTIVE.ordinal());
	}

	/**
	 * 对象锁定
	 * 
	 * @return
	 */
	public boolean lockObj() {
		if (lock != null) {
			lock.lock();
			setObjFlag(ObjFlag.OBJ_LOCKED.ordinal());
			return true;
		}
		return false;
	}

	/**
	 * 对象解锁
	 * 
	 * @return
	 */
	public boolean unlockObj() {
		if (lock != null) {
			clearObjFlag(ObjFlag.OBJ_LOCKED.ordinal());
			lock.unlock();
			return true;
		}
		return false;
	}

	/**
	 * 设置对象指定标记
	 * 
	 * @param flag
	 */
	protected void setObjFlag(int flag) {
		this.flag |= flag;
	}

	/**
	 * 清除对象指定标记
	 * 
	 * @param flag
	 */
	protected void clearObjFlag(int flag) {
		this.flag &= ~flag;
	}

	/**
	 * 查询对象指定标记
	 * 
	 * @param flag
	 * @return
	 */
	protected boolean hasObjFlag(int flag) {
		return (this.flag & flag) != 0;
	}

	/**
	 * 判断对象释放激活状态
	 * 
	 * @return
	 */
	protected boolean isObjActive() {
		return hasObjFlag(ObjFlag.OBJ_ACTIVE.ordinal());
	}

	/**
	 * 判断对象释放锁定状态
	 * 
	 * @return
	 */
	protected boolean isObjLocked() {
		return hasObjFlag(ObjFlag.OBJ_LOCKED.ordinal());
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @return
	 */
	protected boolean isObjEmpty() {
		if (impl != null || hasObjFlag(ObjFlag.OBJ_ACTIVE.ordinal()) || hasObjFlag(ObjFlag.OBJ_LOCKED.ordinal())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断对象是否有效
	 * 
	 * @return
	 */
	public boolean isObjValid() {
		if (impl != null) {
			return isObjActive();
		}
		return false;
	}
}
