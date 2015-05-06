package org.hawk.obj;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hawk.os.HawkTime;

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
	protected ObjKey key;
	/**
	 * 对象实体
	 */
	protected ObjType impl;
	/**
	 * 容器锁
	 */
	protected Lock lock;
	/**
	 * 上次访问时间
	 */
	protected volatile long visitTime;
	
	/**
	 * 构造函数
	 */
	protected HawkObjBase(boolean lockable) {
		visitTime = HawkTime.getMillisecond();
		if (lockable) {
			lock = new ReentrantLock();
		}
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
	 * 对象锁定
	 * 
	 * @return
	 */
	public boolean lockObj() {
		if (lock != null) {
			lock.lock();
		}
		return true;
	}

	/**
	 * 对象解锁
	 * 
	 * @return
	 */
	public boolean unlockObj() {
		if (lock != null) {
			lock.unlock();
		}
		return true;
	}

	/**
	 * 判断对象是否有效
	 * 
	 * @return
	 */
	public boolean isObjValid() {
		return impl != null;
	}
	
	/**
	 * 获取上次访问时间
	 * @return
	 */
	public long getVisitTime() {
		return visitTime;
	}

	/**
	 * 获取上次访问时间
	 * @param visitTime
	 */
	public void setVisitTime(long visitTime) {
		this.visitTime = visitTime;
	}
}
