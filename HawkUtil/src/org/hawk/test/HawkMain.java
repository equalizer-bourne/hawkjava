package org.hawk.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ListModel;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.cryption.HawkAESCrypt;
import org.hawk.cryption.HawkDESCrypt;
import org.hawk.cryption.HawkRsaCrypt;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.rpc.HawkRpcServer;
import org.hawk.rpc.HawkRpcWorker;
import org.hawk.timer.HawkTimerEntry;
import org.hawk.timer.HawkTimerListener;
import org.hawk.timer.HawkTimerManager;
import org.hawk.util.services.HawkReportService;
import org.hawk.util.services.HawkReportService.CommonData;
import org.hawk.util.services.HawkReportService.LoginData;
import org.hawk.util.services.HawkReportService.RechargeData;
import org.hawk.util.services.HawkReportService.RegisterData;
import org.hawk.xid.HawkXID;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;
import org.hibernate.dialect.DB2390Dialect;
import org.hibernate.sql.Update;

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
			byte[] keyValue = new byte[] { 22, 25, -35, -45, 25, 98, -55, -45, 10, 35, -45, 25, 26, -95, 25, -65, -78, -99, 85, 45, -62, 10, -0, 11, -35, 48, -98, 65, -32, 14, -78, 25, 36, -56, -45, -45, 12, 15, -35, -75, 15, -14, 62, -25, 33, -45, 55, 68, -88 };

			HawkAESCrypt crypt1 = new HawkAESCrypt();
			crypt1.init(keyValue, true);
			byte[] encrypt = crypt1.digit("hawk".getBytes());

			HawkAESCrypt crypt2 = new HawkAESCrypt();
			crypt2.init(keyValue, false);
			System.out.println(new String(crypt2.digit(encrypt)));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private static void testReport() {
		HawkReportService.getInstance().install("lz", "ios", "1", "http://127.0.0.1:9010", 2000, null);
		/*
		 * RegisterData registerData = new RegisterData("91_123456", "abc-def", 1, ""); HawkReportService.getInstance().doReport(registerData);
		 * 
		 * LoginData loginData = new LoginData("91_123456", "abc-def", 1, ""); HawkReportService.getInstance().doReport(loginData);
		 * 
		 * RechargeData rechargeData = new RechargeData("91_123456", "abc-def", 1, "四叶草", 10, "1234567890", 1000, "rmb", ""); HawkReportService.getInstance().doReport(rechargeData);
		 * 
		 * CommonData commonData = new CommonData("91_123456", "abc-def", 1, ""); commonData.setArgs("10", "四叶草"); HawkReportService.getInstance().doReport(commonData);
		 */
	}

	public static void testEmail() {
		try {
			Email email = new SimpleEmail();
			email.setHostName("smtp.163.com");
			email.setSmtpPort(25);
			email.setAuthentication("hawkproject@163.com", "******");
			email.setCharset("UTF-8");
			email.setFrom("hawkproject@163.com");
			email.addTo("daijunhua@com4loves.com");
			email.setSubject("数据汇报邮件");
			email.setMsg("游戏数据: ");
			email.send();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private static void dutyOrder() {
		List<String> client = Arrays.asList("赵路", "周桐", "美琪");
		List<String> server = Arrays.asList("鹏飞", "徐林", "戴俊华");
		
		HawkRand.randomOrder(client);
		HawkRand.randomOrder(server);
		
		System.out.println(client);
		System.out.println(server);
	}
	
	/**
	 * 测试主函数入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new HawkApp(HawkXID.valueOf(0)) {
		};

		// System.out.println(HawkRsaCrypt.generateKeyPair(1024));
		
		dutyOrder();
		
		filterGameJs();
	}

	private static void filterGameJs() {
		try {
			List<String> fileLines = new LinkedList<String>();
			HawkOSOperator.readTextFileLines(System.getProperty("user.dir") + "/game.js", fileLines);
			int lineIndex = 0;
			for (int i=0; i<fileLines.size(); i++) {
				String lineContent = fileLines.get(i);
				int pos = lineContent.indexOf(".prototype.__class__");
				if (pos > 0 && lineContent.indexOf("\";") > pos) {
					String className = lineContent.substring(0, pos);
					String fileContent = "";
					for (int j=lineIndex; j<=i; j++) {
						lineContent = fileLines.get(j).trim();
						if (lineContent.length() > 0) {
							fileContent += fileLines.get(j);
							fileContent += "\r\n";
						}
					}
					// System.out.println(fileContent);
					saveAsFile(fileContent, "game/" + className + ".js");
					lineIndex = i + 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void saveAsFile(String contentItem, String filePath) throws IOException {
		// 打印文件名
		System.out.println(filePath);
		
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(contentItem);
		bw.close();
	}
}
