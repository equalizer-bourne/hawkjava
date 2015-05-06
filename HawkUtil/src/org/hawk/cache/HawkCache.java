package org.hawk.cache;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 对象缓存管理器(线程安全)
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
	 * 缓存容器
	 */
	Queue<HawkCacheObj> cache;

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
		this.cache = new ConcurrentLinkedQueue<HawkCacheObj>();

		// 预开辟
		for (int i = 0; i < count; i++) {
			HawkCacheObj obj = stub.clone();
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
		HawkCacheObj obj = cache.poll();
		if (obj == null) {
			obj = stub.clone();
		}
		return (T) obj;
	}

	/**
	 * 把用完的对象放回缓存
	 * 
	 * @param obj
	 */
	public void release(HawkCacheObj obj) {
		if (obj != null) {
			cache.add(obj);
		}
	}
}
