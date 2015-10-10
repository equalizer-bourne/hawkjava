package org.hawk.net;

import java.util.concurrent.atomic.AtomicLong;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkTickable;

import com.google.gson.JsonObject;

public class HawkNetStatistics extends HawkTickable {
	// 当前会话数
	protected AtomicLong curSession;
	// 峰值会话数
	protected AtomicLong peakSession;
	// 总接收字节数
	protected AtomicLong totalRecvBytes;
	// 总接收协议数
	protected AtomicLong totalRecvProto;
	// 总发送字节数
	protected AtomicLong totalSendBytes;
	// 总发送协议数
	protected AtomicLong totalSendProto;
	// 当前接收速率
	protected long curRecvRate;
	// 接收峰值速率(byte/s)
	protected long peakRecvRate;
	// 当前发送速率
	protected long curSendRate;
	// 发送峰值速率(byte/s)
	protected long peakSendRate;
	// 上一秒的总接收字节数
	private long lastTotalRecvBytes;
	// 上一秒的总发送字节数
	private long lastTotalSendBytes;
	// 速率统计时间
	private long rateRecordTime;
	// 信息日志记录时间
	private long infoLogTime;
	
	/**
	 * 实例对象
	 */
	private static HawkNetStatistics instance;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static HawkNetStatistics getInstance() {
		if (instance == null) {
			instance = new HawkNetStatistics();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private HawkNetStatistics() {
		totalSendBytes = new AtomicLong();
		totalSendProto = new AtomicLong();
		totalRecvBytes = new AtomicLong();
		totalRecvProto = new AtomicLong();
		curSession = new AtomicLong();
		peakSession = new AtomicLong();
		
		rateRecordTime = HawkTime.getMillisecond();
		infoLogTime = HawkTime.getMillisecond();
	}

	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		HawkApp.getInstance().addTickable(this);
		return true;
	}
	
	/**
	 * 帧更新统计信息
	 */
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		if (curTime - rateRecordTime >= 1000) {
			curRecvRate = totalRecvBytes.get() - lastTotalRecvBytes;
			curSendRate = totalSendBytes.get() - lastTotalSendBytes;
			
			peakRecvRate = Math.max(peakRecvRate, curRecvRate);
			peakSendRate = Math.max(peakSendRate, curSendRate);
			
			lastTotalRecvBytes = totalRecvBytes.get();
			lastTotalSendBytes = totalSendBytes.get();
			
			rateRecordTime = curTime;
		}
		
		if (curTime - infoLogTime >= 30000) {
			infoLogTime = curTime;
			HawkLog.logPrintln("net-statistics: " + toString());
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public String toString() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("curSession", getCurSession());
		jsonObject.addProperty("peakSession", getPeakSession());
		jsonObject.addProperty("totalRecvBytes", getTotalRecvBytes());
		jsonObject.addProperty("totalRecvProto", getTotalRecvProto());
		jsonObject.addProperty("totalSendBytes", getTotalSendBytes());
		jsonObject.addProperty("totalSendProto", getTotalSendProto());
		jsonObject.addProperty("curRecvRate", getCurRecvRate());
		jsonObject.addProperty("peakRecvRate", getPeakRecvRate());
		jsonObject.addProperty("curSendRate", getCurSendRate());
		jsonObject.addProperty("peakSendRate", getPeakSendRate());
		return jsonObject.toString();
	}
	
	/**
	 * 通知会话开启
	 */
	public void onSessionCreated() {
		long value = curSession.incrementAndGet();
		if (value > peakSession.get()) {
			peakSession.set(value);
		}
	}
	
	/**
	 * 通知会话关闭
	 */
	public void onSessionClosed() {
		curSession.decrementAndGet();
	}
	
	/**
	 * 获取当前会话数
	 * 
	 * @return
	 */
	public long getCurSession() {
		return curSession.get();
	}
	
	/**
	 * 获取会话数峰值
	 * 
	 * @return
	 */
	public long getPeakSession() {
		return peakSession.get();
	}
	
	/**
	 * 通知接收数据字节数
	 * 
	 * @param bytes
	 */
	public void onRecvBytes(int bytes) {
		totalRecvBytes.addAndGet(bytes);
	}
	
	/**
	 * 通知发送数据字节数
	 * 
	 * @param bytes
	 */
	public void onSendBytes(int bytes) {
		totalSendBytes.addAndGet(bytes);
	}

	/**
	 * 通知接收到协议
	 */
	public void onRecvProto() {
		totalRecvProto.incrementAndGet();
	}
	
	/**
	 * 通知发送协议
	 */
	public void onSendProto() {
		totalSendProto.incrementAndGet();
	}

	/**
	 * 获取总接收字节数
	 * 
	 * @return
	 */
	public long getTotalRecvBytes() {
		return totalRecvBytes.get();
	}

	/**
	 * 获取总接收协议数
	 * 
	 * @return
	 */
	public long getTotalRecvProto() {
		return totalRecvProto.get();
	}

	/**
	 * 获取总发送字节数
	 * 
	 * @return
	 */
	public long getTotalSendBytes() {
		return totalSendBytes.get();
	}

	/**
	 * 获取总发送协议数
	 * 
	 * @return
	 */
	public long getTotalSendProto() {
		return totalSendProto.get();
	}

	/**
	 * 获取接收字节当前速率
	 * 
	 * @return
	 */
	public long getCurRecvRate() {
		return curRecvRate;
	}

	/**
	 * 获取接收直接峰值速率
	 * 
	 * @return
	 */
	public long getPeakRecvRate() {
		return peakRecvRate;
	}

	/**
	 * 获取发送字节当前速率
	 * 
	 * @return
	 */
	public long getCurSendRate() {
		return curSendRate;
	}

	/**
	 * 获取发送直接峰值速率
	 * 
	 * @return
	 */
	public long getPeakSendRate() {
		return peakSendRate;
	}
}
