package com.hawk.cdk.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hawk.log.HawkLog;

import com.hawk.cdk.data.CdkTypeReward;

import net.sf.json.JSONObject;

/**
 * cdk工具类
 * 
 * @author hawk
 */
@SuppressWarnings({ "rawtypes" })
public class CdkUtil {
	/**
	 * json对象转换为map
	 * 
	 * @param jsonString
	 * @return
	 */
	public static Map<String, CdkTypeReward> stringToTypeRewards(String typeRewards) {
		Map<String, CdkTypeReward> typeRewardMap = new HashMap<String, CdkTypeReward>();

		JSONObject jsonObject = JSONObject.fromObject(typeRewards);
		Iterator keyIt = jsonObject.keys();
		while (keyIt.hasNext()) {
			String key = "";
			String value = "";
			try {
				key = (String) keyIt.next();
				value = (String) jsonObject.get(key);

				CdkTypeReward typeReward = new CdkTypeReward();
				if (typeReward.parse(value)) {
					typeRewardMap.put(key, typeReward);
				}
			} catch (Exception e) {
				HawkLog.logPrintln(String.format("TypeRewards: %s, Key: %s, Value: %s", typeRewards, key, value));
			}
		}
		return typeRewardMap;
	}

	/**
	 * 字符串jsonmap转换为字符串
	 * 
	 * @param jsonMap
	 * @return
	 */
	public static String typeRewardsToString(Map<String, CdkTypeReward> typeRewardMap) {
		Map<String, String> typeInfos = new HashMap<String, String>();
		for (Map.Entry<String, CdkTypeReward> entry : typeRewardMap.entrySet()) {
			typeInfos.put(entry.getKey(), entry.getValue().toString());
		}
		JSONObject jsonObject = JSONObject.fromObject(typeInfos);
		return jsonObject.toString();
	}

	/**
	 * 获取日期字符串
	 * 
	 * @return
	 */
	public static String getDateString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(Calendar.getInstance().getTime());
	}
}
