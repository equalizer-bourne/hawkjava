package org.hawk.config;

import org.hawk.os.HawkException;

/**
 * 不可变对象修改
 * 
 * @author hawk
 */
public class HawkConstable {
	private boolean constLock = false;

	/**
	 * 默认构造
	 */
	public HawkConstable() {
		constLock = false;
	}

	/**
	 * 构造
	 * 
	 * @param constLock
	 */
	public HawkConstable(boolean constLock) {
		this.constLock = constLock;
	}

	/**
	 * 可变检测
	 * 
	 * @return
	 * @throws HawkException
	 */
	public boolean constCheck() {
		if (constLock) {
			throw new RuntimeException("const object rejeck modification");
		}
		return true;
	}

	/**
	 * 锁定
	 * 
	 * @param constLock
	 * @return
	 */
	public void lockConst(boolean constLock) {
		this.constLock = constLock;
	}
}
