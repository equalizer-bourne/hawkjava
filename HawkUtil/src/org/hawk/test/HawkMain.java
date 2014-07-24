package org.hawk.test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.hawk.cryption.HawkAESCrypt;
import org.hawk.cryption.HawkDESCrypt;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.rpc.HawkRpcServer;
import org.hawk.rpc.HawkRpcWorker;
import org.hawk.timer.HawkTimerEntry;
import org.hawk.timer.HawkTimerListener;
import org.hawk.timer.HawkTimerManager;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.hibernate.dialect.DB2390Dialect;
import org.hibernate.sql.Update;

import antlr.collections.List;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xml.internal.serialize.LineSeparator;

@SuppressWarnings("unused")
public class HawkMain {
	private static class RpcWorker extends HawkRpcWorker {
		@Override
		public HawkProtocol response(HawkProtocol protocol) {
			return protocol;
		}
	}

	private static void testRpcServer() {
		// 添加库加载目录
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");

		// 初始化zmq管理器
		HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);

		HawkRpcServer rpcServer = new HawkRpcServer();
		if (rpcServer.init("tcp://*:9595", "tcp://*:9596", true)) {
			RpcWorker worker = new RpcWorker();
			if (worker.init("tcp://10.0.3.110:9596")) {
				rpcServer.addWorker(worker);
			}
		}

		while (true) {
			rpcServer.onTick();

			HawkOSOperator.osSleep(20);
		}
	}

	private static void testAlarm() {
		HawkTimerManager.getInstance().init(false);
		try {
			HawkTimerManager.getInstance().addAlarm("test", 5, true, new HawkTimerListener() {
				@Override
				public void handleAlarm(HawkTimerEntry entry) {
					System.err.println("Alarm: " + HawkTime.getTimeString());
				}
			});

			System.in.read();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private static void testCrypt() {
		try {
			// 用户密钥
			byte[] keyValue = new byte[] { 
					22, 25, -35, -45, 25, 98, -55, -45, 
					10, 35, -45, 25, 26, -95, 25, -65, 
					-78, -99, 85, 45, -62, 10, -0, 11, 
					-35, 48, -98, 65, -32, 14, -78, 25, 
					36, -56, -45, -45, 12, 15, -35, -75, 
					15, -14, 62, -25, 33, -45, 55, 68, -88
				};

			HawkAESCrypt crypt1 = new HawkAESCrypt();
			crypt1.init(keyValue, true);
			byte[] encrypt = crypt1.digit("hawk".getBytes());

			HawkAESCrypt crypt2 = new HawkAESCrypt();
			crypt2.init(keyValue, false);
			System.out.println(new String(crypt2.digit(encrypt)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 测试主函数入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	}
}
