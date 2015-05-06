package org.hawk.net.websocket;

import java.security.MessageDigest;

import org.apache.mina.core.buffer.IoBuffer;
import org.hawk.cryption.HawkBase64;
import org.hawk.net.HawkSession;
import org.hawk.os.HawkException;

public class HawkWebSocketUtil {
	// 帧类型定义
	public static int FRAME_TEXT = 1;
	public static int FRAME_BINARY = 2;
	public static int FRAME_CLOSE = 8;
	public static int FRAME_PING = 9;
	public static int FRAME_PONG = 10;
	  
	/**
	 * Construct a successful websocket handshake response using the key param (See RFC 6455).
	 * 
	 * @param key
	 * @return
	 */
	public static String getWebSocketHandshakeResponse(String key, String protocol) {
		String response = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n";
		response += "Upgrade: websocket\r\n";
		response += "Connection: Upgrade\r\n";
		if (protocol.length() > 0) {
			response += "Sec-WebSocket-Protocol: " + protocol + "\r\n";
		}
		response += "Sec-WebSocket-Accept: " + key + "\r\n";
		response += "\r\n";
		return response;
	}

	/**
	 * Parse the string as a websocket request and return the value from Sec-WebSocket-Key header (See RFC 6455). Return empty string if not found.
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientWebSocketRequestKey(String request) {
		String[] headers = request.split("\r\n");
		String socketKey = "";
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].contains("Sec-WebSocket-Key")) {
				socketKey = (headers[i].split(":")[1]).trim();
				break;
			}
		}
		return socketKey;
	}

	/**
	 * Parse the string as a websocket request and return the value from WebSocket-Protocol header (See RFC 6455). Return empty string if not found.
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientWebSocketProtocol(String request) {
		String[] headers = request.split("\r\n");
		String protocol = "";
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].contains("WebSocket-Protocol")) {
				protocol = (headers[i].split(":")[1]).trim();
				break;
			}
		}
		return protocol;
	}
	
	/**
	 * Parse the string as a websocket request and return the value from Origin header (See RFC 6455). Return empty string if not found.
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientWebSocketOrigin(String request) {
		String[] headers = request.split("\r\n");
		String origin = "";
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].contains("Origin")) {
				origin = (headers[i].split(":")[1]).trim();
				break;
			}
		}
		return origin;
	}
	
	/**
	 * Builds the challenge response to be used in WebSocket handshake. First append the challenge with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11" and then make a SHA1 hash and finally Base64 encode it. (See RFC 6455)
	 * 
	 * @param challenge
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String getWebSocketKeyChallengeResponse(String socketKey) {
		try {
			String challenge = socketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.reset();
			digest.update(challenge.getBytes("utf8"));
			byte[] hashedBytes = digest.digest();
			return HawkBase64.encode(hashedBytes);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "";
	}

	/**
	 * Encode the in buffer according to the Section 5.2. RFC 6455
	 * 
	 * @param buf
	 * @param jsonMode 文本模式
	 * @return
	 */
	public static IoBuffer encodeWebSocketDataFrameBuffer(IoBuffer buf, boolean jsonMode) {
		IoBuffer buffer = IoBuffer.allocate(buf.limit() + 4, false);
		buffer.setAutoExpand(true);
		if (jsonMode) {
			buffer.put((byte) 0x81);
		} else {
			buffer.put((byte) 0x82);
		}
		if (buf.limit() <= 125) {
			byte capacity = (byte) (buf.limit());
			buffer.put(capacity);
		} else {
			buffer.put((byte) 126);
			buffer.putShort((short) buf.limit());
		}
		buffer.put(buf);
		buffer.flip();
		return buffer;
	}

