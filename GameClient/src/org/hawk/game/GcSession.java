package org.hawk.game;

import java.io.UnsupportedEncodingException;

import org.hawk.cryption.HawkDecryption;
import org.hawk.cryption.HawkEncryption;
import org.hawk.log.HawkLog;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.protocol.SysProtocol.DataWarpper;

public class GcSession extends HawkClientSession {
	public GcSession(boolean cryption) {
		super();

		if (cryption) {
			setEncryption(new HawkEncryption());
			setDecryption(new HawkDecryption());
		}
	}

	@Override
	protected void onReceived(Object message) {
		if (message instanceof HawkProtocol) {
			HawkProtocol protocol = (HawkProtocol) message;
			DataWarpper data = protocol.parseProtocol(DataWarpper.getDefaultInstance());
			try {
				HawkLog.logPrintln("Recv: " + data.getData().toString("utf-8"));
			} catch (UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}
		}
	}
}
