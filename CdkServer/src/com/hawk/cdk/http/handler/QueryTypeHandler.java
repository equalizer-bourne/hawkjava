package com.hawk.cdk.http.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.util.services.HawkCdkService;

import net.sf.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hawk.cdk.Cdk;
import com.hawk.cdk.CdkServices;
import com.hawk.cdk.data.CdkTypeReward;
import com.hawk.cdk.http.CdkHttpServer;
import com.hawk.cdk.http.param.QueryTypeParam;

/**
 * cdk类型信息查询处理
 * 
 * @author hawk
 */
public class QueryTypeHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		int status = HawkCdkService.CDK_PARAM_ERROR;
		Map<String, String> params = CdkHttpServer.parseHttpParam(httpExchange);
		Cdk.checkToken(params.get("token"));
		
		QueryTypeParam cdkparam = new QueryTypeParam();
		if (cdkparam.initParam(params)) {
			cdkparam.toLowerCase();
			
			JSONObject jsonObject = new JSONObject();
			if (cdkparam.getType() != null && cdkparam.getType().length() > 0) {
				CdkTypeReward typeReward = CdkServices.getInstance().queryTypeReward(cdkparam);
				if (typeReward != null) {
					status = HawkCdkService.CDK_STATUS_OK;
				} else {
					status = HawkCdkService.CDK_TYPE_NONEXIST;
				}
				
				if (status == HawkCdkService.CDK_STATUS_OK) {
					jsonObject.put("type", typeReward.toString());
				}
			} else {
				Map<String, CdkTypeReward> typeRewards = CdkServices.getInstance().getGameTypeRewards(cdkparam.getGame());
				if (typeRewards != null) {
					status = HawkCdkService.CDK_STATUS_OK;
					for (Entry<String, CdkTypeReward> entry : typeRewards.entrySet()) {
						jsonObject.put(entry.getKey(), entry.getValue().toString());
					}
				} else {
					status = HawkCdkService.CDK_TYPE_NONEXIST;
				}
			}
			
			jsonObject.put("status", String.valueOf(status));
			CdkHttpServer.response(httpExchange, jsonObject);
		}
	}
}
