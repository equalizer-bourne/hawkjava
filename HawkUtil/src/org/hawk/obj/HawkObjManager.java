package org.hawk.obj;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
	 * 对象键值映射表定义
	 */
	Map<ObjKey, HawkObjBase<ObjKey, ObjType>> objBaseMap = null;
	/**
	 * 对象是否可锁定
	 */
	protected boolean lockable = true;
	/**
	 * 对象超时时间
	 */
	protected long objTimeout = 0;
	
	/**
	 * 对象过滤器, 外部继承必须实现传入Manager的构造函数
	   class Filter extends HawkObjManager.InfoFilter{
			public filter(HawkObjManager objMan) {
				// InfoFilter的宿主HawkObjManager才可调用自己的super
				objMan.super();
			}
		}
		
		// 使用方法
		HawkObjManager<Integer， Integer> objMan = new HawkObjManager<Integer, Integer>();
		Filter filter = new Filter(objMan);
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
	public HawkObjManager(boolean lockable) {
		this.lockable = lockable;
		this.objBaseMap = new ConcurrentHashMap<ObjKey, HawkObjBase<ObjKey, ObjType>>();
	}

	/**
	 * 设置对象超时时间
	 * @param objTimeout
	 */
	public void setObjTimeout(long objTimeout) {
		this.objTimeout = objTimeout;
	}
	
	/**
	 * 获取对象超时时间
	 * 
	 * @return
	 */
	public long getObjTimeout() {
		return objTimeout;
	}
	
	/**
	 * 清空对象表
	 * 
	 * @return
	 */
	public void clearObjMap() {
		objBaseMap.clear();
	}
	
	/**
	 * 获取对象表
	 * 
	 * @return
	 */
	public Map<ObjKey, HawkObjBase<ObjKey, ObjType>> getObjBaseMap() {
		return objBaseMap;
	}
	
	/**
	 * 开辟基础对象
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public synchronized HawkObjBase<ObjKey, ObjType> allocObject(ObjKey key, ObjType obj) {
		HawkObjBase<ObjKey, ObjType> objBase = objBaseMap.get(key);
		if (objBase != null) {
			HawkLog.errPrintln("objkey duplicate: " + key);
			objBase = null;
		} else {
			objBase = new HawkObjBase<ObjKey, ObjType>(lockable);
			objBase.setImpl(key, obj);
			objBaseMap.put(key, objBase);
		}
		return objBase;
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
		HawkObjBase<ObjKey, ObjType> objBase = objBaseMap.get(key);
		// key校验
		if (objBase != null && !objBase.getObjKey().equals(key)) {
			HawkLog.errPrintln("objkey error: " + key);
			objBase = null;
		}
		return objBase;
	}

	/**
	 * 释放实体对象
	 * 
	 * @param key
	 * @return
	 */
	public boolean freeObject(ObjKey key) {
		objBaseMap.remove(key);
		return true;
	}

	/**
	 * 收集对象键序列
	 * 
	 * @param filter
	 * @return
	 */
	public int collectObjKey(Collection<ObjKey> keys, ObjFilter filter) {
		for (Map.Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
			if (filter == null || filter.doFilter(entry.getValue().getImpl()))
				keys.add(entry.getKey());
		}
		return keys.size();
	}

	/**
	 * 收集对象值序列
	 * 
	 * @param filter
	 * @return
	 */
	public int collectObjValue(Collection<ObjType> vals, ObjFilter filter) {
		for (Map.Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
			if (filter == null || filter.doFilter(entry.getValue().getImpl()))
				vals.add(entry.getValue().getImpl());
		}
		return vals.size();
	}

	/**
	 * 收集对象符合条件的信息列表
	 * 
	 * @param infos
	 * @param filter
	 * @return
	 */
	public <T> int collectObjInfo(Collection<T> infos, InfoFilter<T> filter) {
		for (Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry : objBaseMap.entrySet()) {
			filter.doFilter(entry.getValue().getImpl(), infos);
		}
		return infos.size();
	}
	
	/**
	 * 移除超时对象
	 */
	public List<ObjType> removeTimeoutObj(long curSysTime) {
		List<ObjType> removeObjs = null;
		if (objTimeout > 0) {
			Iterator<Entry<ObjKey, HawkObjBase<ObjKey, ObjType>>> iterator = objBaseMap.entrySet().iterator();  
			while(iterator.hasNext()) {  
			    Entry<ObjKey, HawkObjBase<ObjKey, ObjType>> entry = iterator.next();  
			    if (curSysTime > entry.getValue().getVisitTime() + objTimeout) {
			    	if (removeObjs == null) {
			    		removeObjs = new LinkedList<ObjType>();
			    	}
			    	removeObjs.add(entry.getValue().getImpl());
			    	iterator.remove();
			    }
			}
		}
		return removeObjs;
	}
}
