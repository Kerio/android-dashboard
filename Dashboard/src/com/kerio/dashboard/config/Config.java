package com.kerio.dashboard.config;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.*;
import android.util.Log;

public class Config {
	
	public final static String SERVER_CONFIG = "Servers";
	private final static String KEY_SERVERS = "com.kerio.dashboard.SERVERS";
	
	private SharedPreferences settings;
	
	public Config(SharedPreferences settings) {
		this.settings = settings;
	}
	
	public List<ServerConfig> get() {
		
		Log.d("Config", "in Config::get()");
		
		List<ServerConfig> list = new ArrayList<ServerConfig>();
		
		String serversJsonStr = settings.getString(KEY_SERVERS, "");
		
		try {
			JSONArray serverJsonArr = new JSONArray(serversJsonStr);
			
			for (int i = 0; serverJsonArr.length() != i; ++i) {
				ServerConfig serverConfig = new ServerConfig();
				
				try {
					serverConfig.fromJsonObject(serverJsonArr.getJSONObject(i));
				}
				catch (JSONException e) {
					//TODO silently ignore, continue with another JSON object
					Log.e("Config", e.getMessage());
					continue;
				}
				list.add(serverConfig);
			}
			
		} catch (JSONException e) {
			//TODO silently ignore, return empty config.
			Log.e("Config", e.getMessage());
			return new ArrayList<ServerConfig>();
		}
		
		return list;
	}
	
	public boolean set(List<ServerConfig> config) {

		Log.d("Config", "in Config::set()");
		
		SharedPreferences.Editor editor = this.settings.edit();
		JSONArray serverJsonArr = new JSONArray();
		
		for (ServerConfig serverConfig : config) {
			try {
				JSONObject serverJson = serverConfig.toJsonObject();
				serverJsonArr.put(serverJson);
			} catch (JSONException e) {
				Log.e("Config", e.getMessage());
				continue;
			}
		
		}
		
		editor.putString(KEY_SERVERS, serverJsonArr.toString());
		return editor.commit();
	}
	

	
}
