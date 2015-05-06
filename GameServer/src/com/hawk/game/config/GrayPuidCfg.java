package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/grayPuid.xml", struct = "map")
public class GrayPuidCfg extends HawkConfigBase {
	/**
	 * 灰度账号
	 */
	@Id
	protected final String puid;

	public GrayPuidCfg() {
		puid = null;
	}

	public String getPuid() {
		return puid;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
}
