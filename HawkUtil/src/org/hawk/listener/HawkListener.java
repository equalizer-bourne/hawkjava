package org.hawk.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.HawkMsgHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.protocol.HawkProtocolHandler;
import org.hawk.os.HawkException;

import com.google.protobuf.ProtocolMessageEnum;

/**
 * 消息&协议的监听器
 * 
 * @author hawk
 */
public class HawkListener {
	/**
	 * 注解方法
	 * @author hawk
	 */
	class AnnoMethods {
		/**
		 * 消息注解方法列表
		 */
		Map<Integer, Method> msgAnnoMethods;
		/**
		 * 协议注解方法列表
		 */
		Map<Integer, Method> protoAnnoMethods;
		
		/**
		 * 构造函数
		 */
		AnnoMethods() {
			msgAnnoMethods = new HashMap<Integer, Method>();
			protoAnnoMethods = new HashMap<Integer, Method>();
		}
		
		/**
		 * 判断是否监听消息
		 * 
		 * @param msg
		 */
		boolean isListenMsg(int msg) {
			return msgAnnoMethods.containsKey(msg);
		}

		/**
		 * 判断是否监听协议
		 * 
		 * @param proto
		 */
		boolean isListenProto(int proto) {
			return protoAnnoMethods.containsKey(proto);
		}
	}
	static Map<Class<?>, AnnoMethods> classListenAnnoMethods = new ConcurrentHashMap<Class<?>, AnnoMethods>();
	
	/**
	 * 监听消息列表
	 */
	protected Map<Integer, HawkMsgHandler> msgHandlers;
	/**
	 * 监听协议列表
	 */
	protected Map<Integer, HawkProtocolHandler> protoHandlers;

	/**
	 * 默认构造函数
	 */
	public HawkListener() {
		scanAnnotation();
		msgHandlers = new HashMap<Integer, HawkMsgHandler>();
		protoHandlers = new HashMap<Integer, HawkProtocolHandler>();
	}

	/**
	 * 注册消息监听
	 * 
	 * @param msg
	 */
	public void listenMsg(int msg) {
		if (this.msgHandlers.containsKey(msg)) {
			throw new RuntimeException("duplication listen message");
		}
		this.msgHandlers.put(msg, null);
	}

	/**
	 * 注册消息监听
	 * 
	 * @param msg
	 */
	public void listenMsg(ProtocolMessageEnum msg) {
		if (this.msgHandlers.containsKey(msg.getNumber())) {
			throw new RuntimeException("duplication listen message");
		}
		this.msgHandlers.put(msg.getNumber(), null);
	}

	/**
	 * 注册消息监听
	 * 
	 * @param msg
	 * @param handler
	 */
	public void listenMsg(int msg, HawkMsgHandler handler) {
		if (this.msgHandlers.containsKey(msg)) {
			throw new RuntimeException("duplication listen message");
		}
		this.msgHandlers.put(msg, handler);
	}

	/**
	 * 注册消息监听
	 * 
	 * @param msg
	 * @param handler
	 */
	public void listenMsg(ProtocolMessageEnum msg, HawkMsgHandler handler) {
		if (this.msgHandlers.containsKey(msg.getNumber())) {
			throw new RuntimeException("duplication listen message");
		}
		this.msgHandlers.put(msg.getNumber(), handler);
	}

	/**
	 * 注册协议监听
	 * 
	 * @param proto
	 */
	public void listenProto(int proto) {
		if (this.protoHandlers.containsKey(proto)) {
			throw new RuntimeException("duplication listen protocol");
		}
		this.protoHandlers.put(proto, null);
	}

	/**
	 * 注册协议监听
	 * 
	 * @param proto
	 */
	public void listenProto(ProtocolMessageEnum proto) {
		if (this.protoHandlers.containsKey(proto.getNumber())) {
			throw new RuntimeException("duplication listen protocol");
		}
		this.protoHandlers.put(proto.getNumber(), null);
	}

	/**
	 * 注册协议监听
	 * 
	 * @param proto
	 * @param handler
	 */
	public void listenProto(int proto, HawkProtocolHandler handler) {
		if (this.protoHandlers.containsKey(proto)) {
			throw new RuntimeException("duplication listen protocol");
		}
		this.protoHandlers.put(proto, handler);
	}

	/**
	 * 注册协议监听
	 * 
	 * @param proto
	 * @param handler
	 */
	public void listenProto(ProtocolMessageEnum proto, HawkProtocolHandler handler) {
		if (this.protoHandlers.containsKey(proto.getNumber())) {
			throw new RuntimeException("duplication listen protocol");
		}
		this.protoHandlers.put(proto.getNumber(), handler);
	}

