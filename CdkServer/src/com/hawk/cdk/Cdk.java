package com.hawk.cdk;

import org.apache.commons.configuration.XMLConfiguration;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.danga.MemCached.SockIOPool;
import com.hawk.cdk.http.CdkHttpServer;

/**
 * CDK服务器
 * 
 * @author hawk
 */
public class Cdk {
	public static void main(String[] args) {
		try {
			// 退出构造装载
			ShutDownHook.install();

			// 打印系统信息
			HawkOSOperator.printOsEnv();
			
			XMLConfiguration conf = new XMLConfiguration(System.getProperty("user.dir") + "/cfg/config.xml");

			SockIOPool pool = SockIOPool.getInstance();
			pool.setServers(new String[] { conf.getString("memcached") });
			pool.initialize();

			// 初始化memcached客户端
			CdkServices.getInstance().initMC();

			CdkHttpServer cdkService = new CdkHttpServer();
			cdkService.setup(conf.getString("httpserver.addr"), conf.getInt("httpserver.port"), conf.getInt("httpserver.pool"));
			cdkService.run();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
