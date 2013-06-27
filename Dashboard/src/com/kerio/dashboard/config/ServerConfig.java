package com.kerio.dashboard.config;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfig implements Comparable<ServerConfig> {
	public int id = 0;
	
	public String description;

	public enum ServerType {
		CONTROL,
		CONNECT,
		OPERATOR
	}
	
	public ServerType type;
	
	public String server;
	
	public String username;
	public String password;
	
	void fromJsonObject(JSONObject serverJson) throws JSONException {
		this.id = serverJson.getInt(KEY_ID);
				
		this.type = ServerType.values()[serverJson.getInt(KEY_TYPE)];
		this.server = serverJson.getString(KEY_SERVER);
		this.username = serverJson.getString(KEY_USERNAME);
		this.password = serverJson.getString(KEY_PASSWORD);
		this.description = serverJson.getString(KEY_DESCRIPTION);
		
	}

	JSONObject toJsonObject() throws JSONException {
		
		JSONObject result = new JSONObject();
		result.put(KEY_ID, this.id);
		
		result.put(KEY_TYPE, this.type.ordinal());
		result.put(KEY_SERVER, this.server);
		result.put(KEY_USERNAME, this.username);
		result.put(KEY_PASSWORD, this.password);
		result.put(KEY_DESCRIPTION, this.description);
		return result;
	}
	
	/**
	 * Assigns user defined parameters and leaves ID untouched.
	 * @param newConfig source object to be used for obtaining new attribute values of this object.
	 * */
	public void assign(ServerConfig newConfig) {
		this.type = newConfig.type;
		this.server = newConfig.server;
		this.username = newConfig.username;
		this.password = newConfig.password;
		this.description = newConfig.description;
	}
	
	public final static String KEY_ID = "id";
	public final static String KEY_TYPE = "type";
	public final static String KEY_SERVER = "server";
	public final static String KEY_USERNAME = "username";
	public final static String KEY_PASSWORD = "password";
	public final static String KEY_DESCRIPTION = "description";

	@Override
	public int compareTo(ServerConfig another) {
		return this.id - another.id;
	}
	
}
