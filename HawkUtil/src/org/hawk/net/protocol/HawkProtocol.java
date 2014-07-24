package org.hawk.net.protocol;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.hawk.net.HawkSession;
import org.hawk.octets.HawkOctetsStream;
import org.hawk.os.HawkException;

import com.google.protobuf.GeneratedMessage.Builder;

/**
 * 协议包装类
 * 
 * @author hawk
 */
public class HawkProtocol {
	/**
	 * 协议头字节数
	 */
	public static final int HEADER_SIZE = 16;

	/*
	 * 协议头格式
	 */
	public static class ProtocolHeader {
		/**
		 * 类型
		 */
		public int type;
		/**
		 * 字节数
		 */
		public int size;
		/**
		 * 保留字段
		 */
		public int reserve;
		/**
		 * 校验码
		 */
		public int crc;

		/**
		 * 构造函数
		 * 
		 * @param type
		 */
		ProtocolHeader(int type) {
			this.type = type;
		}

		/**
		 * 清空信息
		 */
		public void clear() {
			type = 0;
			size = 0;
			reserve = 0;
			crc = 0;
		}
	}

	/**
	 * 协议头
	 */
	private ProtocolHeader header;
	/**
	 * 协议数据
	 */
	private HawkOctetsStream octets;
	/**
	 * 来源会话
	 */
	private HawkSession session;

	/**
	 * 构造函数
	 */
	public HawkProtocol() {
		header = new ProtocolHeader(0);
	}

	/**
	 * 构造函数
	 * 
	 * @param type
	 */
	public HawkProtocol(int type) {
		header = new ProtocolHeader(type);
	}

	/**
	 * 构造函数
	 * 
	 * @param type
	 * @param bytes
	 */
	public HawkProtocol(int type, byte[] bytes) {
		this(type);

		writeOctets(bytes);
	}

	/**
	 * 构造函数
	 * 
	 * @param type
	 * @param buffer
	 */
	public HawkProtocol(int type, ByteBuffer buffer) {
		this(type);

		writeOctets(buffer);
	}

	/**
	 * 构造函数
	 * 
	 * @param type
	 * @param builder
	 */
	public HawkProtocol(int type, Builder<?> builder) {
		this(type, builder.build().toByteArray());
	}

	/**
	 * 绑定会话来源
	 * 
	 * @param session
	 */
	public void bindSession(HawkSession session) {
		this.session = session;
	}

	/**
	 * 获得绑定的会话
	 * 
	 * @return
	 */
	public HawkSession getSession() {
		return session;
	}

	/**
	 * 获得协议类型
	 * 
	 * @return
	 */
	public int getType() {
		if (header != null) {
			return header.type;
		}
		return 0;
	}

	/**
	 * 获得协议大小
	 * 
	 * @return
	 */
	public int getSize() {
		if (header != null) {
			return header.size;
		}
		return 0;
	}

	/**
	 * 获取保留字段
	 * 
	 * @return
	 */
	public int getReserve() {
		if (header != null) {
			return header.reserve;
		}
		return 0;
	}

	/**
	 * 设置保留字段
	 * 
	 * @return
	 */
	public void setReserve(int reserve) {
		if (header != null) {
			header.reserve = reserve;
		}
	}

	/**
	 * 获得协议校检码
	 * 
	 * @return
	 */
	public int getCrc() {
		if (header != null) {
			return header.crc;
		}
		return 0;
	}

	/**
	 * 获取协议buffer
	 * 
	 * @return
	 */
	public HawkOctetsStream getOctets() {
		return octets;
	}

	/**
	 * crc校验
	 */
	public boolean checkCrc(int crc) {
		return header.crc == crc;
	}

