package org.hawk.algorithm;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 读写交换列表
 * 
 * @author hawk
 * @param <ObjType>
 */
public class HawkRWSwapList<ObjType> {
	/**
	 * 对象队列
	 */
	Queue<ObjType>[] objList;
	/**
	 * 队列写索引
	 */
	volatile int readIndex;
	/**
	 * 队列写索引
	 */
	volatile int writeIndex;
	/**
	 * 索引锁
	 */
	private Lock indexLock;

	/**
	 * 构造
	 */
	@SuppressWarnings("unchecked")
	public HawkRWSwapList() {
		readIndex = 0;
		writeIndex = 0;
		indexLock = new ReentrantLock();
		objList = new Queue[] { new LinkedList<ObjType>(), new LinkedList<ObjType>() };
	}

	/**
	 * 放入对象进入写对象列表
	 * 
	 * @param object
	 */
	public void push(ObjType object) {
		indexLock.lock();
		try {
			objList[writeIndex].add(object);
		} finally {
			indexLock.unlock();
		}
	}

	/**
	 * 切换读写队列
	 */
	public Queue<ObjType> swap() {
		// 交换读写列表
		indexLock.lock();
		readIndex = writeIndex;
		try {
			writeIndex = 1 - writeIndex;
		} finally {
			indexLock.unlock();
		}
		return objList[readIndex];
	}
}
