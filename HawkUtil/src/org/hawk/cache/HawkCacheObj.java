package org.hawk.cache;

/**
 * 缓存对象基础类
 * 
 * @author hawk
 * 
 */
public abstract class HawkCacheObj {
	/**
	 * 是否在缓存总
	 */
	protected volatile boolean cached = false;

	/**
	 * 缓存对象数据清理接口
	 * 
	 * @return
	 */
	protected abstract boolean clear();

	/**
	 * 缓存对象克隆构造器
	 */
	protected abstract HawkCacheObj clone();
}
