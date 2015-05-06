package com.hawk.game.gm;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;

import com.sun.net.httpserver.HttpExchange;

/**
 * 配置重新加载
 * 
 * @author hawk
 */
public class ShutdownCallbackHandler extends HawkScript {
	@Override
	public void action(String params, HttpExchange httpExchange) {
		try {
			HawkLog.logPrintln("script shutdown callback invoke");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}