package org.hawk.script;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.nativeapi.HawkNativeApi;
import org.hawk.net.HawkSession;
import org.hawk.net.HawkSessionHttpExchange;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScriptConfig.ScriptInfo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.tools.javac.Main;

/**
 * 脚本管理器
 * 
 * @author hawk
 */
public class HawkScriptManager {
	/**
	 * 服务器对象
	 */
	private HttpServer httpServer;
	/**
	 * 脚本配置文件
	 */
	private HawkScriptConfig scriptCfg;
	/**
	 * 编译器对象
	 */
	private static final Main main = new Main();
	/**
	 * 脚本映射表
	 */
	private ConcurrentHashMap<String, HawkScript> scriptMap = null;
	/**
	 * 单例使用
	 */
	private static HawkScriptManager instance;

	/**
	 * 获取全局管理器
	 * 
	 * @return
	 */
	public static HawkScriptManager getInstance() {
		if (instance == null) {
			instance = new HawkScriptManager();
		}
		return instance;
	}

	/**
	 * 构造函数
	 * 
	 * @param scriptCfg
	 */
	private HawkScriptManager() {
		this.scriptMap = new ConcurrentHashMap<String, HawkScript>();
	}

	/**
	 * 开启服务
	 */
	public boolean init(String cfgFile) {
		// 检测
		if (!HawkNativeApi.checkHawk()) {
			return false;
		}
		
		scriptCfg = new HawkScriptConfig(cfgFile);
		// 创建输出目录
		File scriptBin = new File(scriptCfg.getBaseOutDir());
		scriptBin.mkdir();
		
		String httpAddr = scriptCfg.getHttpAddr();
		try {
			loadAllScript();

			if (httpAddr != null && httpAddr.length() > 0) {
				String[] addrInfo = httpAddr.split(":");
				if (addrInfo != null && addrInfo.length == 2) {
					httpServer = HttpServer.create(new InetSocketAddress(addrInfo[0], Integer.valueOf(addrInfo[1])), 0);
					httpServer.createContext("/", new HawkScriptHttpHandler());
					httpServer.setExecutor(Executors.newFixedThreadPool(2));
					httpServer.start();
					HawkLog.logPrintln(String.format("init script http, addr: %s", httpAddr));
					return true;
				}
			}
		} catch (BindException e) {
			HawkLog.errPrintln("script http command bind failed, address: " + httpAddr);
			HawkException.catchException(e);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 关闭脚本服务器
	 */
	public void close() {
		if (httpServer != null) {
			httpServer.stop(0);
			httpServer = null;
		}
	}

	/**
	 * 重启http服务器
	 */
	public void restart() {
		if (httpServer != null) {
			httpServer.stop(0);
			
			String httpAddr = scriptCfg.getHttpAddr();
			try {
				if (httpAddr != null && httpAddr.length() > 0) {
					String[] addrInfo = httpAddr.split(":");
					if (addrInfo != null && addrInfo.length == 2) {
						httpServer = HttpServer.create(new InetSocketAddress(addrInfo[0], Integer.valueOf(addrInfo[1])), 0);
						httpServer.createContext("/", new HawkScriptHttpHandler());
						httpServer.setExecutor(null);
						httpServer.start();
						HawkLog.logPrintln(String.format("restart script http, addr: %s", httpAddr));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 获取脚本对象
	 * 
	 * @param scriptId
	 * @return
	 */
	public HawkScript getScript(String scriptId) {
		return (HawkScript) scriptMap.get(scriptId);
	}

	/**
	 * 获取脚本配置
	 * 
	 * @return
	 */
	public HawkScriptConfig getScriptConfig() {
		return scriptCfg;
	}

	/**
	 * 加载所有脚本文件
	 * 
	 * @return
	 */
	public String loadAllScript() {
		String loadResult = "";
		this.scriptMap.clear();
		HawkLog.logPrintln("parse script/script.xml config");
		this.scriptCfg = new HawkScriptConfig(System.getProperty("user.dir") + "/script/script.xml");
		for (Map.Entry<String, ScriptInfo> entry : scriptCfg.getScriptMap().entrySet()) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter printWriter = new PrintWriter(baos);
				// 编译
				HawkLog.logPrintln("compile script, id: " + entry.getValue().getId() + ", class: " + entry.getValue().getClassName());
				Object result = compileAndLoadScript(entry.getValue().getClassName(), printWriter);
				if (result == null) {
					String error = new String(baos.toByteArray(), "UTF-8");
					loadResult += "script load failed, scriptid: " + entry.getKey() + ", class: " + entry.getValue() + "\n\n";
					loadResult += error;

					HawkLog.errPrintln("script load failed, scriptid: " + entry.getKey() + ", class: " + entry.getValue() + ", error: " + error);
				}

				printWriter.close();
				baos.close();
				loadResult += "script load success, scriptid: " + entry.getKey() + ", class: " + entry.getValue() + "\n\n";

				HawkScript script = (HawkScript) result;
				if (script != null) {
					scriptMap.put(entry.getKey(), script);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				loadResult += HawkException.formatStackMsg(e) + "\n";
			}
		}
		return loadResult;
	}

	/**
	 * 自动脚本运行
	 * 
	 * @return
	 */
	public int autoRunScript() {
		int count = 0;
		if (scriptCfg != null && scriptCfg.getScriptMap() != null) {
			for (Map.Entry<String, ScriptInfo> entry : scriptCfg.getScriptMap().entrySet()) {
				if (entry.getValue().isAutoRun()) {
					HawkScript script = HawkScriptManager.getInstance().getScript(entry.getKey());
					if (script != null) {
						try {
							script.action("", null);
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			}
		}
		return count;
	}
	
	/**
	 * 编译脚本文件
	 * 
	 * @param className
	 * @param printWriter
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "static-access", "resource" })
	private Object compileAndLoadScript(String className, PrintWriter printWriter) throws Exception {
		String filePath = scriptCfg.getBaseSrcDir() + "/" + className.replace(".", "/") + ".java";
		File srcFile = new File(filePath);
		if (!srcFile.exists()) {
			HawkLog.errPrintln("script: " + filePath.toLowerCase().replace("\\", "/") + " not exist");
			return null;
		}

		int result = main.compile(new String[] { filePath, "-encoding", "UTF-8", "-d", scriptCfg.getBaseOutDir() }, printWriter);
		if (result != 0) {
			return null;
		}

		String urlStr = null;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			urlStr = "file:/" + scriptCfg.getBaseOutDir() + "/";
		} else {
			urlStr = "file:" + scriptCfg.getBaseOutDir() + "/";
		}
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { new URL(urlStr) });
		Class<?> classObject = urlClassLoader.loadClass(className);
		return classObject.newInstance();
	}

	/**
	 * 参数解析
	 * 
	 * @param paramsInfo
	 * @return
	 */
	public static Map<String, String> paramsToMap(String paramsInfo) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (paramsInfo != null && paramsInfo.length() > 0) {
			String[] paramsArray = paramsInfo.split(";");
			try {
				for (String params : paramsArray) {
					String[] ps = params.split(":");
					if (ps.length == 2) {
						paramsMap.put(ps[0], ps[1]);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return paramsMap;
	}

	/**
	 * 统一的回应消息发送接口
	 * 
	 * @param httpExchange
	 * @param result
	 */
	public static void sendResponse(HttpExchange httpExchange, String result) {
		if (httpExchange != null) {
			try {
				// 是会话协议格式的请求
				if (httpExchange instanceof HawkSessionHttpExchange) {
					HawkSession session = (HawkSession) httpExchange.getAttribute("session");
					session.sendProtocol(HawkProtocol.valueOf(0, result.getBytes("UTF-8")));
					return;
				}
				
				byte[] bytes = result.getBytes("UTF-8");
				httpExchange.sendResponseHeaders(200, bytes.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(bytes);
				os.close();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 脚本执行用户校验
	 * 
	 * @param user
	 * @return
	 */
	public boolean checkUser(String user) {
		if (user != null && user.length() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 处理系统协议
	 * 
	 * @param protocol
	 * @return
	 */
	public boolean onSysProtocol(HawkProtocol protocol) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(new String(protocol.getOctets().getBuffer().array(), 0, protocol.getSize(), "UTF-8"));
			String user = jsonObject.getString("user").trim();
			if (user != null && user.equals("hawk")) {
				String action = jsonObject.getString("action").trim();
				if (action.equals("list_script")) {
					String scriptInfo = scriptCfg.toJsonInfo().toString();
					protocol.getSession().sendProtocol(HawkProtocol.valueOf(0, scriptInfo.getBytes("UTF-8")));
					return true;
				} else if (action.equals("run_script")) {
					String scriptId = jsonObject.getString("script").trim();
					String params = "";
					if (jsonObject.containsKey("params")) {
						params = jsonObject.getString("params").trim();
					}
					HawkScript script = getScript(scriptId);
					if (script != null) {
						script.action(params, new HawkSessionHttpExchange(protocol.getSession()));
					}
					return true;
				} else if (action.equals("run_shell")) {
					String cmd = jsonObject.getString("cmd").trim();
					long timeout = -1;
					if (jsonObject.containsKey("timeout")) {
						timeout = jsonObject.getLong("timeout");
					}
					// 执行系统shell命令
					String result = HawkApp.getInstance().onShellCommand(cmd, timeout);
					if (result != null) {
						protocol.getSession().sendProtocol(HawkProtocol.valueOf(0, result.getBytes("UTF-8")));
					}
					return true;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		return false;
	}
}
