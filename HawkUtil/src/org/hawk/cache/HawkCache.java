package org.hawk.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对象缓存管理器
 * 
 * @author hawk
 * 
 */
public class HawkCache {
	/**
	 * 对象存根, 用作构造器
	 */
	HawkCacheObj stub;
	/**
	 * 容器锁
	 */
	ReentrantLock lock;
	/**
	 * 缓存容器
	 */
	List<HawkCacheObj> cache;

	/**
	 * 构造函数
	 * 
	 * @param stub
	 */
	public HawkCache(HawkCacheObj stub) {
		this(stub, 0);
	}

	/**
	 * stub为存根, count为预开辟个数
	 * 
	 * @param stub
	 * @param count
	 */
	public HawkCache(HawkCacheObj stub, int count) {
		this.stub = stub;
		this.lock = new ReentrantLock();
		this.cache = new LinkedList<HawkCacheObj>();

		// 预开辟
		for (int i = 0; i < count; i++) {
			HawkCacheObj obj = stub.clone();
			obj.cached = true;
			cache.add(obj);
		}
	}

	/**
	 * 从缓存中创建对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T create() {
		HawkCacheObj obj = null;
		lock.lock();
		try {
			if (cache.size() > 0) {
				obj = cache.remove(0);
			} else {
				obj = stub.clone();
			}
		} finally {
			lock.unlock();
		}

		// 清除缓存标记
		if (obj != null) {
			obj.cached = false;
		}

		return (T) obj;
	}

	/**
	 * 把用完的对象放回缓存
	 * 
	 * @param obj
	 */
	public void release(HawkCacheObj obj) {
		lock.lock();
		try {
			// 判断缓存标记, 避免多次缓存
			if (obj != null && !obj.cached) {
				obj.cached = true;
				cache.add(obj);
			}
		} finally {
			lock.unlock();
		}
	}
}
