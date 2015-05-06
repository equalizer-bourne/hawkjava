package com.hawk.cdk;

import org.hawk.config.HawkXmlCfg;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.cdk.http.CdkHttpServer;

/**
 * CDK服务器
 * 
 * @author hawk
 */
public class Cdk {
	/**
	 * 系统token校验
	 */
	private static String httpToken = "";
	
	public static void setToken(String token) {
		httpToken = token.trim();
	}
	
	public static boolean checkToken(String token) {
		if (httpToken != null && httpToken.length() > 0) {
			if (token == null || !token.equals(httpToken)) {
				throw new RuntimeException("http token check failed.");
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		try {
			// 退出构造装载
			ShutDownHook.install();

			// 打印系统信息
			HawkOSOperator.printOsEnv();
			
			HawkXmlCfg conf = new HawkXmlCfg(System.getProperty("user.dir") + "/cfg/config.xml");

			// 初始化memcached客户端
			if (conf.containsKey("memcache.addr")) {
				int timeout = 0;
				if (conf.containsKey("memcache.timeout")) {
					timeout = conf.getInt("memcache.timeout");
				}
				
				if (!CdkServices.getInstance().initMC(conf.getString("memcache.addr"), timeout, 0)) {
					HawkLog.errPrintln("init memcached failed, addr: " + conf.getString("memcache.addr") + ", timeout: " + timeout);
					return;
				}
			} else {
				if (!CdkServices.getInstance().initMC(conf.getString("redis.addr"), 0, conf.getInt("redis.port"))) {
					HawkLog.errPrintln("init redis failed, addr: " + conf.getString("redis.addr") + ", port: " + conf.getString("redis.port"));
					return;
				}
			}

			CdkHttpServer cdkService = new CdkHttpServer();
			cdkService.setup(conf.getString("httpserver.addr"), conf.getInt("httpserver.port"), conf.getInt("httpserver.pool"));
			
			// 设置校验码
			if (conf.containsKey("httpserver.token")) {
				Cdk.setToken(conf.getString("httpserver.token"));
			}
			
			cdkService.run();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
