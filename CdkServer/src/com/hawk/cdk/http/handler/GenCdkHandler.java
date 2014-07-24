package com.hawk.cdk.http.handler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hawk.cdk.CdkServices;
import com.hawk.cdk.data.CdkTypeReward;
import com.hawk.cdk.http.CdkHttpServer;
import com.hawk.cdk.http.param.GenCdkParam;

/**
 * cdk生成处理
 * 
 * @author hawk
 */
public class GenCdkHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		int status = CdkServices.CDK_PARAM_ERROR;
		JSONObject jsonObject = new JSONObject();
		List<String> genCdks = new LinkedList<String>();

		Map<String, String> params = CdkHttpServer.parseHttpParam(httpExchange);
		GenCdkParam cdkparam = new GenCdkParam();
		if (cdkparam.initParam(params)) {
			cdkparam.toLowerCase();
			status = CdkServices.getInstance().genCdk(cdkparam, genCdks);
		}

		jsonObject.put("status", String.valueOf(status));
		if (status == CdkServices.CDK_STATUS_OK) {
			JSONArray jsonArray = new JSONArray();
			for (String cdk : genCdks) {
				jsonArray.add(cdk);
			}

			CdkTypeReward typeReward = CdkServices.getInstance().getTypeReward(cdkparam.getGame() + "." + cdkparam.getType());
			jsonObject.put("type", typeReward.toString());
			jsonObject.put("cdks", jsonArray);
		}

		CdkHttpServer.response(httpExchange, jsonObject);
	}
}
