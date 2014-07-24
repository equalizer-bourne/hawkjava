package org.hawk.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置文件基类
 * 
 * @author hawk
 */
public class HawkConfigBase extends HawkConstable {
	/**
	 * Id关键字注解
	 * 
	 * @author hawk
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD })
	public @interface Id {
	}

	/**
	 * 配置加载完成调用, 便于上层进行数据按照应用层要求重新构建 
	 * 备注: 返回true表示格式正确, 否则格式错误不添加进入
	 */
	protected boolean assemble() {
		return true;
	}
}
