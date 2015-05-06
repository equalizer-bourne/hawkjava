package org.hawk.script;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.cryption.HawkBase64;
import org.hawk.log.HawkLog;
import org.hawk.net.HawkNetManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkShutdownHook;
import org.hawk.os.HawkTime;
import org.hawk.service.HawkServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 脚本的http请求处理器
 * 
 * @author hawk
 */
public class HawkScriptHttpHandler implements HttpHandler {
	/**
	 * 配置日志对象
	 */
	static Logger logger = LoggerFactory.getLogger("Script");
	/**
	 * 是否挂起状态
	 */
	private boolean suspend = false;

	/**
	 * 脚本执行线程
	 * 
	 * @author hawk
	 */
	private class ScriptShellExecutor extends Thread {
		/**
		 *  参数
		 */
		String params;
		/**
		 *  http请求对象
		 */
		HttpExchange httpExchange;
		
		/**
		 * 构造
		 */
		ScriptShellExecutor(HttpExchange httpExchange, String params) {
			this.params = params;
			this.httpExchange = httpExchange;			
		}
		
		/**
		 * 线程执行
		 */
		@Override
		public void run() {
			String result = onShellCommand(HawkScriptManager.paramsToMap(params));
			if (result != null) {
				HawkScriptManager.sendResponse(httpExchange, result);
			}
		}
	}
	
	/**
	 * 处理http请求
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		// 白名单控制
		if (HawkScriptManager.getInstance().getScriptConfig().isWhiteIptables()) {
			String ip = httpExchange.getRemoteAddress().toString().split(":")[0].substring(1);
			if (!HawkNetManager.getInstance().checkWhiteIptables(ip)) {
				HawkLog.logPrintln(String.format("http request closed by white iptables, ipaddr: %s", ip));
				httpExchange.close();
				return;
			}
		}

		String result = "" + HawkTime.getTimeString() + ":\n\n";
		String uriInfo = httpExchange.getRequestURI().getQuery();
		String path = httpExchange.getRequestURI().getPath();
		String command = path.substring(1);
		String params = null;
		String user = null;

		// url携带参数分离解析
		if (uriInfo != null) {
			String[] querys = uriInfo.split("&");
			for (String query : querys) {
				String[] pair = query.split("=");
				if (pair.length == 2) {
					if (pair[0].equals("params")) {
						params = URLDecoder.decode(pair[1], "UTF-8");
					} else if (pair[0].equals("user")) {
						user = pair[1];
					}
				}
			}
		}

		// 必须使用登陆用户
		if (!HawkScriptManager.getInstance().checkUser(user)) {
			result += "the user is banned : " + user;
			HawkScriptManager.sendResponse(httpExchange, result);
			httpExchange.close();
			return;
		}
		
		if (command != null) {
			// 记录命令信息
			logger.info(String.format("script http request, command: %s, params: %s, user: %s", command, params, user));

			if (checkSysCommand(command)) {
				if (user != null && user.equals(HawkApp.getInstance().getAppCfg().getAdmin())) {
					// 挂起脚本服务
					if ("suspend".equals(command)) {
						suspend = true;
						HawkScriptManager.sendResponse(httpExchange, "hawk script handler suspend");
					} 

					// 恢复脚本服务
					if ("resume".equals(command)) {
						suspend = false;
						HawkScriptManager.sendResponse(httpExchange, "hawk script handler resume");
					} 

					// 重新加载脚本
					if ("reload".equals(command)) {
						result += HawkScriptManager.getInstance().loadAllScript();
						HawkScriptManager.sendResponse(httpExchange, result);
					} 

					// 更新逻辑服务组件
					if ("update_service".equals(command)) {
						if (HawkServiceManager.getInstance().update()) {
							HawkScriptManager.sendResponse(httpExchange, "update service successful");
						} else {
							HawkScriptManager.sendResponse(httpExchange, "update service failed");
						}
					} 

					// 更新配置文件
					if ("update_config".equals(command)) {
						if (HawkConfigManager.getInstance().updateReload()) {
							HawkScriptManager.sendResponse(httpExchange, "update config successful");
						} else {
							HawkScriptManager.sendResponse(httpExchange, "update config failed");
						}
					} 

					// 执行shell命令
					if ("shell".equals(command)) {
						new ScriptShellExecutor(httpExchange, params).start();
					} 

					// 退出服务
					if ("shutdown".equals(command)) {						
						HawkScriptManager.sendResponse(httpExchange, "hawk script shutdown");
						Map<String, String> paramsMap = HawkScriptManager.paramsToMap(params);
						if (paramsMap != null && paramsMap.containsKey("notify") && "false".equals(paramsMap.get("notify"))) {
							HawkShutdownHook.getInstance().processShutdown(false);
						} else {
							HawkShutdownHook.getInstance().processShutdown(true);
						}
					}
				}
			} else if (!suspend) {
				HawkScript script = HawkScriptManager.getInstance().getScript(command);
				if (script != null) {
					try {
						// 先日志记录
						script.logger(user, params);
	
						script.action(params, httpExchange);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				} else {
					result += "unkonwn command : " + command;
					HawkScriptManager.sendResponse(httpExchange, result);
				}
			}
		} else {
			result += "illicit command";
			HawkScriptManager.sendResponse(httpExchange, result);
			httpExchange.close();
		}
	}

	/**
	 * 执行shell命令
	 * 
	 * @param paramsMap
	 * @return
	 */
	public static String onShellCommand(Map<String, String> paramsMap) {
		if (paramsMap != null && paramsMap.containsKey("cmd")) {
			long timeout = -1;
			try {
				String urlCmd = paramsMap.get("cmd").replace('_', '/').replace('-', '=');
				String cmd = new String(HawkBase64.decode(urlCmd));
				if (paramsMap.containsKey("timeout")) {
					timeout = Integer.valueOf(paramsMap.get("timeout"));
				}
				return HawkApp.getInstance().onShellCommand(cmd, timeout);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return null;
	}

	/**
	 * 检测是否为系统命令
	 * 
	 * @param command
	 * @return
	 */
	private boolean checkSysCommand(String command) {
		if ("shell".equals(command) || "suspend".equals(command) || "resume".equals(command) || "reload".equals(command) || "shutdown".equals(command) || 
			"update_service".equals(command) || "update_config".equals(command)) {
			return true;
		}
		return false;
	}
}
