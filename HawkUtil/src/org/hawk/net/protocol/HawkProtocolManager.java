package org.hawk.net.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.protobuf.Parser;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * 协议管理器封装
 * 
 * @author xulinqs
 * 
 */
public class HawkProtocolManager {
	/**
	 * 日志记录器
	 */
	private static final Logger logger = LoggerFactory.getLogger("Protocol");
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
	 * 协议注册基础配置集合
	 */
	static Map<Integer, HawkPacket> packetStub;
	
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
		packetStub = new ConcurrentHashMap<Integer, HawkPacket>();
	}

	/**
	 * 创建一个协议对象
	 * @param protoType
	 * @return
	 */
	public final HawkPacket createPacket(int type){
		HawkPacket packet = packetStub.get(type);
		if(packet == null) {
			return null;	
		}
		return (HawkPacket)packet.clone();
	}
	
	/**
	 * 注册协议文件
	 * @param protoType
	 * @param protocol
	 * @return
	 */
	public final boolean registerPacket(int type, HawkPacket packet){
		if(packetStub.containsKey(type)) {
			HawkLog.errPrintln("register duplicate packet, type: " + type);
			return false;
		}
		packetStub.put(type, packet);
		return true;
	}
	
	/**
	 * 记录协议日志
	 * @param builder
	 */
	public void logProtocolBuilder(HawkProtocol protocol, Object builder) {
		if (builder != null && HawkApp.getInstance().isDebug()) {
			logger.info("protocol: {}, size: {}, crc: {}, pb: \r\n{}", new Object[] { protocol.getType(), protocol.getSize(), protocol.getCrc(), builder.toString()} ); 
		}
	}
	
	/**
	 * 实际解析协议模板
	 * 
	 * @param protocol
	 * @param template
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GeneratedMessage> T parseProtocol(HawkProtocol protocol, T template) {
		if (protocol != null && template != null) {
			try {
				T pbProtocol = template;
				Parser<T> parser = (Parser<T>) parsers.get(template.getClass().getName());
				if (parser == null) {
					parser = (Parser<T>) template.getClass().getField("PARSER").get(template);
					parsers.put(template.getClass().getName(), parser);
				}

				if (protocol.getSize() > 0) {
					pbProtocol = parser.parseFrom(protocol.getOctets().getBuffer().array(), 0, protocol.getSize());
				}
				
				logProtocolBuilder(protocol, pbProtocol);
				return pbProtocol;
			} catch (Exception e) {
				HawkException.catchException(e);
				// 抛出运行时异常
				throw new RuntimeException("protocol parse exception: " + protocol.getType());
			}
		}
		return null;
	}
	
	/**
	 * 实际解析协议模板
	 * 
	 * @param protocol
	 * @param template
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GeneratedMessage> T parseFromJson(HawkProtocol protocol, T template) {
		if (protocol != null && template != null) {
			try {
				T pbProtocol = template;
				if (protocol.getSize() > 0) {
					String pbJson = new String(protocol.getOctets().getBuffer().array(), 0, protocol.getSize());
					GeneratedMessage.Builder<?> builder = (Builder<?>) template.newBuilderForType();
					JsonFormat.merge(pbJson, builder);
					pbProtocol = (T) builder.build();
				}
				
				logProtocolBuilder(protocol, pbProtocol);
				return pbProtocol;
			} catch (Exception e) {
				HawkException.catchException(e);
				// 抛出运行时异常
				throw new RuntimeException("protocol parse exception: " + protocol.getType());
			}
		}
		return null;
	}
	
	/**
	 * 实际解析协议模板
	 * 
	 * @param protocol
	 * @return
	 */
	public HawkPacket parsePacket(HawkProtocol protocol) {
		if (protocol != null) {
			try {
				HawkPacket packet = createPacket(protocol.getType());
				if (packet != null) {
					packet.unmarshal(protocol.getOctets());
					return packet;
				}
				// 协议存根不存在
				throw new RuntimeException("packet stub illegal: " + protocol.getType());
				
			} catch (Exception e) {
				HawkException.catchException(e);
				// 抛出运行时异常
				throw new RuntimeException("packet parse exception: " + protocol.getType());
			}
		}
		return null;
	}
}
