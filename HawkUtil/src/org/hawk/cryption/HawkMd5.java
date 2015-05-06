package org.hawk.cryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * md5操作封装
 * @author hawk
 *
 */
public class HawkMd5 {
	/**
	 * 转换md5的字节流到十六进制字符串
	 * @param data
	 * @return
	 */
	private static String bytesToHexString(byte[] data) {		
		StringBuilder builder = new StringBuilder(data.length * 4);
		for (int i = 0; i < data.length; i++) {
			builder.append(HawkOSOperator.HEX_DIGITS[data[i] >>> 4 & 0xF]);
			builder.append(HawkOSOperator.HEX_DIGITS[data[i] & 0xF]);
		}
		return builder.toString();
	}
	
	/**
	 * 返回32位MD5字符串
	 * @param bytes
	 * @return
	 */
	public static String makeMD5(byte[] bytes) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");			
			messageDigest.update(bytes);
			byte[] md5Bytes = messageDigest.digest();
			return bytesToHexString(md5Bytes);
		} catch (NoSuchAlgorithmException e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 返回32位MD5字符串
	 * @param bytes
	 * @param offset
	 * @param size
	 * @return
	 */
	public static String makeMD5(byte[] bytes, int offset, int size) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");			
			messageDigest.update(bytes, offset, size);
			byte[] md5Bytes = messageDigest.digest();
			return bytesToHexString(md5Bytes);
		} catch (NoSuchAlgorithmException e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 返回32位MD5字符串
	 * @param string
	 * @return
	 */
	public static String makeMD5(String string) {
		try {
			return makeMD5(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 计算文件的md5
	 * @param file
	 * @return
	 */
	public static String makeMD5(File file) {
		FileInputStream inputStream = null;
		if (file.isFile()) {
			try {
				int count = 0;
				byte data[] = new byte[4096];
				inputStream = new FileInputStream(file);
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				while ((count = inputStream.read(data, 0, data.length)) > 0) {
					messageDigest.update(data, 0, count);
				}
				byte[] md5Bytes = messageDigest.digest();
				return bytesToHexString(md5Bytes);
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						HawkException.catchException(e);
					}
				}
			}
		}
		return null;
	}
}