	/**
	 * 判断是否监听消息
	 * 
	 * @param msg
	 */
	public boolean isListenMsg(int msg) {
		if (this.msgHandlers.containsKey(msg)) {
			return true;
		}
		
		AnnoMethods annoMethods = classListenAnnoMethods.get(this.getClass());
		if (annoMethods != null && annoMethods.isListenMsg(msg)) {
			return true;
		}

		return false;
	}

	/**
	 * 判断是否监听协议
	 * 
	 * @param proto
	 */
	public boolean isListenProto(int proto) {
		if (this.protoHandlers.containsKey(proto)) {
			return true;
		}
		
		AnnoMethods annoMethods = classListenAnnoMethods.get(this.getClass());
		if (annoMethods != null && annoMethods.isListenProto(proto)) {
			return true;
		}

		return false;
	}

	/**
	 * 获取消息处理句柄
	 * 
	 * @param msg
	 * @return
	 */
	public HawkMsgHandler getMsgHandler(int msg) {
		return this.msgHandlers.get(msg);
	}

	/**
	 * 获取消息处理句柄
	 * 
	 * @param msg
	 * @return
	 */
	public HawkMsgHandler getMsgHandler(ProtocolMessageEnum msg) {
		return this.msgHandlers.get(msg.getNumber());
	}

	/**
	 * 获取协议处理句柄
	 * 
	 * @param proto
	 * @return
	 */
	public HawkProtocolHandler getProtoHandler(int proto) {
		return this.protoHandlers.get(proto);
	}

	/**
	 * 获取协议处理句柄
	 * 
	 * @param proto
	 * @return
	 */
	public HawkProtocolHandler getProtoHandler(ProtocolMessageEnum proto) {
		return this.protoHandlers.get(proto.getNumber());
	}

	/**
	 * 协议和消息处理句柄注解扫描
	 */
	protected void scanAnnotation() {
		// 自身扫描
		if (!classListenAnnoMethods.containsKey(this.getClass())) {
			Method[] methods = this.getClass().getDeclaredMethods();
			AnnoMethods annoMethods = new AnnoMethods();
			for (Method method : methods) {
				try {
					// 方法是否带有协议处理注解
					if (method.isAnnotationPresent(ProtocolHandler.class)) {
						method.setAccessible(true);
						ProtocolHandler protocolAnnotation = method.getAnnotation(ProtocolHandler.class);
						if (protocolAnnotation.code() != null) {
							for (int code : protocolAnnotation.code()) {
								annoMethods.protoAnnoMethods.put(code, method);
							}
						}
					} 
					
					// 方法是否带有消息处理注解
					if (method.isAnnotationPresent(MessageHandler.class)) {
						method.setAccessible(true);
						MessageHandler messageAnnotation = method.getAnnotation(MessageHandler.class);
						if (messageAnnotation.code() != null) {
							for (int code : messageAnnotation.code()) {
								annoMethods.msgAnnoMethods.put(code, method);
							}
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			synchronized (classListenAnnoMethods) {
				if (!classListenAnnoMethods.containsKey(this.getClass())) {
					classListenAnnoMethods.put(this.getClass(), annoMethods);
				}
			}			
		}
	}

	/**
	 * 消息响应
	 * 
	 * @param appObj
	 * @param msg
	 * @return
	 */
	protected boolean invokeMessage(HawkAppObj appObj, HawkMsg msg) {
		try {
			HawkMsgHandler handler = getMsgHandler(msg.getMsg());
			if (handler != null) {
				handler.onMessage(appObj, msg);
				return true;
			} 
			
			// 消息采用注解模式调用
			AnnoMethods annoMethods = classListenAnnoMethods.get(this.getClass());
			if (annoMethods != null) {
				Method method = annoMethods.msgAnnoMethods.get(msg.getMsg());
				if (method != null) {
					method.invoke(this, msg);
					return true;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 协议响应
	 * 
	 * @param appObj
	 * @param protocol
	 * @return
	 */
	protected boolean invokeProtocol(HawkAppObj appObj, HawkProtocol protocol) {
		try {
			HawkProtocolHandler handler = getProtoHandler(protocol.getType());
			if (handler != null) {
				handler.onProtocol(appObj, protocol);
				return true;
			} 
			
			// 协议采用注解模式调用
			AnnoMethods annoMethods = classListenAnnoMethods.get(this.getClass());
			if (annoMethods != null) {
				Method method = annoMethods.protoAnnoMethods.get(protocol.getType());
				if (method != null) {
					method.invoke(this, protocol);
					return true;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
}
