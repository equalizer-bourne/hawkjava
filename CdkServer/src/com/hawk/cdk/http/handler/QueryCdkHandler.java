package com.hawk.cdk.http.handler;

import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hawk.cdk.CdkServices;
import com.hawk.cdk.data.CdkInfo;
import com.hawk.cdk.data.CdkTypeReward;
import com.hawk.cdk.http.CdkHttpServer;
import com.hawk.cdk.http.param.QueryCdkParam;

/**
 * cdk查询处理
 * 
 * @author hawk
 */
public class QueryCdkHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		int status = CdkServices.CDK_PARAM_ERROR;
		JSONObject jsonObject = new JSONObject();

		CdkInfo cdkInfo = null;
		Map<String, String> params = CdkHttpServer.parseHttpParam(httpExchange);
		QueryCdkParam cdkparam = new QueryCdkParam();
		if (cdkparam.initParam(params)) {
			cdkparam.toLowerCase();
			cdkInfo = CdkServices.getInstance().queryCdkInfo(cdkparam);
			if (cdkInfo != null) {
				status = CdkServices.CDK_STATUS_OK;
			} else {
				status = CdkServices.CDK_STATUS_NONEXIST;
			}
		}

		jsonObject.put("status", String.valueOf(status));
		if (status == CdkServices.CDK_STATUS_OK) {
			String gameName = CdkServices.getInstance().getGameNameFromCdk(cdkparam.getCdk());
			String typeName = CdkServices.getInstance().getTypeNameFromCdk(cdkparam.getCdk());
			// 填充类型信息
			CdkTypeReward typeReward = CdkServices.getInstance().getTypeReward(gameName + "." + typeName);
			if (typeReward != null) {
				jsonObject.put("type", typeReward.toString());
			}
			jsonObject.put("cdk", cdkInfo.toString());
		}

		CdkHttpServer.response(httpExchange, jsonObject);
	}
}
