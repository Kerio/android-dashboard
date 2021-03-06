package com.kerio.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

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
		super(handler, 0);
		this.config = config;
		this.client = client;
	}
	
	
	@Override
	public void execute() {
		boolean emptyDashboard = true;
		
		notify("UpdateStarted");
		boolean connected = false;
		try{
			connected = this.client.login(this.config.username, this.config.password);
 		}catch(SSLHandshakeException se){
 			connected = false;
 		}
		
		if( ! connected){
 			this.notify("Unable to connect: " + this.client.getLastError());
 			return;
		}

		JSONObject settings = this.client.exec("Session.getSettings", null);
		if (settings == null) {
			this.notify("Unable to load settings: " + this.client.getLastError());
			return;
		}

		Map<String, Object> tiles = new LinkedHashMap<String, Object>();
		
		try {
			JSONObject d = settings.getJSONObject("settings").getJSONObject("admin").getJSONObject("dashboard");
			JSONArray columns = d.getJSONArray("columns");
			
	 		

			for (int i = 0; i < columns.length(); i++) {
				JSONArray column = columns.getJSONArray(i);
				if(!column.isNull(0)){
					emptyDashboard = false;
				}
				for (int j = 0; j < column.length(); j++) {
					JSONObject item = column.getJSONObject(j);
					if(item.getString("type").equals("tileTrafficChart")){
						JSONObject properties = item.getJSONObject("custom");
						tiles.put(item.getString("type")+properties.getString("chartId"), item.optJSONObject("custom")); //we need the key of map to be unique
					}else{
						tiles.put(item.getString("type"), item.optJSONObject("custom"));
					}
				}
			}
			if(emptyDashboard){
				addDefaultTiles(tiles);
			}

		} catch (JSONException e) {
			addDefaultTiles(tiles);
		}
		
		notify("UpdateDone");
		notify(tiles);
	}
	
	private void addDefaultTiles(Map<String, Object> tiles){
		tiles.put("tileSystemHealth", null);
		tiles.put("tileSystem", null);
		tiles.put("tileSystemStatus", null);
	}
}
