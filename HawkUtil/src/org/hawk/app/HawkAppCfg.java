package org.hawk.app;

import org.hawk.config.HawkConfigBase;

/**
 * 应用配置类
 * 
 * @author hawk
 * 
 */
public class HawkAppCfg extends HawkConfigBase {
	/**
	 * 是否允许控制台打印
	 */
	public final boolean console = true;
	/**
	 * 调试模式
	 */
	public final boolean isDebug = true;
	/**
	 * 帧更新周期
	 */
	public final int tickPeriod = 50;
	/**
	 * 逻辑线程数
	 */
	public final int threadNum = 4;
	/**
	 * 系统时间差异
	 */
	public final long calendarOffset = 0;
	/**
	 * 完了接收器端口
	 */
	public final int acceptorPort = 9595;
	/**
	 * 会话缓冲区大小
	 */
	public final int sessionBuffSize = 4096;
	/**
	 * 会话空闲超时
	 */
	public final int sessionIdleTime = 0;
	/**
	 * 会话协议频率
	 */
	public final int sessionPPS = 0;
	/**
	 * 会话加密
	 */
	public final boolean sessionEncryption = false;
	/**
	 * 会话数限制
	 */
	public final int sessionMaxSize = 0;
	/**
	 * mina的ioFilter线程数
	 */
	public final int ioFilterChain = 0;
	/**
	 * 数据库连接hbm配置
	 */
	public final String dbHbmXml = null;
	/**
	 * 数据库连接地址
	 */
	public final String dbConnUrl = null;
	/**
	 * 数据库连接用户名
	 */
	public final String dbUserName = null;
	/**
	 * 数据库连接密码
	 */
	public final String dbPassWord = null;
	/**
	 * 数据库实体包路径
	 */
	public final String entityPackages = null;
	/**
	 * 数据库异步落地线程周期
	 */
	public final int dbAsyncPeriod = 300000;
	/**
	 * 脚本配置文件
	 */
	public final String scriptXml = null;
	/**
	 * 配置文件包名
	 */
	public final String configPackages = null;
	/**
	 * sercice服务jar包路径
	 */
	public final String servicePath = null;
}
