package org.hawk.net.mq;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

/**
 * 协议解码器
 * 
 * @author hawk
 */
public class HawkMQDecoder extends ProtocolDecoderAdapter {
	/**
	 * 协议解码
	 */
	@Override
	public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		// 协议解码
		Object attrObject = session.getAttribute(HawkMQSession.SESSION_ATTR);
		if (attrObject instanceof HawkMQSession) {
			decodeProtocol((HawkMQSession) attrObject, buffer, output);
		} else if (attrObject instanceof HawkMQClient) {
			decodeProtocol((HawkMQClient) attrObject, buffer, output);
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
	private void decodeProtocol(HawkMQSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		if (session != null) {
			IoBuffer inBuffer = session.getInBuffer();
			if (inBuffer != null) {
				try {
					// 输入缓冲区接收数据
					inBuffer.put(buffer).flip();
					// 协议解码
					while (inBuffer.remaining() >= HawkProtocol.HEADER_SIZE) {
						// 协议解码
						HawkProtocol protocol = HawkProtocol.valueOf();
						// 协议解码
						if (!protocol.decode(inBuffer)) {
							break;
						}
						// 解码成协议返回
						output.write(protocol);
					}
				} catch (Exception e) {
					// 协议解码异常
					session.close(true);
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
	private void decodeProtocol(HawkMQClient session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
		if (session != null) {
			IoBuffer inBuffer = session.getInBuffer();
			if (inBuffer != null) {
				try {
					// 输入缓冲区接收数据
					inBuffer.put(buffer).flip();
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
					session.close(true);
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
