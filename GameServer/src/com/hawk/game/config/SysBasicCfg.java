package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/sysBasic.cfg")
public class SysBasicCfg extends HawkConfigBase {
	/**
	 * 玩家对象缓存时间
	 */
	protected final int playerCacheTime;

	/**
	 * 全局静态对象
	 */
	private static SysBasicCfg instance = null;

	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static SysBasicCfg getInstance() {
		return instance;
	}

	public SysBasicCfg() {
		instance = this;

		playerCacheTime = 86400000;
	}

	public int getPlayerCacheTime() {
		return playerCacheTime;
	}
}
