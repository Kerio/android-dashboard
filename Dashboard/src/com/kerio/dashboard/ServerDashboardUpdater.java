package com.kerio.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.config.ServerConfig;

public class ServerDashboardUpdater extends PeriodicTask {
	
	private ServerConfig config;
	private ApiClient client;
	
	public ServerDashboardUpdater(Handler handler, ApiClient client, ServerConfig config) {
		super(handler);
		this.config = config;
		this.client = client;
	}
	
//	public ServerDashboardUpdater(Handler handler, ServerConfig config, long delay) {
//		super(handler, delay);
//		this.config = config;
//	}
	
	@Override
	public void execute() {
		notify("UpdateStarted");
 		if ( ! this.client.login(this.config.username, this.config.password)) {
 			this.notify("Unable to connect: " + this.client.getLastError());
 			return;
 		}

		JSONObject settings = this.client.exec("Session.getSettings", null);
		if (settings == null) {
			this.notify("Unable to load settings: " + this.client.getLastError());
			return;
		}

		try {
			JSONObject d = settings.getJSONObject("settings").getJSONObject("admin").getJSONObject("dashboard");
			JSONArray columns = d.getJSONArray("columns");
			
	 		Map<String, Object> tiles = new LinkedHashMap<String, Object>();

			for (int i = 0; i < columns.length(); i++) {
				JSONArray column = columns.getJSONArray(i);
				for (int j = 0; j < column.length(); j++) {
					JSONObject item = column.getJSONObject(j);
					tiles.put(item.getString("type"), item.optJSONObject("custom"));
				}
			}
			
			notify("UpdateDone");
			notify(tiles);
		} catch (JSONException e) {
			this.notify("Unable to load settings, incorrect format");
		}
	}
}
