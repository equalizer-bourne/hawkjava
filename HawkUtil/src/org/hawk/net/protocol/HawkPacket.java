package org.hawk.net.protocol;

import org.hawk.octets.HawkMarshal;

/**
 * 数据序列化和反序列化
 * @author hawk
 *
 */
public abstract class HawkPacket extends HawkMarshal {
	/**
	 * 协议id
	 */
	private int type;
	
	/**
	 * 构造函数
	 */
	public HawkPacket(int type) {
		this.type = type;
	}
	
	/**
	 * 获取协议id
	 * @return
	 */
	public int getType() {
		return type;
	}
}
