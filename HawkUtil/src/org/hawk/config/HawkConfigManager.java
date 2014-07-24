package org.hawk.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

/**
 * 配置文件管理器
 * 
 * @author hawk
 */
public class HawkConfigManager {
	/**
	 * xml类型配置注解
	 * 
	 * @author hawk
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ java.lang.annotation.ElementType.TYPE })
	public @interface XmlResource {
		/**
		 * 文件路径
		 * 
		 * @return
		 */
		public String file() default "";

		/**
		 * 存储结构, "map" | "list"
		 * 
		 * @return
		 */
		public String struct() default "map";
	}

	/**
	 * kv类型配置注解
	 * 
	 * @author hawk
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ java.lang.annotation.ElementType.TYPE })
	public @interface KVResource {
		/**
		 * 文件路径
		 * 
		 * @return
		 */
		public String file() default "";
	}

	/**
	 * json类型配置注解
	 * 
	 * @author hawk
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ java.lang.annotation.ElementType.TYPE })
	public @interface JsonResource {
		/**
		 * 文件路径
		 * 
		 * @return
		 */
		public String file() default "";
		
		/**
		 * 存储结构, "map" | "list"
		 * 
		 * @return
		 */
		public String struct() default "map";
	}
	
	/**
	 * 配置对象存储器
	 */
	private ConcurrentHashMap<Class<?>, HawkConfigStorage> storages = new ConcurrentHashMap<Class<?>, HawkConfigStorage>();
	/**
	 * 存储更新配置对象
	 */
	private Map<Class<?>, HawkConfigStorage> updateStorages = new HashMap<Class<?>, HawkConfigStorage>();
	/**
	 * 配置管理器实例
	 */
	private static HawkConfigManager instance;

	/**
	 * 获取配置管理器实例
	 * 
	 * @return
	 */
	public static HawkConfigManager getInstance() {
		if (instance == null) {
			instance = new HawkConfigManager();
		}
		return instance;
	}

	/**
	 * 初始化配置管理器
	 * 
	 * @param packages
	 *            , 多个包以逗号分隔
	 * @throws Exception
	 */
	public boolean init(String configPackages) {
		try {
			String[] configPackageArray = configPackages.split(",");
			if (configPackageArray != null) {
				for (String configPackage : configPackageArray) {
					HawkLog.logPrintln("init config package: " + configPackage);
					List<Class<?>> classList = HawkClassScaner.scanClassesFilter(configPackage, 
							XmlResource.class, KVResource.class, JsonResource.class);
					
					for (Class<?> configClass : classList) {
						storages.put(configClass, new HawkConfigStorage(configClass));
					}
				}
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 更新加载
	 */
	public void updateReload() {
		updateStorages.clear();
		for (Entry<Class<?>, HawkConfigStorage> entry : storages.entrySet()) {
			if (entry.getValue().checkUpdate()) {
				try {
					updateStorages.put(entry.getKey(), new HawkConfigStorage(entry.getKey()));
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		storages.putAll(updateStorages);
	}

	/**
	 * 获取配置列表
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getConfigList(Class<T> cfgClass) {
		HawkConfigStorage storage = storages.get(cfgClass);
		if (storage != null) {
			return (List<T>) storage.getConfigList();
		}
		return null;
	}

	/**
	 * 获取配置表映射
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<Object, T> getConfigMap(Class<T> cfgClass) {
		HawkConfigStorage storage = storages.get(cfgClass);
		if (storage != null) {
			return (Map<Object, T>) storage.getConfigMap();
		}
		return null;
	}

	/**
	 * 获取指定配置文件中特定索引的配置
	 * 
	 * @param cfgClass
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfigByIndex(Class<T> cfgClass, int index) {
		HawkConfigStorage storage = storages.get(cfgClass);
		if (storage != null) {
			List<T> cfgList = (List<T>) storage.getConfigList();
			if (cfgList != null && cfgList.size() > index) {
				return cfgList.get(index);
			}
		}
		return null;
	}

	/**
	 * 获取指定配置文件中特定key的配置
	 * 
	 * @param cfgClass
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfigByKey(Class<T> cfgClass, Object key) {
		HawkConfigStorage storage = storages.get(cfgClass);
		if (storage != null) {
			Map<Object, T> cfgMap = (Map<Object, T>) storage.getConfigMap();
			if (cfgMap != null) {
				return cfgMap.get(key);
			}
		}
		return null;
	}
}
