package org.hawk.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

/**
 * 消息对象, 做对象事件驱动
 * 
 * @author hawk
 * 
 */
public class HawkMsg {
	/**
	 * 普通消息类型
	 */
	public static int MSG_NORMAL = 0;
	/**
	 * rpc请求消息
	 */
	public static int MSG_RPC_REQ = 1;
	/**
	 * rpc响应消息
	 */
	public static int MSG_RPC_RESP = 2;
	
	/**
	 * rpc共享id
	 */
	private static AtomicInteger msgRpcId = new AtomicInteger();
	
	/**
	 * 消息类型
	 */
	private int type;
	/**
	 * 消息数据信息
	 */
	private int msg;
	/**
	 * 消息发送时间
	 */
	private long time;
	/**
	 * 消息目标ID
	 */
	private HawkXID target;
	/**
	 * 消息来源ID
	 */
	private HawkXID source;
	/**
	 * 消息参数列表
	 */
	private List<Object> params;
	/**
	 * 用户数据
	 */
	private Object userData;
	/**
	 * 消息调用堆栈
	 */
	private String stackTrace;
	/**
	 * 全局请求id
	 */
	private int rpcId;
	
	/**
	 * 消息构造函数, 外部不可见
	 * 
	 * @param msg
	 */
	protected HawkMsg(int msg) {
		this.msg = msg;
		this.setTime(HawkTime.getMillisecond());

		if (HawkApp.getInstance().isDebug()) {
			stackTrace = HawkException.formatStackTrace(Thread.currentThread().getStackTrace(), 3);
		}
	}

	/**
	 * 获取消息类型
	 * 
	 * @return
	 */
	public int getMsg() {
		return msg;
	}

	/**
	 * 设置消息类型
	 * 
	 * @param msg
	 */
	public void setMsg(int msg) {
		this.msg = msg;
	}

	/**
	 * 设置消息目标对象ID
	 * 
	 * @param xid
	 */
	public HawkMsg setTarget(HawkXID xid) {
		if (target != null) {
			target.set(xid);
		} else {
			target = xid.clone();
		}
		return this;
	}

	/**
	 * 获取消息目标对象ID
	 * 
	 * @return
	 */
	public HawkXID getTarget() {
		return target;
	}

	/**
	 * 设置消息源对象ID
	 * 
	 * @param xid
	 */
	public HawkMsg setSource(HawkXID xid) {
		if (source != null) {
			source.set(xid);
		} else {
			source = xid.clone();
		}
		return this;
	}

	/**
	 * 获取消息源对象ID
	 * 
	 * @return
	 */
	public HawkXID getSource() {
		return source;
	}

	/**
	 * 获取消息产生时间
	 * 
	 * @return
	 */
	public long getTime() {
		return time;
	}

	/**
	 * 设置消息产生时间
	 * 
	 * @param time
	 */
	public HawkMsg setTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * 消息是否有效
	 * 
	 * @return
	 */
	public boolean isValid() {
		return msg > 0 && target.isValid();
	}

	/**
	 * 添加参数
	 * 
	 * @param params
	 */
	public HawkMsg pushParam(Object... params) {
		if (this.params == null) {
			this.params = new ArrayList<Object>(params.length);
		}
		this.params.addAll(Arrays.asList(params));
		return this;
	}

	/**
	 * 获取指定索引参数
	 * 
	 * @param idx
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParam(int idx) {
		return (T) params.get(idx);
	}

	/**
	 * 获取参数列表
	 * 
	 * @return
	 */
	public List<Object> getParams() {
		return params;
	}
	
	/**
	 * 打印调用堆栈
	 */
	public void printStackTrace() {
		if (stackTrace != null) {
			HawkLog.logPrintln(stackTrace);
		}
	}

	/**
	 * 获取用户数据
	 * 
	 * @return
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * 设置用户数据
	 * 
	 * @param userData
	 */
	public HawkMsg setUserData(Object userData) {
		this.userData = userData;
		return this;
	}

	/**
	 * 获取保留标记
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 设置保留标记
	 * 
	 * @param type
	 */
	public HawkMsg setType(int type) {
		this.type = type;
		return this;
	}
	
	/**
	 * 获取请求id
	 * 
	 * @return
	 */
	public int getRpcId() {
		return rpcId;
	}

	/**
	 * 设置请求id
	 * 
	 * @param rpcId
	 */
	public HawkMsg setRpcId(int rpcId) {
		this.rpcId = rpcId;
		return this;
	}
	
	/**
	 * 构建rpc消息, 返回rpcid
	 * @return
	 */
	public int buildRpcMsg() {
		type = HawkMsg.MSG_RPC_REQ;
		rpcId = msgRpcId.incrementAndGet();
		return rpcId;
	}
	
	/**
	 * 创建对象
	 * 
	 * @param msg
	 * @return
	 */
	public static HawkMsg valueOf(int msg) {
		HawkMsg hawkMsg = new HawkMsg(msg);
		return hawkMsg;
	}

	/**
	 * 创建消息对象
	 * 
	 * @param msg
	 * @return
	 */
	public static HawkMsg valueOf(int msg, HawkXID target) {
		HawkMsg hawkMsg = new HawkMsg(msg);
		hawkMsg.setTarget(target);
		return hawkMsg;
	}

	/**
	 * 创建消息对象
	 * 
	 * @param msg
	 * @return
	 */
	public static HawkMsg valueOf(int msg, HawkXID target, HawkXID source) {
		HawkMsg hawkMsg = new HawkMsg(msg);
		hawkMsg.setTarget(target);
		hawkMsg.setSource(source);
		return hawkMsg;
	}
}
