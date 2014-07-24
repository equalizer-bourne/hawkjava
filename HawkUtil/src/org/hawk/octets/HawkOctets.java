package org.hawk.octets;

import java.util.Arrays;

import org.hawk.os.HawkOSOperator;

/**
 * 字节数组封装
 * 
 * @author hawk
 * 
 */
public class HawkOctets {
	/**
	 * 有效字节长度
	 */
	protected int size;
	/**
	 * 字节数组
	 */
	protected byte[] byteArray;
	/**
	 * 默认数组长度
	 */
	static final int DEFAULT_SIZE = 1024;

	/**
	 * 默认构造函数
	 */
	public HawkOctets() {
		this(DEFAULT_SIZE);
	}

	/**
	 * 创建指定长度字节数组
	 * 
	 * @param capacity
	 */
	public HawkOctets(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("illegal capacity: " + capacity);
		}

		size = 0;
		if (capacity > 0) {
			byteArray = new byte[capacity];
		}
	}

	/**
	 * 添加字节
	 * 
	 * @param b
	 * @return
	 */
	public boolean add(byte b) {
		reserve(size + 1);
		byteArray[size++] = b;
		return true;
	}

	/**
	 * 添加字节数组
	 * 
	 * @param bytes
	 * @return
	 */
	public boolean add(byte[] bytes) {
		if (bytes != null) {
			reserve(size + bytes.length);
			for (int i = 0; i < bytes.length; i++) {
				byteArray[size++] = bytes[i];
			}
			return true;
		}
		return false;
	}

	/**
	 * 插入字节
	 * 
	 * @param offset
	 * @param b
	 * @return
	 */
	public boolean insert(int offset, byte b) {
		reserve(size + 1);
		System.arraycopy(byteArray, offset, byteArray, offset + 1, size - offset);
		byteArray[offset] = b;
		size++;
		return true;
	}

	/**
	 * 插入字节数组
	 * 
	 * @param offset
	 * @param bs
	 * @return
	 */
	public boolean insert(int offset, byte[] bytes) {
		reserve(size + bytes.length);
		System.arraycopy(byteArray, offset, byteArray, offset + bytes.length, size - offset);

		for (int i = 0; i < bytes.length; i++) {
			byteArray[offset + i] = bytes[i];
		}
		size += bytes.length;
		return true;
	}

	/**
	 * 替换字节数组
	 * 
	 * @param bytes
	 */
	public void replace(byte[] bytes) {
		size = bytes.length;
		reserve(bytes.length);
		System.arraycopy(bytes, 0, byteArray, 0, bytes.length);
	}

	/**
	 * 获取数组有效字节数
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * 获取数组容量
	 * 
	 * @return
	 */
	public int getCapacity() {
		return byteArray.length;
	}

	/**
	 * 获取字节数组
	 * 
	 * @return
	 */
	public byte[] getByteArray() {
		return byteArray;
	}

	/**
	 * 拷贝字节数组
	 * 
	 * @return
	 */
	public byte[] copyByteArray() {
		return Arrays.copyOf(byteArray, size);
	}

	/**
	 * 清除数据
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * 从头开始擦除指定数目字节
	 * 
	 * @param count
	 * @return
	 */
	public boolean erase(int count) {
		return erase(0, count);
	}

	/**
	 * 指定偏移擦除字节
	 * 
	 * @param offset
	 * @param count
	 * @return
	 */
	public boolean erase(int offset, int count) {
		if (offset + count > size) {
			count = size - offset;
		}

		for (int i = offset; i < size - count; i++) {
			byteArray[i] = byteArray[count + i];
		}

		this.size -= count;
		return true;
	}

	/**
	 * 扩充容量
	 * 
	 * @param size
	 */
	private void reserve(int size) {
		int capacity = byteArray.length;
		if (size > capacity) {
			capacity = (capacity * 3) / 2 + 1;
			if (capacity < size) {
				capacity = size;
			}
			byteArray = Arrays.copyOf(byteArray, capacity);
		}
	}

	/**
	 * 计算CRC验证码
	 * 
	 * @return
	 */
	public int calcCrc() {
		return HawkOSOperator.calcCrc(byteArray, 0, size, 0);
	}
}
