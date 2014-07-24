package org.hawk.cryption;

import java.security.MessageDigest;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * SHA加密封装
 * 
 * @author hawk
 */
public class HawkSHACrypt {
	/**
	 * 加密字节数组
	 * 
	 * @param bytes
	 * @param offset
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] bytes, int offset, int size) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA");
		messageDigest.update(bytes, offset, size);
		return messageDigest.digest();
	}

	/**
	 * 加密字节数组, 返回字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String encrypt(byte[] bytes) {
		try {
			byte[] shaBytes = encrypt(bytes, 0, bytes.length);
			return HawkOSOperator.bytesToHexString(shaBytes);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
