package org.hawk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HawkJsonUtil {
	/**
	 * 实例对象
	 */
	private static Gson instance = null;

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static Gson getJsonInstance() {
		if (instance == null) {
			synchronized (Gson.class) {
				instance = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			}
		}
		return instance;
	}
}
