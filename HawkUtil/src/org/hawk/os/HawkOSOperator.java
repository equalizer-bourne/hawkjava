package org.hawk.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Properties;

import org.hawk.log.HawkLog;

/**
 * 系统相关操作封装
 * 
 * @author hawk
 * 
 */
public class HawkOSOperator {
	/**
	 * 加密字符串字符定义
	 */
	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 计算crc校验码, AP Hash算法
	 * 
	 * @param bytes
	 * @param offset
	 * @param size
	 * @param crc
	 * @return
	 */
	public static int calcCrc(byte[] bytes, int offset, int size, int crc) {
		int hash = crc;
		for (int i = offset; i < size; i++) {
			hash ^= ((i & 1) == 0) ? ((hash << 7) ^ (bytes[i] & 0xff) ^ (hash >>> 3)) : (~((hash << 11) ^ (bytes[i] & 0xff) ^ (hash >>> 5)));
		}
		return hash;
	}

	/**
	 * 计算crc校验码
	 * 
	 * @param bytes
	 * @param crc
	 * @return
	 */
	public static int calcCrc(byte[] bytes, int crc) {
		return calcCrc(bytes, 0, bytes.length, crc);
	}

	/**
	 * 计算crc校验码
	 * 
	 * @param bytes
	 * @return
	 */
	public static int calcCrc(byte[] bytes) {
		return calcCrc(bytes, 0);
	}

	/**
	 * AP Hash算法(和calcCrc一致)
	 * 
	 * @param string
	 * @return
	 */
	public static int hashString(String string) {
		int hash = 0;
		for (int i = 0; i < string.length(); i++) {
			hash ^= ((i & 1) == 0) ? ((hash << 7) ^ string.charAt(i) ^ (hash >> 3)) : (~((hash << 11) ^ string.charAt(i) ^ (hash >> 5)));
		}
		return (hash & 0x7FFFFFFF);
	}

