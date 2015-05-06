package org.hawk.zmq;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.zeromq.ZMQ;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.octets.HawkOctetsStream;
import org.hawk.os.HawkException;
import org.hawk.util.HawkByteOrder;

/**
 * zmq操作封装
 * 
 * @author hawk
 * 
 */
public class HawkZmq {
	/**
	 * 上下文线程默认数量
	 */
	public static final int HZMQ_CONTEXT_THREAD = 1;
	/**
	 * 超时默认设置常量
	 */
	public static final int HZMQ_OPT_TIMEOUT = 100;
	/**
	 * 接收缓冲区
	 */
	public static final int HZMQ_BUFFER_SIZE = 8192;
	/**
	 * 读事件id
	 */
	public static final int HZMQ_EVENT_READ = 0x01;
	/**
	 * 写事件id
	 */
	public static final int HZMQ_EVENT_WRITE = 0x02;
	/**
	 * 错误事件id
	 */
	public static final int HZMQ_EVENT_ERROR = 0x04;
	/**
	 * 断开重连时间周期
	 */
	public static final int HZMQ_RECONNECT_IVL = 1000;
	/**
	 * 非阻塞
	 */
	public static final int HZMQ_NOBLOCK = 1;
	/**
	 * 不等待
	 */
    public static final int HZMQ_DONTWAIT = 1;
    /**
     * 继续发生
     */
    public static final int HZMQ_SNDMORE = 2;
    
	/**
	 * Socket transport events (tcp and ipc only) 主要用作Monitor参数事件集
	 * 
	 * @author hawk
	 */
	public static final class SocketEvent {
		public static final int CONNECTED = 1;
		public static final int CONNECT_DELAYED = 2;
		public static final int CONNECT_RETRIED = 4;
		public static final int LISTENING = 8;
		public static final int BIND_FAILED = 16;
		public static final int ACCEPTED = 32;
		public static final int ACCEPT_FAILED = 64;
		public static final int CLOSED = 128;
		public static final int CLOSE_FAILED = 256;
		public static final int DISCONNECTED = 512;
		public static final int MONITOR_STOPPED = 1024;
	}

	/**
	 * 可用类型定义
	 * 
	 * @author hawk
	 */
	public static final class ZmqType {
	    public static final int PAIR = 0;
	    public static final int PUB = 1;
	    public static final int SUB = 2;
	    public static final int REQ = 3;
	    public static final int REP = 4;
	    public static final int DEALER = 5;
	    public static final int XREQ = DEALER;
	    public static final int ROUTER = 6;
	    public static final int XREP = ROUTER;
	    public static final int PULL = 7;
	    public static final int PUSH = 8;
	    public static final int XPUB = 9;
	    public static final int XSUB = 10;
	}
	
	/**
	 * 通信对象
	 */
	ZMQ.Socket socket;
	/**
	 * 通信对象状态监视器
	 */
	ZMQ.Socket monitor;
	/**
	 * 事件查询对象
	 */
	ZMQ.PollItem[] socketPoll;
	/**
	 * 事件查询对象
	 */
	ZMQ.PollItem[] monitorPoll;
	/**
	 * 数据缓冲流
	 */
	HawkOctetsStream stream;

	/**
	 * 默认构造函数
	 */
	protected HawkZmq() {
	}

