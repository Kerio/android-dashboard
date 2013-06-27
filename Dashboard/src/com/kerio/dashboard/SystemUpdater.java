package com.kerio.dashboard;

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
		
		JSONObject infoResult = this.client.exec("ProductInfo.get", emptyArguments);
		JSONObject nameResult = this.client.exec("ProductInfo.getSystemHostname", emptyArguments);
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
