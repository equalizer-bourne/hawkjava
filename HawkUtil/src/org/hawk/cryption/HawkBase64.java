package org.hawk.cryption;

import org.hawk.os.HawkException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * base64加解密接口封装
 * 
 * @author hawk
 * 
 */
public class HawkBase64 {
	/**
	 * Base64加密
	 * 
	 * @param bytes
	 * @return
	 */
	public static String encode(byte[] bytes) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(bytes);
	}

	/**
	 * Base64解密
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] decode(String str) {
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			return decoder.decodeBuffer(str);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
