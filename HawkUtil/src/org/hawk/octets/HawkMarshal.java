package org.hawk.octets;

import org.hawk.os.HawkException;

/**
 * 数据序列化和反序列化
 * 
 * @author hawk
 * 
 */
public abstract class HawkMarshal {
	/**
	 * 序列化
	 * 
	 * @param stream
	 */
	public abstract void marshal(HawkOctetsStream stream);

	/**
	 * 反序列化
	 * 
	 * @param stream
	 * @throws HawkException
	 */
	public abstract void unmarshal(HawkOctetsStream stream) throws HawkException;

	/**
	 * 克隆一个对象
	 */
	@Override
	public abstract HawkMarshal clone();

	/**
	 * 清理对象数据
	 * 
	 * @return
	 */
	public boolean clear() {
		return true;
	}
}
