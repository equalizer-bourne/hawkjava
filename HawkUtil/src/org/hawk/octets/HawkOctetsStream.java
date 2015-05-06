package org.hawk.octets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

/**
 * 字节流封装，注意direct，在ZMQ的使用环境只能使用Direct，因为ZMQ调用的是 系统动态库 内部使用ByteBuffer,感谢新版NIO
 * 
 * @author hawk
 * 
 */
public class HawkOctetsStream {
	/**
	 * 是否是系统本地堆里面创建的缓冲区
	 */
	protected boolean isDirect;
	/**
	 * 缓冲区
	 */
	protected ByteBuffer byteBuffer;

	/**
	 * 默认构造函数
	 */
	private HawkOctetsStream() {
		isDirect = false;
	}

	/**
	 * 默认构造JVM堆内缓冲
	 * 
	 * @return
	 */
	public static HawkOctetsStream create() {
		return create(4096);
	}

	/**
	 * 创建容量为capacity的缓冲区
	 * 
	 * @param capacity
	 * @return
	 */
	public static HawkOctetsStream create(int capacity) {
		HawkOctetsStream os = new HawkOctetsStream();
		os.isDirect = false;
		os.byteBuffer = ByteBuffer.allocate(capacity);
		return os;
	}

	/**
	 * 创建容量为capacity的缓冲区
	 * 
	 * @param capacity
	 * @param isDirect
	 * @return
	 */
	public static HawkOctetsStream create(int capacity, boolean isDirect) {
		HawkOctetsStream os = new HawkOctetsStream();
		os.isDirect = isDirect;
		if (isDirect) {
			os.byteBuffer = ByteBuffer.allocateDirect(capacity);
		} else {
			os.byteBuffer = ByteBuffer.allocate(capacity);
		}
		return os;
	}

	/**
	 * 根据一个已有的Buffer构造Stream
	 * 
	 * @param buffer
	 * @return
	 */
	public static HawkOctetsStream create(ByteBuffer buffer) {
		HawkOctetsStream os = new HawkOctetsStream();
		os.isDirect = buffer.isDirect();
		os.byteBuffer = allocateBuffer(buffer.capacity(), buffer.isDirect()).order(buffer.order()).put(buffer);
		os.byteBuffer.flip();
		return os;
	}

	/**
	 * 根据已有字节数组数据创建ByteBuffer
	 * 
	 * @param bytes
	 *            字节数组
	 * @param isDirect
	 *            是否是本地堆
	 * @return
	 */
	public static HawkOctetsStream create(byte[] bytes, boolean isDirect) {
		HawkOctetsStream os = new HawkOctetsStream();
		os.isDirect = isDirect;
		if (isDirect) {
			os.byteBuffer = ByteBuffer.allocateDirect(bytes.length);
		} else {
			os.byteBuffer = ByteBuffer.allocate(bytes.length);
		}
		os.byteBuffer.put(bytes);
		os.byteBuffer.flip();
		return os;
	}

	/**
	 * 分配空间
	 * 
	 * @param capacity
	 * @param isDirect
	 * @return
	 */
	private static ByteBuffer allocateBuffer(int capacity, boolean isDirect) {
		if (isDirect) {
			return ByteBuffer.allocateDirect(capacity);
		} else {
			return ByteBuffer.allocate(capacity);
		}
	}

	/**
	 * 获得buffer的容量大小
	 * 
	 * @return
	 */
	public int capacity() {
		return byteBuffer.capacity();
	}

	/**
	 * 获得可使用的字节数组大小
	 * 
	 * @return
	 */
	public int limit() {
		return byteBuffer.limit();
	}

	/**
	 * 获得数据游标
	 * 
	 * @return
	 */
	public int position() {
		return byteBuffer.position();
	}

	/**
	 * 设置数据游标
	 * 
	 * @return
	 */
	public HawkOctetsStream position(int pos) {
		byteBuffer.position(pos);
		return this;
	}

	/**
	 * 移动数据游标
	 * 
	 * @return
	 */
	public HawkOctetsStream nonius(int mark) {
		byteBuffer.position(byteBuffer.position() + mark);
		return this;
	}

	/**
	 * position事务mark标记
	 */
	public HawkOctetsStream mark() {
		byteBuffer.mark();
		return this;
	}

	/**
	 * position事务回滚标记
	 */
	public HawkOctetsStream rollback() {
		byteBuffer.reset();
		return this;
	}

