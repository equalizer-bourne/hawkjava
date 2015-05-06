package org.hawk.script;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hawk.os.HawkException;

import com.google.gson.JsonObject;

/**
 * 脚本配置文件
 * 
 * @author hawk
 */
public class HawkScriptConfig {
	/**
	 * 脚本信息
	 * @author hawk
	 */
	public class ScriptInfo {
		protected String  id;
		protected String  classNme;
		protected boolean autoRun = false;
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getClassName() {
			return classNme;
		}
		
		public void setClassName(String classNme) {
			this.classNme = classNme;
		}
		
		public boolean isAutoRun() {
			return autoRun;
		}
		
		public void setAutoRun(boolean autoRun) {
			this.autoRun = autoRun;
		}
	}
	
	/**
	 * http地址
	 */
	private String httpAddr;
	/**
	 * 基础源码目录
	 */
	private String baseSrcDir;
	/**
	 * 基础输出目录
	 */
	private String baseOutDir;
	/**
	 * 是否白名单控制
	 */
	private boolean whiteIptables;
	/**
	 * 类名和命令id的映射表
	 */
	private Map<String, ScriptInfo> scriptMap;

	/**
	 * 获取http地址
	 * 
	 * @return
	 */
	public String getHttpAddr() {
		return httpAddr;
	}

	/**
	 * 获取http端口
	 * 
	 * @return
	 */
	public int getHttpPort() {
		String[] items = httpAddr.split(":");
		try {
			if (items.length > 1) {
				return Integer.valueOf(items[1]);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 获取基础源码路径
	 * 
	 * @return
	 */
	public String getBaseSrcDir() {
		return baseSrcDir;
	}

	/**
	 * 获取基础输出目录
	 * 
	 * @return
	 */
	public String getBaseOutDir() {
		return baseOutDir;
	}

	/**
	 * 获取id&类名映射表
	 * 
	 * @return
	 */
	public Map<String, ScriptInfo> getScriptMap() {
		return scriptMap;
	}

	/**
	 * 获取脚本信息
	 * 
	 * @return
	 */
	public JsonObject toJsonInfo() {
		JsonObject jsonObject = new JsonObject();
		try {
			for (Entry<String, ScriptInfo> entry : scriptMap.entrySet()) {
				jsonObject.addProperty(entry.getValue().getId(), entry.getValue().getClassName());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return jsonObject;
	}
	
	/**
	 * 通过id找到类名
	 * 
	 * @param id
	 * @return
	 */
	public ScriptInfo getScriptById(String id) {
		return scriptMap.get(id);
	}

	/**
	 * 是否受白名单控制
	 * @return
	 */
	public boolean isWhiteIptables() {
		return whiteIptables;
	}

	/**
	 * 设置受白名单控制
	 * @param whiteIptables
	 */
	public void setWhiteIptables(boolean whiteIptables) {
		this.whiteIptables = whiteIptables;
	}
	
	/**
	 * 初始化构造
	 * 
	 * @param xmlPath
	 */
	@SuppressWarnings("unchecked")
	public HawkScriptConfig(String xmlPath) {
		scriptMap = new ConcurrentHashMap<String, ScriptInfo>();
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(xmlPath);
			Element rootElement = document.getRootElement();

			this.httpAddr = rootElement.attributeValue("httpAddr");
			this.baseOutDir = rootElement.attributeValue("baseOutDir");
			this.baseSrcDir = rootElement.attributeValue("baseSrcDir");
			
			String basePath = System.getProperty("user.dir");
			baseOutDir = baseOutDir.replace("${app}", basePath);
			baseSrcDir = baseSrcDir.replace("${app}", basePath);

			if (rootElement.attribute("whiteIptables") != null) {
				whiteIptables = Boolean.valueOf(rootElement.attributeValue("whiteIptables"));
			}
			
			Iterator<Element> itemIterator = rootElement.elementIterator();
			while (itemIterator.hasNext()) {
				ScriptInfo scriptInfo = new ScriptInfo();
				Element element = itemIterator.next();
				scriptInfo.id = element.attributeValue("id");
				scriptInfo.classNme = element.attributeValue("className");
				if (element.attribute("autoRun") != null) {
					scriptInfo.autoRun = Boolean.valueOf(element.attributeValue("autoRun"));
				}
				scriptMap.put(scriptInfo.id, scriptInfo);
			}
		} catch (DocumentException e) {
			HawkException.catchException(e);
		}
	}
}
