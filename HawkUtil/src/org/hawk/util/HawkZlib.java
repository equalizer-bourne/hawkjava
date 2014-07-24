package org.hawk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.hawk.os.HawkException;

/**
 * zlib压缩&解压缩
 * 
 * @author hawk
 */
public class HawkZlib {
	/**
	 * 计算压缩后最大字节数
	 * 
	 * @param sourceLen
	 * @return
	 */
	public static int zlibBound(int sourceLen) {
		return sourceLen + (sourceLen >> 12) + (sourceLen >> 14) + (sourceLen >> 25) + 13;
	}

	/**
	 * 压缩
	 * 
	 * @param inputData
	 * @return
	 */
	public static byte[] zlibDeflate(byte[] inputData) {
		try {
			Deflater deflater = new Deflater();
			deflater.setInput(inputData);
			deflater.finish();

			ByteArrayOutputStream bos = new ByteArrayOutputStream(inputData.length);
			byte[] buf = new byte[1024];
			while (!deflater.finished()) {
				int count = deflater.deflate(buf);
				bos.write(buf, 0, count);
			}
			deflater.end();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 解压缩
	 * 
	 * @param inputData
	 * @return
	 */
	public static byte[] zlibInflate(byte[] inputData) {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(inputData);

			ByteArrayOutputStream bos = new ByteArrayOutputStream(inputData.length);
			byte[] buf = new byte[1024];
			while (!inflater.finished()) {
				try {
					int count = inflater.inflate(buf);
					bos.write(buf, 0, count);
				} catch (DataFormatException e) {
					HawkException.catchException(e);
					break;
				}
			}
			inflater.end();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
