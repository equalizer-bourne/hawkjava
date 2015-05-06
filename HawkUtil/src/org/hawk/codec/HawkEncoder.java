package org.hawk.codec;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.hawk.cryption.HawkEncryption;
import org.hawk.net.HawkNetStatistics;
import org.hawk.net.HawkSession;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.websocket.HawkHandShakeResponse;
import org.hawk.net.websocket.HawkWebSocketUtil;
import org.hawk.os.HawkException;

/**
 * 协议编码器
 * 
 * @author hawk
 */
public class HawkEncoder extends ProtocolEncoderAdapter {
	/**
	 * 开始编码输出
	 */
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput output) throws Exception {
		Object attrObject = session.getAttribute(HawkSession.SESSION_ATTR);
		// 协议编码
		IoBuffer buffer = null;
		if (attrObject instanceof HawkSession) {
			HawkSession hawkSession = (HawkSession) attrObject;
			if (hawkSession.isWebSession()) {
				buffer = encodeWebSocketProtocol(hawkSession, message);
			} else {
				buffer = encodeProtocol(hawkSession, message);
			}
		} else if (attrObject instanceof HawkClientSession) {
			buffer = encodeProtocol((HawkClientSession) attrObject, message);
		}
		
		if (buffer != null) {
			output.write(buffer);
			// 统计更新信息
			HawkNetStatistics.getInstance().onSendBytes(buffer.remaining());
		}
	}

	/**
	 * 服务器模式编码
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	private IoBuffer encodeProtocol(HawkSession session, Object message) throws Exception {
		if (message instanceof HawkProtocol) {			
			// 写会话锁定
			session.lock();
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			try {
				IoBuffer outBuffer = session.getOutBuffer();
				if (outBuffer != null) {
					outBuffer.clear();
					if (!protocol.encode(outBuffer)) {
						throw new HawkException("protocol encode failed");
					}
					outBuffer.flip();
		
					// 协议加密
					ByteBuffer byteBuffer = null;
					HawkEncryption encryption = session.getEncryption();
					if (encryption != null) {
						byteBuffer = encryption.update(outBuffer.buf());
					} else {
						byteBuffer = ByteBuffer.allocate(outBuffer.remaining()).order(outBuffer.order());
						byteBuffer.put(outBuffer.array(), outBuffer.position(), outBuffer.remaining());
						byteBuffer.flip();
					}			
					return IoBuffer.wrap(byteBuffer);
				}
			} finally {
				HawkProtocol.release(protocol);
				session.unlock();
			}
		}
		return encodeProtocol(message);
	}
	
	/**
	 * 服务器模式编码
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	private IoBuffer encodeWebSocketProtocol(HawkSession session, Object message) throws Exception {
		if (message instanceof HawkHandShakeResponse) {
			HawkHandShakeResponse response = (HawkHandShakeResponse) message;
			return response.getResponseBuffer();
		} else if (message instanceof HawkProtocol) {
			// 写会话锁定
			session.lock();
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			try {
				IoBuffer outBuffer = session.getOutBuffer();
				if (outBuffer != null) {
					outBuffer.clear();
					if (!protocol.encode(outBuffer)) {
						throw new HawkException("protocol encode failed");
					}
					outBuffer.flip();

					IoBuffer frameBuffer = HawkWebSocketUtil.encodeWebSocketDataFrameBuffer(outBuffer, session.isJsonWebSession());
					return frameBuffer;
				}
			} finally {
				HawkProtocol.release(protocol);
				session.unlock();
			}
		}
		
		IoBuffer encodeBuffer = encodeProtocol(message);
		return HawkWebSocketUtil.encodeWebSocketDataFrameBuffer(encodeBuffer, session.isJsonWebSession());
	}
	
	/**
	 * 客户端模式编码
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	private IoBuffer encodeProtocol(HawkClientSession session, Object message) throws Exception {
		if (message instanceof HawkProtocol) {
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			IoBuffer outBuffer = session.getOutBuffer();
			outBuffer.clear();
			if (!protocol.encode(outBuffer)) {
				throw new HawkException("protocol encode failed");
			}
			outBuffer.flip();

			// 协议加密
			ByteBuffer byteBuffer = null;
			HawkEncryption encryption = session.getEncryption();
			if (encryption != null) {
				byteBuffer = encryption.update(outBuffer.buf());
			} else {
				byteBuffer = ByteBuffer.allocate(outBuffer.remaining()).order(outBuffer.order());
				byteBuffer.put(outBuffer.array(), outBuffer.position(), outBuffer.remaining());
				byteBuffer.flip();
			}
			return IoBuffer.wrap(byteBuffer);
		}
		return encodeProtocol(message);
	}
	
	/**
	 * 非协议模式编码
	 * 
	 * @param message
	 * @return
	 * @throws HawkException
	 */
	private IoBuffer encodeProtocol(Object message) throws HawkException {
		if (message instanceof ByteBuffer){
			return IoBuffer.wrap((ByteBuffer) message);
		} else if (message instanceof String){
			return IoBuffer.wrap(((String) message).getBytes());
		} else if (message instanceof byte[]){
			return IoBuffer.wrap((byte[]) message);
		} else {
			throw new HawkException("protocol message illegality");
		}
	}
}
