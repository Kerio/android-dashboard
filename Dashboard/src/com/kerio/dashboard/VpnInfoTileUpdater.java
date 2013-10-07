package com.kerio.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public class VpnInfoTileUpdater extends PeriodicTask{
	
	JSONObject activeHosts;
	JSONObject vpnInterfaces;
	
	public class VpnInfo {
		public String clients;
		public String tunnels[][];
	}
	
	protected ApiClient client;
	VpnInfo vi = new VpnInfo();
	
	public VpnInfoTileUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
	}
	
	@Override
	public void execute(){
		if(!sendRequests()){
			return;
		}
		vpnClients();
		vpnInterfaces();
		
		this.notify(vi);
	}
	
	private void vpnInterfaces(){
		int count = 0;
		try {
			JSONArray interfaces = vpnInterfaces.getJSONArray("list");
			for(int i=0;i<interfaces.length();i++){
				JSONObject iface = interfaces.getJSONObject(i);
				if((iface.getString("type").equals("VpnTunnel")) && (!iface.getString("id").equals("VpnServer"))){
					count++;
				}
			}
			
			if(count == 0){
				vi.tunnels = null;
			}else{	
				vi.tunnels = new String[count][2]; //count = number of tunnels, and second dimension stands for name and link status
			
				int j=0;
				for(int i=0;i<interfaces.length();i++){
					JSONObject iface = interfaces.getJSONObject(i);
					if((iface.getString("type").equals("VpnTunnel")) && (!iface.getString("id").equals("VpnServer"))){
						vi.tunnels[j][0] = iface.getString("name");
						vi.tunnels[j][1] = iface.getString("linkStatus");
						j++;
					}
				}
			}
			
		} catch (JSONException e) {
			this.notify("vpnInterfaces: Unable to hadnle response: "+e.toString());
		}
	}
	
	private void vpnClients(){
		int vpnClients = 0;
		vi.clients = "0";
		try {
			JSONArray clients = activeHosts.getJSONArray("list");
			for(int i=0;i<clients.length();i++){
				JSONObject client = clients.getJSONObject(i);
				if(client.getString("authMethod").equals("AuthMethodVpnClient")){
					vpnClients++;
				}
			}
			
		} catch (JSONException e) {
			this.notify("vpnClients: Unable to handle response");
		}
		vi.clients = String.valueOf(vpnClients);
		
	}
	
	private boolean sendRequests(){
		JSONObject interfaces0 = new JSONObject();
		JSONArray interfacesOrderBy = new JSONArray();
		JSONObject interfacesQuery = new JSONObject();
		JSONObject interfacesArguments = new JSONObject();
		
		try{
			interfaces0.put("columnName", "name");
			interfaces0.put("direction", "Asc");
			interfacesOrderBy.put(0, interfaces0);
			interfacesQuery.put("start", 0);
			interfacesQuery.put("limit", -1);
			interfacesQuery.put("orderBy", interfacesOrderBy);
			interfacesArguments.put("sortByGroup", true);
			interfacesArguments.put("query", interfacesQuery);
			
			
			
		}catch(JSONException e){
			this.notify("Unable to make JSONObject for vpn tunnels");
		}
		
		
		//Active VPN hosts request
		JSONObject activeHostsArguments = new JSONObject();
		JSONObject activeHostsQuery = new JSONObject();
		JSONArray activeHostsOrderBy = new JSONArray();
		JSONObject activeHosts0 = new JSONObject();
		try{
			activeHosts0.put("columnName", "hostname");
			activeHosts0.put("direction", "Asc");
			activeHostsOrderBy.put(0, activeHosts0);
			activeHostsQuery.put("orderBy", activeHostsOrderBy);
			activeHostsArguments.put("query", activeHostsQuery);
			activeHostsArguments.put("refresh", false);
			
		}catch(JSONException e){
			this.notify("Unable to make JSONObject for active vpn hosts");
		}
		
		activeHosts = this.client.exec("ActiveHosts.get", activeHostsArguments);
		vpnInterfaces = this.client.exec("Interfaces.get", interfacesArguments);
		if ((activeHosts == null) || vpnInterfaces == null) {
 			this.notify("Unable to update");
			return false;
		}else{
			return true;
		}
	}

}
