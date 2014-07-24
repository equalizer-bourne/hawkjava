package org.hawk.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.hawk.cryption.HawkDecryption;
import org.hawk.net.HawkSession;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
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
		Object attrObject = session.getAttribute(HawkSession.SESSION_ATTR);
		// 协议解码
		if (attrObject instanceof HawkSession) {
			decodeProtocol((HawkSession) attrObject, buffer, output);
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
						HawkProtocol protocol = new HawkProtocol();
						if (!protocol.decode(inBuffer)) {
							break;
						}
						
						// 绑定协议会话
						protocol.bindSession(session);
						
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
						HawkProtocol protocol = new HawkProtocol();
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
