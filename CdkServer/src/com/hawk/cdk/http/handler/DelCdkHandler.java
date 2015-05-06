package com.hawk.cdk.http.handler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.util.services.HawkCdkService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hawk.cdk.Cdk;
import com.hawk.cdk.CdkServices;
import com.hawk.cdk.http.CdkHttpServer;
import com.hawk.cdk.http.param.DelCdkParam;

/**
 * cdk删除操作
 * 
 * @author hawk
 */
public class DelCdkHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		int status = HawkCdkService.CDK_PARAM_ERROR;
		JSONObject jsonObject = new JSONObject();
		List<String> delCdks = new LinkedList<String>();

		Map<String, String> params = CdkHttpServer.parseHttpParam(httpExchange);
		Cdk.checkToken(params.get("token"));
		
		DelCdkParam cdkparam = new DelCdkParam();
		if (cdkparam.initParam(params)) {
			status = HawkCdkService.CDK_STATUS_OK;
			cdkparam.toLowerCase();
			CdkServices.getInstance().delCdk(cdkparam, delCdks);
		}

		jsonObject.put("status", String.valueOf(status));
		if (status == HawkCdkService.CDK_STATUS_OK) {
			JSONArray jsonArray = new JSONArray();
			for (String cdk : delCdks) {
				jsonArray.add(cdk);
			}
			jsonObject.put("cdks", jsonArray);
		}

		CdkHttpServer.response(httpExchange, jsonObject);
	}
}
