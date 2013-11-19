package com.kerio.dashboard;

import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public abstract class TileUpdater extends PeriodicTask {

	protected String method;
	protected JSONObject arguments;
	protected ApiClient client;
	
	public TileUpdater(Handler handler, ApiClient client, String method) {
		super(handler);
		this.client = client;
		this.method = method;
	}
	
	protected abstract boolean initializeArguments();
	protected abstract Object handleResponse(JSONObject response);
		
	@Override
	public void execute() {
		
		if ((this.arguments == null) && ( ! this.initializeArguments())) {
			this.arguments = null;
 			this.notify("Unable to prepare request");
			return;
		}

		JSONObject ret = this.client.exec(this.method, arguments);
		if (ret == null) {
 			this.notify("Unable to update");
			return;
		}
		
		Object result = this.handleResponse(ret);
		if (result != null) {
			this.notify(result);
		}
		else {
 			this.notify("Unable to handle response");
		}
	}
}
