package org.hawk.script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hawk.os.HawkException;

/**
 * 脚本配置文件
 * 
 * @author hawk
 */
public class HawkScriptConfig {
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
	private Map<String, String> idNameMap;

	/**
	 * 获取http地址
	 * 
	 * @return
	 */
	public String getHttpAddr() {
		return httpAddr;
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
	public Map<String, String> getIdNameMap() {
		return idNameMap;
	}

	/**
	 * 通过id找到类名
	 * 
	 * @param id
	 * @return
	 */
	public String getNameById(String id) {
		return idNameMap.get(id);
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
		idNameMap = new HashMap<String, String>();
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
				Element element = itemIterator.next();
				String id = element.attributeValue("id");
				String cName = element.attributeValue("className");
				idNameMap.put(id, cName);
			}
		} catch (DocumentException e) {
			HawkException.catchException(e);
		}
	}
}