	/**
	 * 翻转(limit = position, position = 0, mark = -1)
	 */
	public HawkOctetsStream flip() {
		byteBuffer.flip();
		return this;
	}

	/**
	 * 清空数据(limit = cap, position = 0, mark = -1)
	 */
	public HawkOctetsStream clear() {
		byteBuffer.clear();
		return this;
	}

	/**
	 * 倒带 (position = 0, mark = -1)
	 */
	public HawkOctetsStream rewind() {
		byteBuffer.rewind();
		return this;
	}

	/**
	 * 是否有可读取的字节数
	 * 
	 * @return
	 */
	public boolean hasRemaining() {
		return byteBuffer.hasRemaining();
	}

	/**
	 * 剩余读取的字节数
	 * 
	 * @return
	 */
	public int remaining() {
		return byteBuffer.remaining();
	}

	/**
	 * 移除position之前的数据
	 * 
	 * @return
	 */
	public HawkOctetsStream compact() {
		byteBuffer.compact();
		return this;
	}

	/**
	 * 容量扩展
	 * 
	 * @param size
	 * @return
	 */
	public final HawkOctetsStream expand(int size) {
		int end = position() + size;
		if (end > capacity()) {
			int cap = capacity() * 3 / 2 + 1;
			if (cap < end) {
				cap = end;
			}
			// 扩展
			if (cap > capacity()) {
				capacity(cap);
			}
		}

		if (end > limit()) {
			byteBuffer.limit(end);
		}
		return this;
	}

	/**
	 * 分配空间
	 * 
	 * @param cap
	 * @return
	 */
	private HawkOctetsStream capacity(int capacity) {
		if (capacity > capacity()) {
			// 记录扩容之前的状态
			int pos = position();
			byteBuffer = allocateBuffer(capacity, isDirect).order(byteBuffer.order()).put(byteBuffer);
			byteBuffer.position(pos);
		}
		return this;
	}

	/**
	 * 获取字节缓冲区
	 * 
	 * @return
	 */
	public ByteBuffer getBuffer() {
		return byteBuffer;
	}

	/**
	 * 计算CRC校验码
	 * 
	 * @return
	 */
	public int calcCrc() {
		return HawkOSOperator.calcCrc(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining(), 0);
	}

	/**
	 * 写入字节
	 * 
	 * @param b
	 * @return
	 */
	public final HawkOctetsStream writeByte(byte b) {
		expand(1);
		byteBuffer.put(b);
		return this;
	}

	/**
	 * 写入boolean
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeBoolean(boolean val) {
		expand(1);
		byteBuffer.put((byte) (val ? 1 : 0));
		return this;
	}

	/**
	 * 写入short数据
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeShort(short val) {
		expand(2);
		byteBuffer.putShort(val);
		return this;
	}

	/**
	 * 写入int类型的值
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeInt(int val) {
		expand(4);
		byteBuffer.putInt(val);
		return this;
	}

	/**
	 * 写入float数据类型
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeFloat(float val) {
		expand(4);
		byteBuffer.putFloat(val);
		return this;
	}

	/**
	 * 写double类型数据
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeDouble(double val) {
		expand(8);
		byteBuffer.putDouble(val);
		return this;
	}

	/**
	 * 写入long类型数据
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeLong(long val) {
		expand(8);
		byteBuffer.putLong(val);
		return this;
	}

	/**
	 * 写入字节数组
	 * 
	 * @param bytes
	 * @return
	 */
	public final HawkOctetsStream writeBytes(byte[] bytes) {
		expand(bytes.length);
		writeInt(bytes.length);
		this.byteBuffer.put(bytes);
		return this;
	}

	/**
	 * 写入字符串
	 * 
	 * @param val
	 * @return
	 */
	public final HawkOctetsStream writeString(String val) {
		byte[] bytes = val.getBytes(Charset.forName("utf8"));
		writeBytes(bytes);
		return this;
	}

	/**
	 * 写入字节流数据
	 * 
	 * @param octets
	 * @return
	 */
	public final HawkOctetsStream writeOctets(HawkOctets octets) {
		writeBytes(octets.getByteArray());
		return this;
	}

	/**
	 * 写入ByteBuffer
	 * 
	 * @param buffer
	 * @return
	 */
	public final HawkOctetsStream writeBuffer(ByteBuffer buffer) {
		writeInt(buffer.remaining());
		expand(buffer.remaining());
		byteBuffer.put(buffer);
		return this;
	}

