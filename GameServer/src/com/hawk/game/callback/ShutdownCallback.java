package com.hawk.game.callback;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptManager;
import org.hawk.util.HawkCallback;

public class ShutdownCallback extends HawkCallback {
	// 停服回调
	public int invoke(Object args) {
		try {
			HawkScript script = HawkScriptManager.getInstance().getScript("onshutdown");
			if (script != null) {
				script.action(null, null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
}
