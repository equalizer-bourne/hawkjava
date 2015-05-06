package org.hawk.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.hawk.app.HawkApp;
import org.hawk.cryption.HawkDecryption;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.net.HawkNetStatistics;
import org.hawk.net.HawkSession;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.websocket.HawkWebSocketUtil;
import org.hawk.os.HawkException;

/**
 * 协议解码器
 * 
 * @author hawk
 */
public class HawkDecoder extends ProtocolDecoderAdapter {
	/**
	 * 协议解码
	 */
	@Override
	public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		// 通知统计信息
		HawkNetStatistics.getInstance().onRecvBytes(buffer.remaining());
		
		// 协议解码
		Object attrObject = session.getAttribute(HawkSession.SESSION_ATTR);
		if (attrObject instanceof HawkSession) {
			HawkSession hawkSession = (HawkSession) attrObject;
			if (HawkApp.getInstance().getAppCfg().isWebSocket()) {
				if (hawkSession.isWebSession()) {
					decodeWebSocketProtocol(hawkSession, buffer, output);
				} else {
					// http握手请求判定
					int pos = buffer.position();
					boolean handShake = HawkWebSocketUtil.responseWebSockeHandShake(hawkSession, buffer);
					if (!handShake && hawkSession.isActive()) {
						buffer.position(pos);
						decodeProtocol(hawkSession, buffer, output);
					}
					
					// 调试打印
					try {
						HawkLog.debugPrintln("websocket handshake: " + new String(buffer.array()));
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				} 
			} else {
				decodeProtocol(hawkSession, buffer, output);
			}
		} else if (attrObject instanceof HawkClientSession) {
			decodeProtocol((HawkClientSession) attrObject, buffer, output);
		}
	}

	/**
	 * 服务器接收到协议进行解码
	 * 
	 * @param session
	 * @param buffer
	 * @param output
	 * @throws Exception
	 */
	private void decodeProtocol(HawkSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		if (session != null) {
			IoBuffer inBuffer = session.getInBuffer();
			if (inBuffer != null) {
				try {
					// 输入缓冲区接收数据
					HawkDecryption decryption = session.getDecryption();
					if (decryption != null) {
						decryption.update(buffer.buf(), inBuffer.buf());
					} else {
						inBuffer.put(buffer);
					}
					inBuffer.flip();
					
					// 协议解码
					while (inBuffer.remaining() >= HawkProtocol.HEADER_SIZE) {
						// 协议解码
						HawkProtocol protocol = HawkProtocol.valueOf();
						// 绑定协议会话
						protocol.bindSession(session);
						// 协议解码
						if (!protocol.decode(inBuffer)) {
							break;
						}
						
						if (!HawkNativeApi.protocol(protocol.getType(), protocol.getSize(), protocol.getReserve(), protocol.getCrc())) {
							return;
						}
						
						// 解码成协议返回
						output.write(protocol);
					}
				} catch (Exception e) {
					// 协议解码异常
					session.onDecodeFailed();
					
					HawkException.catchException(e);
				}

				// 缓冲区整理
				int pos = inBuffer.position();
				int remaining = inBuffer.remaining();
				inBuffer.clear();
				if (remaining > 0) {
					inBuffer.put(inBuffer.array(), pos, remaining);
				}
			}
		}
	}
	
	/**
	 * 服务器接收到协议进行解码
	 * 
	 * @param session
	 * @param buffer
	 * @param output
	 * @throws Exception
	 */
	private void decodeWebSocketProtocol(HawkSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		if (session != null) {
			IoBuffer inBuffer = session.getInBuffer();
			if (inBuffer != null) {
				try {
					// 输入缓冲区接收数据
					inBuffer.put(buffer);
					inBuffer.flip();
					
					// 解析出帧数据
					IoBuffer frameBuffer = HawkWebSocketUtil.decodeWebSocketDataBuffer(inBuffer, session);
					
					// 协议解码
					while (frameBuffer != null && frameBuffer.remaining() >= HawkProtocol.HEADER_SIZE) {
						// 协议解码
						HawkProtocol protocol = HawkProtocol.valueOf();
						// 绑定协议会话
						protocol.bindSession(session);
						// 协议解码(支持文本模式和二进制模式)
						if (session.isJsonWebSession()) {
							if (!protocol.decodeFromJson(new String(frameBuffer.array(), 0, frameBuffer.remaining()))) {
								break;
							}
							frameBuffer.position(frameBuffer.limit());
						} else {
							if (!protocol.decode(frameBuffer)) {
								break;
							}
						}
						
						if (!HawkNativeApi.protocol(protocol.getType(), protocol.getSize(), protocol.getReserve(), protocol.getCrc())) {
							return;
						}
						
						// 解码成协议返回
						output.write(protocol);
					}
					
					if (inBuffer.hasRemaining() && HawkApp.getInstance().getAppCfg().isDebug()) {
						HawkLog.logPrintln("websocket decode buffer uncompleted");
					}
				} catch (Exception e) {
					// 协议解码异常
					session.onDecodeFailed();
					
					HawkException.catchException(e);
				}

				// 缓冲区整理
				int pos = inBuffer.position();
				int remaining = inBuffer.remaining();
				inBuffer.clear();
				if (remaining > 0) {
					inBuffer.put(inBuffer.array(), pos, remaining);
				}
			}
		}
	}
	
	/**
	 * 服务器接收到协议进行解码
	 * 
	 * @param session
	 * @param buffer
	 * @param output
	 * @throws Exception
	 */
	private void decodeProtocol(HawkClientSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		if (session != null) {
			IoBuffer inBuffer = session.getInBuffer();
			if (inBuffer != null) {
				try {
					// 输入缓冲区接收数据
					HawkDecryption decryption = session.getDecryption();
					if (decryption != null) {
						decryption.update(buffer.buf(), inBuffer.buf());
					} else {
						inBuffer.put(buffer);
					}
					inBuffer.flip();
	
					// 协议解码
					while (inBuffer.remaining() >= HawkProtocol.HEADER_SIZE) {
						// 协议解码
						HawkProtocol protocol = HawkProtocol.valueOf();
						if (!protocol.decode(inBuffer)) {
							break;
						}
						
						// 解码成协议返回
						output.write(protocol);
					}
				} catch (Exception e) {
					// 协议解码异常
					session.onDecodeError();
					
					HawkException.catchException(e);
				}

				// 缓冲区整理
				int pos = inBuffer.position();
				int remaining = inBuffer.remaining();
				inBuffer.clear();
				if (remaining > 0) {
					inBuffer.put(inBuffer.array(), pos, remaining);
				}
			}
		}
	}
}