	/**
	 * 初始化ZMQ
	 * 
	 * @param context
	 * @param type
	 * @return
	 * @throws HawkException
	 */
	public boolean init(ZMQ.Context context, int type) throws HawkException {
		if (context != null && socket == null) {
			try {
				socket = context.socket(type);
				if (socket != null) {
					// linger选项
					socket.setLinger(0);

					// 设置收发缓冲区
					socket.setSendBufferSize(HZMQ_BUFFER_SIZE);
					socket.setReceiveBufferSize(HZMQ_BUFFER_SIZE);

					// 设置收发超时
					socket.setSendTimeOut(HZMQ_OPT_TIMEOUT);
					socket.setReceiveTimeOut(HZMQ_OPT_TIMEOUT);
					return true;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 绑定监听端口
	 * 
	 * @param address
	 *            (example:"tcp://127.0.0.1:8000")
	 */
	public boolean bind(String address) {
		if (socket != null) {
			try {
				socket.bind(address);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 连接远端对象
	 * 
	 * @param address
	 *            (example:"tcp://127.0.0.1:8000")
	 */
	public boolean connect(String address) {
		if (socket != null) {
			try {
				// 设置重连间隔
				socket.setReconnectIVL(HZMQ_RECONNECT_IVL);
				socket.connect(address);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 获取句柄
	 * 
	 * @return
	 */
	public ZMQ.Socket getSocket() {
		return socket;
	}

	/**
	 * 关闭释放对象
	 */
	protected void close() {
		if (socket != null) {
			try {
				socket.close();
				if (monitor != null) {
					monitor.close();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 设置唯一标识
	 * 
	 * @param identity
	 * @return
	 */
	public boolean setIdentity(int identity) {
		long zmqId = (((long) identity) << 32) | 1;
		return setIdentity(zmqId);
	}

	/**
	 * 设置唯一标识
	 * 
	 * @param identity
	 * @return
	 */
	public boolean setIdentity(long identity) {
		if (socket != null) {
			ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			buffer.putLong(identity);
			buffer.flip();
			return setIdentity(buffer.array());
		}
		return false;
	}

	/**
	 * 设置唯一标识
	 * 
	 * @param bytes
	 * @return
	 */
	public boolean setIdentity(byte[] bytes) {
		if (socket != null) {
			try {
				socket.setIdentity(bytes);
				return true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 发送字节数组
	 * 
	 * @param bytes
	 * @param flag
	 * @return
	 */
	public boolean send(byte[] bytes, int flag) {
		if (socket != null && bytes != null) {
			try {
				return socket.send(bytes, flag);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 发送ByteBuffer 返回发送的字节数
	 * 
	 * @param buffer
	 * @param flag
	 * @return
	 */
	public boolean send(ByteBuffer buffer, int flag) {
		if (socket != null && buffer != null) {
			try {
				int buffSize = buffer.remaining();
				int sendSize = socket.sendByteBuffer(buffer, flag);
				return sendSize == buffSize;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 接受消息
	 * 
	 * @param bytes
	 * @param flag
	 * @return
	 */
	public int recv(byte[] bytes, int flag) {
		if (socket != null && bytes.length > 0) {
			try {
				int recvSize = socket.recv(bytes, 0, bytes.length, flag);
				return recvSize;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return -1;
	}

	/**
	 * 接受消息
	 * 
	 * @param buffer
	 * @param flag
	 * @return
	 */
	public int recv(ByteBuffer buffer, int flag) {
		if (socket != null && buffer != null && buffer.capacity() > 0) {
			try {
				int recvSize = socket.recvZeroCopy(buffer, buffer.capacity(), flag);
				if (recvSize > 0) {
					buffer.flip();
					return recvSize;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return -1;
	}

	/**
	 * 判断封包有跟多数据等待读取
	 * 
	 * @return
	 */
	public boolean isWaitRecv() {
		if (socket != null) {
			try {
				return socket.hasReceiveMore();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	/**
	 * 检查可读事件(可写事件不检测)
	 * 
	 * @param timeout
	 * @return
	 */
	public int pollEvent(int events, int timeout) {
		if (socket != null) {
			// 初始创建
			if (socketPoll == null) {
				socketPoll = new ZMQ.PollItem[] { new ZMQ.PollItem(socket, ZMQ.Poller.POLLIN | ZMQ.Poller.POLLERR) };
			}
			
			// 查询事件
			try {
				int eventCnt = ZMQ.poll(socketPoll, timeout);
				if (eventCnt > 0) {
					return (socketPoll[0].readyOps() & events);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return 0;
	}

	/**
	 * 确认缓冲对象存在
	 */
	public HawkOctetsStream checkStream() {
		if (stream == null) {
			stream = HawkOctetsStream.create(HZMQ_BUFFER_SIZE, true);
		}
		stream.clear();
		return stream;
	}

	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @param flag
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol, int flag) {
		if (protocol != null) {
			checkStream();
			try {
				if (protocol.encode(stream)) {
					return send(stream.flip().getBuffer(), flag);
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * 接收协议
	 * 
	 * @param flag
	 * @return
	 */
	public HawkProtocol recvProtocol(int flag) {
		checkStream();
		if (recv(stream.getBuffer(), flag) > 0) {
			HawkProtocol protocol = HawkProtocol.valueOf();
			try {
				if (protocol.decode(stream)) {
					return protocol;
				}
			} catch (HawkException e) {
				HawkException.catchException(e);
			}
		}
		return null;
	}

	/**
	 * 丢弃消息
	 * 
	 * @return
	 */
	public void discardMsg() {
		checkStream();
		while ((pollEvent(HZMQ_EVENT_READ, 0)) > 0) {
			stream.clear();
			if (recv(stream.getBuffer(), 0) < 0) {
				break;
			}

			// 接收消息帧剩余数据
			while (socket.hasReceiveMore()) {
				stream.clear();
				if (recv(stream.getBuffer(), 0) < 0) {
					break;
				}
			}
		}
	}

	// 开启事件监视器
	public boolean startMonitor(int events) {
		if (socket != null) {
			try {
				String addr = "inproc://hawk-zmq-monitor-" + socket.getFD();
				if (socket.monitor(addr, events)) {
					if (monitor != null) {
						monitor.close();
					}

					// 创建监视器, 连接到对端
					monitor = HawkZmqManager.getInstance().context.socket(ZMQ.PAIR);
					if (monitor != null) {
						monitor.setLinger(0);
						monitor.setSendTimeOut(HZMQ_OPT_TIMEOUT);
						monitor.setReceiveTimeOut(HZMQ_OPT_TIMEOUT);
						monitor.connect(addr);
						return true;
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}

	// 更新监视器事件
	public int updateMonitor(int timeout) {
		if (socket != null && monitor != null) {
			// 初始创建
			if (monitorPoll == null) {
				monitorPoll = new ZMQ.PollItem[] { new ZMQ.PollItem(monitor, ZMQ.Poller.POLLIN | ZMQ.Poller.POLLERR) };
			}

			try {
				int eventCnt = ZMQ.poll(monitorPoll, timeout);
				if (eventCnt > 0 && monitorPoll[0].isReadable()) {
					checkStream();
					int recvSize = monitor.recvZeroCopy(stream.getBuffer(), stream.getBuffer().capacity(), 0);
					stream.getBuffer().flip();

					// 分析事件数据
					Short events = 0;
					if (recvSize > 0) {
						events = HawkByteOrder.reverseShort(stream.readShort());
					}

					// 读取其他数据
					while (monitor.hasReceiveMore()) {
						checkStream();
						recvSize = monitor.recvZeroCopy(stream.getBuffer(), stream.getBuffer().capacity(), 0);
						stream.getBuffer().flip();
					}

					return events;
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return 0;
	}

	// 关闭事件监视器
	public boolean stopMonitor() {
		if (socket != null) {
			try {
				if (monitor != null) {
					monitor.close();
				}
				return socket.monitor("", 0);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return false;
	}
}
