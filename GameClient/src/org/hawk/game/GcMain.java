package org.hawk.game;

import java.util.LinkedList;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.protobuf.ByteString;
import com.hawk.game.protocol.SysProtocol.HPDataWarpper;

public class GcMain {
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			HawkLog.logPrintln(args[i]);
		}
		HawkOSOperator.addUsrPath(System.getProperty("user.dir") + "/lib");
		
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
					HPDataWarpper.Builder builder = HPDataWarpper.newBuilder();
					String value = HawkOSOperator.randomString(64);
					builder.setData(ByteString.copyFrom(value, "utf-8"));
					session.sendProtocol(HawkProtocol.valueOf(1, builder));
				}
				HawkOSOperator.osSleep(sleep);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}