	/**
	 * 协议打包: type + size + reserve + crc + data
	 * 
	 * @param os
	 * @return
	 */
	public boolean encode(HawkOctetsStream os) {
		// 协议编码
		try {
			os.writeInt(header.type);
			os.writeInt(header.size);
			os.writeInt(header.reserve);
			os.writeInt(header.crc);
			if (header.size > 0) {
				os.pushBuffer(octets.getBuffer());
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 协议打包: type + size + reserve + crc + data
	 * 
	 * @param buffer
	 * @return
	 */
	public boolean encode(IoBuffer buffer) {
		// 协议编码
		try {
			buffer.putInt(header.type);
			buffer.putInt(header.size);
			buffer.putInt(header.reserve);
			buffer.putInt(header.crc);
			if (header.size > 0) {
				int pos = octets.position();
				buffer.put(octets.getBuffer());
				octets.position(pos);
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 协议解包
	 * 
	 * @param os
	 * @return
	 * @throws HawkException
	 */
	public boolean decode(HawkOctetsStream os) throws HawkException {
		// 检测协议完整性
		if (os.remaining() < HEADER_SIZE) {
			return false;
		}

		// 重置状态
		header.clear();
		int crc = 0;

		// 标记
		os.mark();
		try {
			header.type = os.readInt();
			header.size = os.readInt();
			header.reserve = os.readInt();
			header.crc = os.readInt();

			// 数据体大小判断
			if (os.remaining() >= header.size) {
				// 读协议数据
				if (header.size > 0) {
					octets = HawkOctetsStream.create(header.size);
					octets.pushBytes(os.popBytes(header.size));
					octets.flip();
					crc = octets.calcCrc();
				}
			} else {
				// 数据包不完全,回滚
				os.rollback();
				return false;
			}
		} catch (Exception e) {
			// 数据包不完全,回滚
			os.rollback();
			HawkException.catchException(e);
			return false;
		}

		// crc校验
		if (!checkCrc(crc)) {
			throw new HawkException(String.format("protocol crc verify failed, type: %d", header.type));
		}

		return true;
	}

	/**
	 * 协议解包
	 * 
	 * @param buffer
	 * @return
	 * @throws HawkException
	 */
	public boolean decode(IoBuffer buffer) throws HawkException {
		// 检测协议完整性
		if (buffer.remaining() < HEADER_SIZE) {
			return false;
		}

		// 重置状态
		header.clear();
		int crc = 0;

		// 标记
		buffer.mark();
		try {
			header.type = buffer.getInt();
			header.size = buffer.getInt();
			header.reserve = buffer.getInt();
			header.crc = buffer.getInt();

			// 数据体大小判断
			if (buffer.remaining() >= header.size) {
				// 读协议数据
				if (header.size > 0) {
					octets = HawkOctetsStream.create(header.size);
					buffer.get(octets.getBuffer().array(), 0, header.size);
					// 移动postion
					octets.position(header.size);
					octets.flip();
					crc = octets.calcCrc();
				}
			} else {
				// 数据包不完全,回滚
				buffer.reset();
				return false;
			}
		} catch (Exception e) {
			// 数据包不完全,回滚
			buffer.reset();
			// 捕获异常
			HawkException.catchException(e);
			return false;
		}

		// crc校验
		if (!checkCrc(crc)) {
			throw new HawkException(String.format("protocol crc verify failed, type: %d", header.type));
		}

		return true;
	}

	/**
	 * 写缓冲区
	 * 
	 * @param bytes
	 * @return
	 */
	private boolean writeOctets(byte[] bytes) {
		if (bytes == null) {
			return true;
		}

		try {
			octets = HawkOctetsStream.create(bytes, false);
			calcOctets();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 写缓冲区
	 * 
	 * @param buffer
	 * @return
	 */
	private boolean writeOctets(ByteBuffer byteBuffer) {
		if (byteBuffer == null) {
			return true;
		}

		try {
			octets = HawkOctetsStream.create(byteBuffer);
			calcOctets();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 计算协议信息
	 */
	private void calcOctets() {
		if (octets != null) {
			header.size = octets.remaining();
			header.crc = octets.calcCrc();
		}
	}

	/**
	 * 解析协议
	 * 
	 * @param template
	 * @return
	 */
	public <T> T parseProtocol(T template) {
		return (T) HawkProtocolManager.getInstance().parseProtocol(this, template);
	}
}
