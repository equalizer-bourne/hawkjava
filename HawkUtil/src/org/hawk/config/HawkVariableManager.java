package org.hawk.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

/**
 * 配置文件变量管理器
 * 
 * @author hawk
 */
public class HawkVariableManager {
	/**
	 * 变量文件
	 */
	private Map<String, String> variableFiles;
	/**
	 * 变量表
	 */
	private Map<String, String> variableMap;
	
	/**
	 * 配置管理器实例
	 */
	private static HawkVariableManager instance;

	/**
	 * 获取配置管理器实例
	 * 
	 * @return
	 */
	public static HawkVariableManager getInstance() {
		if (instance == null) {
			instance = new HawkVariableManager();
		}
		return instance;
	}
	
	/**
	 * 默认构造
	 */
	private HawkVariableManager() {
		variableFiles = new ConcurrentHashMap<String, String>();
		variableMap = new ConcurrentHashMap<String, String>();
	}
	
	/**
	 * 获取参数(字符串)
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return variableMap.get(key);
	}
	
	/**
	 * 获取参数(整数)
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		String var = getString(key);
		if (var != null) {
			return Integer.valueOf(var);
		}
		throw new RuntimeException("variable fault");
	}
	
	/**
	 * 获取参数(浮点)
	 * 
	 * @param key
	 * @return
	 */
	public float getFloat(String key) {
		String var = getString(key);
		if (var != null) {
			return Float.valueOf(var);
		}
		throw new RuntimeException("variable fault");
	}
	
	/**
	 * 获取参数(布尔)
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		String var = getString(key);
		if (var != null) {
			return Boolean.valueOf(var);
		}
		throw new RuntimeException("variable fault");
	}

	/**
	 * 注册变量
	 * 
	 * @param filePath
	 * @return
	 */
	public boolean registerFiles(String filePath) {
		if (readVariable(filePath)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 更新所有已注册的变量
	 * 
	 * @param filePath
	 * @return
	 */
	public void updateVariable() {
		for (Entry<String, String> entry : variableFiles.entrySet()) {
			if (readVariable(entry.getKey())) {
				HawkLog.logPrintln("update variable success: " + entry.getKey());
			} else {
				HawkLog.logPrintln("update variable failed: " + entry.getKey());
			}
		}		
	}
	
	/**
	 * 从文件中读取变量
	 * 
	 * @param file
	 * @return
	 */
	private boolean readVariable(String filePath) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				HawkLog.logPrintln("variable file not exist: " + filePath);
				return false;
			}

			String fileMd5 = HawkMd5.makeMD5(file);
			if (variableFiles.containsKey(filePath) && variableFiles.get(filePath).equals(fileMd5)) {
				return true;
			}
			variableFiles.put(filePath, fileMd5);
			
			ResourceBundle bundle = new PropertyResourceBundle(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8"));
			if (bundle != null) {
				Enumeration<String> keys = bundle.getKeys();
				Map<String, String> newVariable = new HashMap<String, String>();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement().trim();
					String val = bundle.getString(key).trim();
					if (newVariable.containsKey(key)) {
						HawkLog.logPrintln("variable key has exist: " + key);
						return false;
					}
					newVariable.put(key, val);
				}
				variableMap.putAll(newVariable);
				return true;
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
}
