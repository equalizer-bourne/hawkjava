package org.hawk.config;

import org.apache.commons.configuration.XMLConfiguration;

@SuppressWarnings("serial")
public class HawkXmlCfg extends XMLConfiguration {
	/**
	 * 从文件构造
	 * 
	 * @param xmlCfg
	 * @throws Exception
	 */
	public HawkXmlCfg(String xmlCfg) throws Exception {
		super(xmlCfg);
	}
}
