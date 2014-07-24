package org.hawk.script;

import java.io.IOException;

import org.hawk.log.HawkLog;
import org.hawk.net.HawkNetManager;
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
						params = pair[1];
					} else if (pair[0].equals("user")) {
						user = pair[1];
					}
				}
			}
		}

		if (command != null) {
			// 记录命令信息
			logger.info(String.format("script http request, command: %s, params: %s, user: %s", command, params, user));

			if (checkSysCommand(command)) {
				if ("hawk".equals(user)) {
					if ("suspend".equals(command)) {
						suspend = true;
					} else if ("resume".equals(command)) {
						suspend = false;
					} else if ("reload".equals(command)) {
						result += HawkScriptManager.getInstance().loadAllScript();
						HawkScriptManager.sendResponse(httpExchange, result);
					} else if ("service_update".equals(command)) {
						HawkServiceManager.getInstance().update();
					}
				}
			} else if (!suspend) {
				HawkScript script = HawkScriptManager.getInstance().getScript(command);
				if (script != null) {
					// 先日志记录
					script.logger(user, params);

					result += script.action(params, httpExchange);
				} else {
					result += "unkonwn command :";
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
	 * 检测是否为系统命令
	 * 
	 * @param command
	 * @return
	 */
	private boolean checkSysCommand(String command) {
		if ("suspend".equals(command) || "resume".equals(command) || "reload".equals(command) || "service_update".equals(command)) {
			return true;
		}
		return false;
	}
}
