package org.hawk.game;

import java.util.LinkedList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.rpc.HawkRpcClient;
import org.hawk.rpc.HawkRpcWorker;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.google.protobuf.ByteString;
import com.hawk.game.protocol.SysProtocol.DataWarpper;

public class GcMain {
	public static class RpcWorker extends HawkRpcWorker {
		@Override
		public HawkProtocol response(HawkProtocol protocol) {
			return protocol;
		}
	}
	
	public static void test_RpcClient() {
		HawkRpcClient rpcClient = new HawkRpcClient();
		if (rpcClient.init("tcp://10.0.3.110:9595")) {
			while (true) {
				try {
					DataWarpper.Builder builder = DataWarpper.newBuilder();
					String value = HawkOSOperator.randomString(64);
					builder.setData(ByteString.copyFrom(value, "utf-8"));
					HawkProtocol response = rpcClient.request(new HawkProtocol(1, builder), 10000);
					if (response != null) {
						DataWarpper data = response.parseProtocol(DataWarpper.getDefaultInstance());
						System.out.println("Response: " + data.getData().toString("utf-8"));
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		// 打印启动参数
		for (int i = 0; i < args.length; i++) {
			HawkLog.logPrintln(args[i]);
		}
		
		// 添加库加载目录
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");
		
		// 初始化zmq管理器
		HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);
		
		test_RpcClient();
		
		// 获取参数
		String ip = "127.0.0.1";
		if (args.length > 0) {
			ip = args[0];
		}
		
		int port = 9595;
		if (args.length > 1) {
			port = Integer.valueOf(args[1]);
		}
		
		int timeout = 5000;
		if (args.length > 2) {
			timeout = Integer.valueOf(args[2]);
		}
		
		int count = 1;
		if (args.length > 3) {
			count = Integer.valueOf(args[3]);
		}
		
		int sleep = 1000;
		if (args.length > 4) {
			sleep = Integer.valueOf(args[4]);
		}
		
		List<GcSession> sessions = new LinkedList<>();
		try {
			for (int i = 0; i < count; i++) {
				GcSession session = new GcSession(false);
				boolean succ = session.connect(ip, port, timeout);
				if (succ) {
					sessions.add(session);
				} else {
					HawkLog.errPrintln("connect failed");
				}
			}

			while (true) {
				for (GcSession session : sessions) {
					DataWarpper.Builder builder = DataWarpper.newBuilder();
					String value = HawkOSOperator.randomString(64);
					builder.setData(ByteString.copyFrom(value, "utf-8"));
					session.sendProtocol(new HawkProtocol(1, builder));
				}
				HawkOSOperator.osSleep(sleep);
			}

		} catch (Exception e) {
		}
	}
}