	/**
	 * 写入可序列化对象
	 * 
	 * @param marshal
	 * @return
	 */
	public final HawkOctetsStream writeMarshal(HawkMarshal marshal) {
		marshal.marshal(this);
		return this;
	}
	
	/**
	 * 直接写入字节数组
	 * @param bytes
	 */
	public final HawkOctetsStream pushBytes(byte[] bytes) {
		if (bytes != null) {
			expand(bytes.length);
			byteBuffer.put(bytes);
		}
		return this;
	}
	
	/**
	 * 直接写入字节数组
	 * @param bytes
	 */
	public final HawkOctetsStream pushBytes(byte[] bytes, int offset, int length) {
		if (bytes != null) {
			expand(length);
			byteBuffer.put(bytes, offset, length);
		}
		return this;
	}
	
	/**
	 * 直接写入ByteBuffer
	 * @param buffer
	 * @return
	 */
	public final HawkOctetsStream pushBuffer(ByteBuffer buffer) {
		if (buffer != null) {
			expand(buffer.remaining());
			byteBuffer.put(buffer.array(), buffer.position(), buffer.remaining());
		}
		return this;
	}
	
	/**
	 * 获取单个字节
	 * 
	 * @return
	 * @throws HawkException
	 */
	public final byte readByte() throws HawkException {
		try {
			return byteBuffer.get();
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}

	/**
	 * 读取boolean值
	 * 
	 * @return
	 */
	public final boolean readBoolean() throws HawkException {
		return readByte() == 1;
	}

	/**
	 * 读取short数据
	 * 
	 * @return
	 */
	public final short readShort() throws HawkException {
		try {
			return byteBuffer.getShort();
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}

	/**
	 * 读取Int类型值
	 * 
	 * @return
	 * @throws HawkException
	 */
	public final int readInt() throws HawkException {
		try {
			return byteBuffer.getInt();
		} catch (Exception e) {
			throw new HawkException("OctetsStream Underflow");
		}
	}

	/**
	 * 读取浮点数据
	 * 
	 * @return
	 */
	public final float readFloat() throws HawkException {
		try {
			return byteBuffer.getFloat();
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}

	/**
	 * 读取double型数据
	 * 
	 * @return
	 */
	public final double readDouble() throws HawkException {
		try {
			return byteBuffer.getDouble();
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}

	/**
	 * 读取long型数据
	 * 
	 * @return
	 */
	public final long readLong() throws HawkException {
		try {
			return byteBuffer.getLong();
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}

	/**
	 * 获取字节数组
	 * 
	 * @param bytes
	 * @param offset
	 * @param size
	 * @return
	 */
	public final byte[] readBytes() throws HawkException {
		int size = readInt();
		if (size > 0) {
			try {
				byte[] bytes = new byte[size];
				byteBuffer.get(bytes, 0, size);
				return bytes;
			} catch (Exception e) {
				throw new HawkException("octetsstream underflow");
			}
		}
		return null;
	}

	/**
	 * 读取字符串
	 * 
	 * @return
	 * @throws HawkException
	 */
	public String readString() throws HawkException {
		byte[] bytes = readBytes();
		if (bytes != null) {
			return new String(bytes, Charset.forName("utf8"));
		}
		return "";
	}

	/**
	 * 读取字节数组
	 * 
	 * @return
	 * @throws HawkException
	 */
	public final HawkOctets readOctets() throws HawkException {
		byte[] bytes = readBytes();
		HawkOctets octets = new HawkOctets(bytes.length);
		octets.add(bytes);
		return octets;
	}

	/**
	 * 读取可反序列化对象
	 * 
	 * @param marshal
	 * @return
	 * @throws HawkException
	 */
	public final HawkOctetsStream readMarshal(HawkMarshal marshal) throws HawkException {
		marshal.clear();
		marshal.unmarshal(this);
		return this;
	}
	
	/**
	 * 获取字节数组
	 * @param size
	 * @return
	 */
	public final byte[] popBytes(int size) throws HawkException {
		try {
			byte[] bytes = new byte[size];
			byteBuffer.get(bytes, 0, size);
			return bytes;
		} catch (Exception e) {
			throw new HawkException("octetsstream underflow");
		}
	}
}
