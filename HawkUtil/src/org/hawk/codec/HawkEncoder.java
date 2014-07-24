package org.hawk.codec;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.hawk.cryption.HawkEncryption;
import org.hawk.net.HawkSession;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
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
		if (attrObject instanceof HawkSession) {
			encodeProtocol((HawkSession) attrObject, message, output);
		} else if (attrObject instanceof HawkClientSession) {
			encodeProtocol((HawkClientSession) attrObject, message, output);
		}
	}

	/**
	 * 服务器模式编码
	 * 
	 * @param session
	 * @param message
	 * @param output
	 * @throws Exception
	 */
	private void encodeProtocol(HawkSession session, Object message, ProtocolEncoderOutput output) throws Exception {
		if (message instanceof HawkProtocol) {
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			IoBuffer outBuffer = session.getOutBuffer();
			outBuffer.clear();
			if (!protocol.encode(outBuffer)) {
				throw new HawkException("encode protocol error");
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
			output.write(IoBuffer.wrap(byteBuffer));
		} else {
			throw new HawkException("protocol message error");
		}
	}
	
	/**
	 * 客户端模式编码
	 * 
	 * @param session
	 * @param message
	 * @param output
	 * @throws Exception
	 */
	private void encodeProtocol(HawkClientSession session, Object message, ProtocolEncoderOutput output) throws Exception {
		if (message instanceof HawkProtocol) {
			// 协议编码
			HawkProtocol protocol = (HawkProtocol) message;
			IoBuffer outBuffer = session.getOutBuffer();
			outBuffer.clear();
			if (!protocol.encode(outBuffer)) {
				throw new HawkException("encode protocol error");
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
			output.write(IoBuffer.wrap(byteBuffer));
		} else {
			throw new HawkException("protocol message error");
		}
	}
}
