package org.hawk.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.cache.HawkCacheObj;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

/**
 * 消息对象, 做对象事件驱动
 * 
 * @author hawk
 * 
 */
public class HawkMsg extends HawkCacheObj {
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
	 * 消息标记
	 */
	private int flag;

	/**
	 * 消息构造函数, 外部不可见
	 * 
	 * @param msg
	 */
	protected HawkMsg(int msg) {
		this.msg = msg;
		this.setTime(HawkTime.getMillisecond());
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
	public void setTarget(HawkXID xid) {
		if (target != null) {
			target.set(xid);
		} else {
			target = xid;
		}
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
	public void setSource(HawkXID xid) {
		if (source != null) {
			source.set(xid);
		} else {
			source = xid;
		}
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
	 * @return
	 */
	public long getTime() {
		return time;
	}

	/**
	 * 设置消息产生时间
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}
	
	/**
	 * 设置消息标记
	 * 
	 * @param xid
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * 获取消息标记
	 * 
	 * @return
	 */
	public int getFlag() {
		return flag;
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
	public void pushParam(Object... params) {
		if (this.params == null) {
			this.params = new ArrayList<Object>(params.length);
		}
		this.params.addAll(Arrays.asList(params));
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
	 * 克隆对象
	 */
	@Override
	protected HawkCacheObj clone() {
		return new HawkMsg(msg);
	}

	/**
	 * 清理数据
	 */
	@Override
	protected boolean clear() {
		flag = 0;
		setTime(0);
		target.clear();
		source.clear();
		params.clear();
		return true;
	}
}
