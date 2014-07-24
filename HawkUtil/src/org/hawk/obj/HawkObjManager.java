package org.hawk.obj;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.hawk.log.HawkLog;

/**
 * 基础对象管理器
 * 
 * @author hawk
 * 
 * @param <ObjKey>
 * @param <ObjType>
 */
public class HawkObjManager<ObjKey, ObjType> {
	/**
	 * 表的读写锁
	 */
	ReentrantReadWriteLock objManLock = null;
	/**
	 * 无效基础对象
	 */
	HawkObjBase<ObjKey, ObjType> nullObjBase = null;
	/**
	 * 对象键值映射表定义
	 */
	Map<ObjKey, HawkObjBase<ObjKey, ObjType>> objBaseMap = null;
	/**
	 * 对象队列定义
	 */
	Deque<HawkObjBase<ObjKey, ObjType>> objBaseQueue = null;

	/**
	 * 对象过滤器, 外部继承必须实现传入Manager的构造函数
	   class AppFilter extends HawkObjManager.InfoFilter{
			public ABC(HawkObjManager objMan) {
				//InfoFilter的宿主HawkObjManager才可调用自己的super
				objMan.super();
			}
		}
		
		//使用方法
		HawkObjManager<Integer， Integer> objMan = new HawkObjManager<Integer, Integer>();
		ABC filter = new ABC(objMan);
	 * @author hawk
	 *
	 * @param <T>
	 */
	public abstract class ObjFilter {
		abstract boolean doFilter(ObjType obj);
	}

	/**
	 * 信息过滤器, 外部继承必须实现传入Manager的构造函数
	 * 
	 * @author hawk
	 * 
	 * @param <T>
	 */
	public abstract class InfoFilter<T> {
		abstract boolean doFilter(ObjType obj, Collection<T> infos);
	}

	/**
	 * 构造函数
	 */
	public HawkObjManager() {
		objManLock = new ReentrantReadWriteLock();
		nullObjBase = new HawkObjBase<ObjKey, ObjType>();
		objBaseMap = new HashMap<ObjKey, HawkObjBase<ObjKey, ObjType>>();
		objBaseQueue = new LinkedList<HawkObjBase<ObjKey, ObjType>>();
	}

	/**
	 * 开辟基础对象
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public HawkObjBase<ObjKey, ObjType> allocObject(ObjKey key, ObjType obj) {
		objManLock.writeLock().lock();
		HawkObjBase<ObjKey, ObjType> objBase = objBaseMap.get(key);
		try {
			if (objBase != null) {
				HawkLog.errPrintln("objkey duplicate: " + key);
				objBase = null;
			} else {
				objBase = mallocObjBase();
				if (objBase != null) {
					objBase.lockObj();
					objBase.setImpl(key, obj);
					objBase.unlockObj();
					objBaseMap.put(key, objBase);
				}
			}
			return objBase;
		} finally {
			objManLock.writeLock().unlock();
		}
	}

	/**
	 * 锁定对象, 必须调用unlockObject释放锁
	 * 
	 * @param key
	 * @return
	 */
	public HawkObjBase<ObjKey, ObjType> lockObject(ObjKey key) {
		HawkObjBase<ObjKey, ObjType> objBase = queryObject(key);
		if (objBase != null) {
			objBase.lockObj();
		}
		return null;
	}

	/**
	 * 解锁对象
	 * 
	 * @param objBase
	 * @return
	 */
	public boolean unlockObject(HawkObjBase<ObjKey, ObjType> objBase) {
		if (objBase != null) {
			objBase.unlockObj();
			return true;
		}
		return false;
	}

	/**
	 * 查询对象, 不建议应用层直接使用
	 * 
	 * @param key
	 * @return
	 */
	public HawkObjBase<ObjKey, ObjType> queryObject(ObjKey key) {
		objManLock.readLock().lock();
		try {
			HawkObjBase<ObjKey, ObjType> objBase = objBaseMap.get(key);
			// key校验
			if (objBase != null && !objBase.getObjKey().equals(key)) {
				HawkLog.errPrintln("objkey error: " + key);
				objBase = null;
			}
			return objBase;
		} finally {
			objManLock.readLock().unlock();
		}
	}

	/**
	 * 释放实体对象
	 * 
	 * @param key
	 * @return
	 */
	public boolean freeObject(ObjKey key) {
		objManLock.writeLock().lock();
		HawkObjBase<ObjKey, ObjType> objBase = null;
		try {
			objBase = objBaseMap.get(key);
			if (objBase != null) {
				// 状态检查
				if (!objBase.isObjActive()) {
					HawkLog.errPrintln("objkey inactive: " + key);
				}

				// key检查
				if (!objBase.getObjKey().equals(key)) {
					HawkLog.errPrintln("objkey error: " + key);
				}

				// 重置状态
				objBase.lockObj();
				objBase.freeObj();
				objBase.unlockObj();

				// 加入缓存
				objBaseMap.remove(key);
				objBaseQueue.push(objBase);

				return objBase != null;
			}
		} finally {
			objManLock.writeLock().unlock();
		}
		return false;
	}

	/**
	 * 分配基础对象
	 * 
	 * @return
	 */
	private HawkObjBase<ObjKey, ObjType> mallocObjBase() {
		HawkObjBase<ObjKey, ObjType> objBase = null;
		if (!objBaseQueue.isEmpty()) {
			Iterator<HawkObjBase<ObjKey, ObjType>> it = objBaseQueue.iterator();
			while (it.hasNext()) {
				objBase = it.next();
				if (objBase != null && objBase.isObjEmpty()) {
					it.remove();
					break;
				}
				objBase = null;
			}
		}

		if (objBase == null) {
			objBase = new HawkObjBase<ObjKey, ObjType>();
		}

		return objBase;
	}

	/**
	 * 获取无效基础对象
	 * 
	 * @return
	 */
	public HawkObjBase<ObjKey, ObjType> nullObjBase() {
		return nullObjBase;
	}

	/**
	 * 收集对象键序列
	 * 
	 * @param filter
	 * @return
	 */
	public int collectObjKey(Collection<ObjKey> keys, ObjFilter filter) {
		objManLock.readLock().lock();
		try {
			for (Map.Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
				if (filter == null || filter.doFilter(entry.getValue().getImpl()))
					keys.add(entry.getKey());
			}
			return keys.size();
		} finally {
			objManLock.readLock().unlock();
		}
	}

	/**
	 * 收集对象值序列
	 * 
	 * @param filter
	 * @return
	 */
	public int collectObjValue(Collection<ObjType> vals, ObjFilter filter) {
		objManLock.readLock().lock();
		try {
			for (Map.Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
				if (filter == null || filter.doFilter(entry.getValue().getImpl()))
					vals.add(entry.getValue().getImpl());
			}
			return vals.size();
		} finally {
			objManLock.readLock().unlock();
		}
	}

	/**
	 * 收集对象符合条件的信息列表
	 * 
	 * @param infos
	 * @param filter
	 * @return
	 */
	public <T> int collectObjInfo(Collection<T> infos, InfoFilter<T> filter) {
		objManLock.readLock().lock();
		try {
			for (Map.Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
				filter.doFilter(entry.getValue().getImpl(), infos);
			}
			return infos.size();
		} finally {
			objManLock.readLock().unlock();
		}
	}
}
