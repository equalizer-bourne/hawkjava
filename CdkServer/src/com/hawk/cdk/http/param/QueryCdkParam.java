package com.hawk.cdk.http.param;

import java.util.Map;

import org.hawk.os.HawkException;

/**
 * cdk 查询功能
 * 
 * @author hawk
 */
public class QueryCdkParam {
	private String cdk;

	public String getCdk() {
		return cdk;
	}

	public void setCdk(String cdk) {
		this.cdk = cdk;
	}

	public void toLowerCase() {
		if (cdk != null) {
			cdk.toLowerCase();
		}
	}

	public boolean initParam(Map<String, String> params) {
		try {
			cdk = params.get("cdk");

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
