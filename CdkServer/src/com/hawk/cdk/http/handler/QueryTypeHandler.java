package com.hawk.cdk.http.handler;

import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
		int status = CdkServices.CDK_PARAM_ERROR;
		JSONObject jsonObject = new JSONObject();

		CdkTypeReward typeReward = null;
		Map<String, String> params = CdkHttpServer.parseHttpParam(httpExchange);
		QueryTypeParam cdkparam = new QueryTypeParam();
		if (cdkparam.initParam(params)) {
			cdkparam.toLowerCase();
			typeReward = CdkServices.getInstance().queryTypeReward(cdkparam);
			if (typeReward != null) {
				status = CdkServices.CDK_STATUS_OK;
			} else {
				status = CdkServices.CDK_TYPE_NONEXIST;
			}
		}

		jsonObject.put("status", String.valueOf(status));
		if (status == CdkServices.CDK_STATUS_OK) {
			jsonObject.put("type", typeReward.toString());
		}

		CdkHttpServer.response(httpExchange, jsonObject);
	}
}
