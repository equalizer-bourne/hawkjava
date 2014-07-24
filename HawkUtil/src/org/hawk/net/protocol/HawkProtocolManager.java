package org.hawk.net.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;

import com.google.protobuf.Parser;

/**
 * 协议管理器封装
 * 
 * @author xulinqs
 * 
 */
public class HawkProtocolManager {
	/**
	 * 单例使用
	 */
	static HawkProtocolManager instance;
	/**
	 * 默认空对象协议字节数组
	 */
	static byte[] defaultBytes = new byte[0];
	/**
	 * 协议解析器
	 */
	static Map<String, Parser<?>> parsers = null;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkProtocolManager getInstance() {
		if (instance == null) {
			instance = new HawkProtocolManager();
		}
		return instance;
	}

	/**
	 * 默认构造函数
	 */
	private HawkProtocolManager() {
		parsers = new ConcurrentHashMap<String, Parser<?>>();
	}

	/**
	 * 实际解析协议模板
	 * 
	 * @param protocol
	 * @param template
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T parseProtocol(HawkProtocol protocol, T template) {
		if (protocol != null && template != null) {
			try {
				Parser<T> parser = (Parser<T>) parsers.get(template.getClass().getName());
				if (parser == null) {
					parser = (Parser<T>) template.getClass().getField("PARSER").get(template);
					parsers.put(template.getClass().getName(), parser);
				}

				if (protocol.getSize() <= 0) {
					return parser.parseFrom(defaultBytes);
				}

				return parser.parseFrom(protocol.getOctets().getBuffer().array(), 0, protocol.getSize());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return null;
	}
}
