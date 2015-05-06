package com.hawk.cdk.http.param;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;

/**
 * 删除指定cdk(列表)参数
 * 
 * @author hawk
 */
public class DelCdkParam {
	private List<String> cdks;

	public DelCdkParam() {
		cdks = new LinkedList<String>();
	}

	public void toLowerCase() {
		for (int i = 0; cdks != null && i < cdks.size(); i++) {
			cdks.set(i, cdks.get(i).toLowerCase());
		}
	}

	public List<String> getCdks() {
		return cdks;
	}

	public void setCdks(List<String> cdks) {
		this.cdks = cdks;
	}

	public boolean initParam(Map<String, String> params) {
		try {
			String cdksInfo = params.get("cdk");
			if (cdksInfo != null && cdksInfo.length() > 0) {
				String[] cdkArray = cdksInfo.split(",");
				for (String cdk : cdkArray) {
					cdks.add(cdk);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return cdks.size() > 0;
	}
}
