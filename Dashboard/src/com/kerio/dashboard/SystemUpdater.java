package com.kerio.dashboard;

import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public class SystemUpdater extends PeriodicTask {
	
	public class SystemInfo {
		public String versionString;
		public String osDescription;
		public String hostname;
	}

	protected ApiClient client;
	
	public SystemUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
	}

	@Override
	public void execute() {
		
		JSONObject emptyArguments = new JSONObject();
		
		LinkedHashMap<String, JSONObject> requests = new LinkedHashMap<String, JSONObject>();
		requests.put("ProductInfo.get", emptyArguments);
		requests.put("ProductInfo.getSystemHostname", emptyArguments);
		LinkedHashMap<String, JSONObject> response = this.client.execBatch(requests);
		
		JSONObject infoResult = response.get("ProductInfo.get");
		JSONObject nameResult = response.get("ProductInfo.getSystemHostname");
		
		if ((infoResult == null) || (nameResult == null)) {
 			this.notify("Unable to update");
			return;
		}
		
		try {
			SystemInfo sysInfo = new SystemInfo();
			
			JSONObject productInfo = infoResult.getJSONObject("productInfo");
			sysInfo.versionString = productInfo.getString("versionString");
			sysInfo.osDescription = productInfo.getString("osDescription");
			sysInfo.hostname = nameResult.getString("hostname");
			
			this.notify(sysInfo);
		} catch (JSONException e) {
 			this.notify("Unable to handle response");
		}
	}
}
