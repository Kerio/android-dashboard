package com.kerio.dashboard.gui.tiles;

import org.json.JSONException;
import org.json.JSONObject;

import com.kerio.dashboard.api.ApiClient;

import android.content.Context;

public class TileFactory {

	public TileFactory(Context context, ApiClient client) {
		this.client = client;
		this.context = context;
		
	}
	public Tile create(String type, JSONObject data) {
		
		if (type.equals("notification")) {
			return new NotificationTile(context, client);
		}
		else if (type.equals("tileSystemHealth")) {
			return new Frame(context, "System Health", new SystemHealthTile(context, client));
		}
		else if (type.equals("tileSystem")) {
			return new Frame(context, "System", new SystemTile(context, client));
		}
		else if (type.equals("tileSystemStatus")) {
			return new Frame(context, "System Status", new SystemStatusTile(context, client));
		}
		else if (type.equals("tileVpn")) {
			return new Frame(context, "VPN Info", new VpnInfoTile(context, client));
		}
		else if (type.equals("tileConnectivity")) {
			return new Frame(context, "Connectivity", new ConnectivityTile(context, client));
		}
		else if (type.equals("tileActiveHosts")) {
			return new Frame(context, "Top Active Hosts", new TopActiveHostsTile(context, client));
		}
		else if (type.equals("tileLicense")) {
			return new Frame(context, "License info", new LicenseTile(context, client));
		}
		else if (type.startsWith("tileTrafficChart")) { //format is tileTrafficChart+chartId
			String chartId = null;
			String chartName = null;
			Tile result = null;
			try {
				chartId = data.getString("chartId");
				chartName = data.getString("chartName");
				result = new Frame(context, chartName, new TrafficChartTile(context, client, chartId));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return result;
		}
		return null;
	}
	
	private Context context;
	private ApiClient client;
}