	/**
	 * Try parsing the message as a websocket handshake request. If it is such
	 * a request, then send the corresponding handshake response (as in Section 4.2.2 RFC 6455).
	 * 
	 * @param session
	 * @param inBuffer
	 * @return
	 */
	public static boolean responseWebSockeHandShake(HawkSession session, IoBuffer inBuffer) {
        try{
            String payLoadMsg = new String(inBuffer.array(), inBuffer.position(), inBuffer.remaining());
            // 设置数据全部被读取
            inBuffer.position(inBuffer.limit());
            String socketKey = getClientWebSocketRequestKey(payLoadMsg);
            if (socketKey.length() <= 0) {
            	return false;
            }
            String challenge = getWebSocketKeyChallengeResponse(socketKey);
            String protocol = getClientWebSocketProtocol(payLoadMsg);
            if (protocol.length() <= 0) {
            	return false;
            }
            String origin = getClientWebSocketOrigin(payLoadMsg);
            String response = getWebSocketHandshakeResponse(challenge, protocol);
            session.setWebSession(true);
            session.setWebSocketProtocol(protocol);
            session.setWebSocketOrigin(origin);
            session.sendMessage(new HawkHandShakeResponse(response));
            return true;
        } catch(Exception e){
        	session.close(true);
        	HawkException.catchException(e);
        	return false;
        }
    }
	 
	/**
	 * Decode the in buffer according to the Section 5.2. RFC 6455 If there are multiple websocket dataframes in the buffer, this will parse all and return one complete decoded buffer.
	 * 
	 * @param inBuffer
	 * @param session
	 * @return
	 */
	public static IoBuffer decodeWebSocketDataBuffer(IoBuffer inBuffer, HawkSession session) {
		IoBuffer frameBuffer = null;
		do {
			inBuffer.mark();

			byte frameInfo = inBuffer.get();
			byte opCode = (byte) (frameInfo & 0x0F);			
			if (opCode == FRAME_BINARY) {
				session.setWebSessionMode(HawkSession.BINARY_WEB_SESSION);
			} else if (opCode == FRAME_TEXT) {
				session.setWebSessionMode(HawkSession.JSON_WEB_SESSION);
			}
			
			// fetch frame data length
			byte payloadLen = inBuffer.get();
			int frameLen = (payloadLen & (byte) 0x7F);
			if (frameLen == 126) {
				frameLen = inBuffer.getShort();
			}

			// Validate if we have enough data in the buffer to completely
			// parse the WebSocket DataFrame. If not return null.
			boolean maskingKey = false;
			if ((payloadLen & 0x80) > 0) {
				maskingKey = true;
			}
			
			// Validate frame length
			if (maskingKey && inBuffer.remaining() < frameLen + 4) {
				inBuffer.reset();
				break;
			}

			// fetch buffer mask
			byte mask[] = new byte[4];
			if (maskingKey) {
				for (int i = 0; i < 4; i++) {
					mask[i] = inBuffer.get();
				}
			}
			
			/*
			 * now un-mask frameLen bytes as per Section 5.3 RFC 6455 Octet i of the transformed data ("transformed-octet-i") is the XOR of octet i of the original data ("original-octet-i") with octet at index i modulo 4 of the masking key ("masking-key-octet-j"):
			 * 
			 * j = i MOD 4 transformed-octet-i = original-octet-i XOR masking-key-octet-j
			 */
			byte[] unMaskedPayLoad = new byte[frameLen];
			for (int i = 0; i < frameLen; i++) {
				byte maskedByte = inBuffer.get();
				if (maskingKey) {
					unMaskedPayLoad[i] = (byte) (maskedByte ^ mask[i % 4]);
				} else {
					unMaskedPayLoad[i] = maskedByte;
				}
			}

			if (frameBuffer == null) {
				frameBuffer = IoBuffer.wrap(unMaskedPayLoad);
				frameBuffer.position(frameBuffer.limit());
				frameBuffer.setAutoExpand(true);
			} else {
				frameBuffer.put(unMaskedPayLoad);
			}
			
			// session close
			if (opCode == FRAME_CLOSE) {
				// opCode 8 means close. See RFC 6455 Section 5.2
				// return what ever is parsed till now.
				session.close(true);
				return null;
			} else if (opCode == FRAME_PING) {
				return null;
			} else if (opCode == FRAME_PONG) {
				return null;
			}
			
		} while (inBuffer.hasRemaining());

		if (frameBuffer != null) {
			frameBuffer.flip();
		}
		return frameBuffer;
	}
}
