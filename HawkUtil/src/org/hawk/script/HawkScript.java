package org.hawk.script;

import com.sun.net.httpserver.HttpExchange;

/**
 * 脚本接口
 * 
 * @author hawk
 */
public interface HawkScript {
	/**
	 * 日志记录
	 * 
	 * @param user
	 * @param params
	 */
	public void logger(String user, String params);

	/**
	 * 响应行为
	 * 
	 * @param params
	 * @param httpExchange
	 * @return
	 */
	public String action(String params, HttpExchange httpExchange);
}