	/**
	 * 字节数组转换为16进制字符串
	 * 
	 * @param data
	 * @return
	 */
	public static String bytesToHexString(byte[] data) {
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			chars[i * 2 + 0] = hexDigits[data[i] >>> 4 & 0xF];
			chars[i * 2 + 1] = hexDigits[data[i] & 0xF];
		}
		return new String(chars);
	}

	/**
	 * 生成随机长度字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String randomString(int length) {
		StringBuilder builder = new StringBuilder();
		try {
			for (int i = 0; i < length; i++) {
				char ch = 0;
				if (HawkRand.randInt(0, 1) == 0) {
					ch = (char) HawkRand.randInt('a', 'z');
				} else {
					ch = (char) HawkRand.randInt('A', 'Z');
				}
				builder.append(ch);
			}
		} catch (HawkException e) {
			HawkException.catchException(e);
		}
		return builder.toString();
	}

	/**
	 * 系统休眠
	 * 
	 * @param msTime
	 */
	public static void osSleep(long msTime) {
		try {
			Thread.sleep(msTime);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 默认sleep
	 */
	public static void sleep() {
		try {
			Thread.sleep(1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 添加路径到库加载路径列表
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void addUsrPath(String path) {
		try {
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (path.equals(paths[i])) {
					return;
				}
			}

			String[] pathArray = new String[paths.length + 1];
			System.arraycopy(paths, 0, pathArray, 0, paths.length);
			pathArray[paths.length] = path;
			field.set(null, pathArray);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取当前进程ID
	 * 
	 * @return
	 */
	public static long getProcessId() {
		try {
			String processName = ManagementFactory.getRuntimeMXBean().getName();
			return Long.parseLong(processName.split("@")[0]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取当前线程ID
	 * 
	 * @return
	 */
	public static long getThreadId() {
		long threadId = Thread.currentThread().getId();
		return threadId;
	}

	/**
	 * 打印系统环境
	 */
	public static void printOsEnv() {
		// 打印系统信息
		Properties props = System.getProperties();
		HawkLog.logPrintln("Os: " + props.getProperty("os.name") + ", Arch: " + props.getProperty("os.arch") + ", Version: " + props.getProperty("os.version"));

		// 用户路径
		String userDir = System.getProperty("user.dir");
		HawkLog.logPrintln("UserDir: " + userDir);

		// 系统路径
		String homeDir = System.getProperty("java.home");
		HawkLog.logPrintln("JavaHome: " + homeDir);
	}

	/**
	 * 判断windows系统
	 * 
	 * @return
	 */
	public static boolean isWindowsOS() {
		Properties props = System.getProperties();
		if (props.getProperty("os.name").toLowerCase().contains("windows")) {
			return true;
		}
		return false;
	}

	/**
	 * 从文件路径获取文件名
	 * 
	 * @param fullName
	 * @return
	 */
	public static String splitFileName(String fullName) {
		String name = "";
		fullName.replace("\\", "/");
		String[] items = fullName.split("/");
		if (items.length > 0) {
			name = items[items.length - 1];
		}
		return name;
	}

	/**
	 * 从文件路径获取文件路径
	 * 
	 * @param fullName
	 * @return
	 */
	public static String splitFilePath(String fullName) {
		String path = "";
		fullName.replace("\\", "/");
		String[] items = fullName.split("/");
		for (int i = 0; i < items.length - 1; i++) {
			path += items[i];
			path += "/";
		}
		return path;
	}

	/**
	 * 是否存在某文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean existFile(String fileName) {
		File file = new File(fileName);
		return file.exists() && file.isFile();
	}

	/**
	 * 是否存在某目录
	 * 
	 * @param folderName
	 * @return
	 */
	public static boolean existFolder(String folderName) {
		File file = new File(folderName);
		return file.exists() && file.isDirectory();
	}

	/**
	 * 获得文件大小
	 * 
	 * @param fileName
	 * @return
	 */
	public static long getFileSize(String fileName) {
		try {
			RandomAccessFile rafFile = new RandomAccessFile(fileName, "rb");
			long size = rafFile.length();
			rafFile.close();
			return size;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean osDeleteFile(String fileName) {
		File file = new File(fileName);
		return file.delete();
	}

	/**
	 * 重命名文件
	 * 
	 * @param fileName
	 * @param newFileName
	 * @return
	 */
	public static boolean renameFile(String fileName, String newFileName) {
		File file = new File(fileName);
		return file.renameTo(new File(newFileName));
	}

	/**
	 * 设置文件可写
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean setFileWritable(String fileName) {
		File file = new File(fileName);
		return file.setWritable(true);
	}

	/**
	 * 创建目录树
	 * 
	 * @param dirName
	 * @return
	 */
	public static boolean createDir(String dirName) {
		if (dirName.length() <= 0)
			return true;

		String sysDir = dirName;
		sysDir.replace("\\", "/");
		File file = new File(dirName);
		return file.mkdirs();
	}

	/**
	 * 保证文件目录树存在
	 * 
	 * @param filePath
	 */
	public static void makeSureFilePath(String filePath) {
		createDir(filePath);
	}

	/**
	 * 保证文件名前缀目录树存在
	 * 
	 * @param fileName
	 */
	public static void makeSureFileName(String fileName) {
		String folderPath = splitFilePath(fileName);
		createDir(folderPath);
	}

	/**
	 * 读取文件
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static String readFile(String filePath) throws Exception {
		InputStreamReader inputReader = null;
		BufferedReader bufferReader = null;
		try {
			InputStream inputStream = new FileInputStream(filePath);
			inputReader = new InputStreamReader(inputStream);
			bufferReader = new BufferedReader(inputReader);

			// 读取一行
			String line = null;
			StringBuffer stringBuffer = new StringBuffer();
			while ((line = bufferReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			return stringBuffer.toString();
		} catch (Exception e) {
			throw e;
		} finally {
			bufferReader.close();
			inputReader.close();
		}
	}
}
