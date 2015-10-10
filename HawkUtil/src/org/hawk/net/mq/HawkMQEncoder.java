package org.hawk.net.mq;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

/**
 * 协议编码器
 * 
 * @author hawk
 */
public class HawkMQEncoder extends ProtocolEncoderAdapter {
	/**
	 * 开始编码输出
	 */
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput output) throws Exception {
		Object attrObject = session.getAttribute(HawkMQSession.SESSION_ATTR);
		// 协议编码
		IoBuffer buffer = null;
		if (attrObject instanceof HawkMQSession) {
			buffer = encodeProtocol((HawkMQSession) attrObject, message);
		} else if (attrObject instanceof HawkMQClient) {
			buffer = encodeProtocol((HawkMQClient) attrObject, message);
		}

		if (buffer != null) {
			output.write(buffer);
		}
	}

	/**
	 * 服务器模式编码
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	private IoBuffer encodeProtocol(HawkMQSession session, Object message) throws Exception {
		if (message instanceof HawkProtocol) {
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			IoBuffer outBuffer = session.getOutBuffer();
			if (outBuffer != null) {
				outBuffer.clear();
				if (!protocol.encode(outBuffer)) {
					throw new HawkException("protocol encode failed");
				}
				outBuffer.flip();

				ByteBuffer byteBuffer = ByteBuffer.allocate(outBuffer.remaining()).order(outBuffer.order());
				byteBuffer.put(outBuffer.array(), outBuffer.position(), outBuffer.remaining());
				byteBuffer.flip();
				return IoBuffer.wrap(byteBuffer);
			}
		}
		return encodeProtocol(message);
	}

	/**
	 * 客户端模式编码
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	private IoBuffer encodeProtocol(HawkMQClient session, Object message) throws Exception {
		if (message instanceof HawkProtocol) {
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			IoBuffer outBuffer = session.getOutBuffer();
			outBuffer.clear();
			if (!protocol.encode(outBuffer)) {
				throw new HawkException("protocol encode failed");
			}
			outBuffer.flip();

			ByteBuffer byteBuffer = ByteBuffer.allocate(outBuffer.remaining()).order(outBuffer.order());
			byteBuffer.put(outBuffer.array(), outBuffer.position(), outBuffer.remaining());
			byteBuffer.flip();
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
		if (message instanceof ByteBuffer) {
			return IoBuffer.wrap((ByteBuffer) message);
		} else if (message instanceof String) {
			return IoBuffer.wrap(((String) message).getBytes());
		} else if (message instanceof byte[]) {
			return IoBuffer.wrap((byte[]) message);
		} else {
			throw new HawkException("protocol message illegality");
		}
	}
}
