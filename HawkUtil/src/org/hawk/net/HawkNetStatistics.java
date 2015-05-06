package org.hawk.net;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkTickable;

import com.google.gson.JsonObject;

public class HawkNetStatistics extends HawkTickable {
	// 当前会话数
	protected AtomicInteger curSession;
	// 峰值会话数
	protected AtomicInteger peakSession;
	// 总接收字节数
	protected AtomicInteger totalRecvBytes;
	// 总接收协议数
	protected AtomicInteger totalRecvProto;
	// 总发送字节数
	protected AtomicInteger totalSendBytes;
	// 总发送协议数
	protected AtomicInteger totalSendProto;
	// 当前接收速率
	protected int curRecvRate;
	// 接收峰值速率(byte/s)
	protected int peakRecvRate;
	// 当前发送速率
	protected int curSendRate;
	// 发送峰值速率(byte/s)
	protected int peakSendRate;
	// 上一秒的总接收字节数
	private int lastTotalRecvBytes;
	// 上一秒的总发送字节数
	private int lastTotalSendBytes;
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
		totalSendBytes = new AtomicInteger();
		totalSendProto = new AtomicInteger();
		totalRecvBytes = new AtomicInteger();
		totalRecvProto = new AtomicInteger();
		curSession = new AtomicInteger();
		peakSession = new AtomicInteger();
		
		rateRecordTime = HawkTime.getMillisecond();
		infoLogTime = HawkTime.getMillisecond();
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
		int value = curSession.incrementAndGet();
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
	public int getCurSession() {
		return curSession.get();
	}
	
	/**
	 * 获取会话数峰值
	 * 
	 * @return
	 */
	public int getPeakSession() {
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
	public int getTotalRecvBytes() {
		return totalRecvBytes.get();
	}

	/**
	 * 获取总接收协议数
	 * 
	 * @return
	 */
	public int getTotalRecvProto() {
		return totalRecvProto.get();
	}

	/**
	 * 获取总发送字节数
	 * 
	 * @return
	 */
	public int getTotalSendBytes() {
		return totalSendBytes.get();
	}

	/**
	 * 获取总发送协议数
	 * 
	 * @return
	 */
	public int getTotalSendProto() {
		return totalSendProto.get();
	}

	/**
	 * 获取接收字节当前速率
	 * 
	 * @return
	 */
	public int getCurRecvRate() {
		return curRecvRate;
	}

	/**
	 * 获取接收直接峰值速率
	 * 
	 * @return
	 */
	public int getPeakRecvRate() {
		return peakRecvRate;
	}

	/**
	 * 获取发送字节当前速率
	 * 
	 * @return
	 */
	public int getCurSendRate() {
		return curSendRate;
	}

	/**
	 * 获取发送直接峰值速率
	 * 
	 * @return
	 */
	public int getPeakSendRate() {
		return peakSendRate;
	}
}
