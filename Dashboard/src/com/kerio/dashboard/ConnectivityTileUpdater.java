package com.kerio.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public class ConnectivityTileUpdater extends PeriodicTask{
	
	JSONObject interfaces;
	
	public class Connectivity {
		public String interfaces[][];
	}
	
	protected ApiClient client;
	Connectivity con = new Connectivity();
	
	public ConnectivityTileUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
	}
	
	@Override
	public void execute(){
		if(!sendRequests()){
			return;
		}
		
		internetInterfaces();
		this.notify(con);
	}
	
	private void internetInterfaces(){
		int count = 0;
		try {
			JSONArray ifaces = interfaces.getJSONArray("list");
			for(int i=0;i<ifaces.length();i++){
				JSONObject iface = ifaces.getJSONObject(i);
				if((iface.getString("type").equals("Ethernet")) && (iface.getString("group").equals("Internet"))){
					count++;
				}
			}
			
			if(count == 0){
				con.interfaces = null;
			}else{	
				con.interfaces = new String[count][2]; //count = number of tunnels, and second dimension stands for name and link status
			
				int j=0;
				for(int i=0;i<ifaces.length();i++){
					String status = "";
					JSONObject iface = ifaces.getJSONObject(i);
					if((iface.getString("type").equals("Ethernet")) && (iface.getString("group").equals("Internet"))){
						con.interfaces[j][0] = iface.getString("name");
						if(iface.getString("linkStatus").length()==2){
							status = iface.getString("linkStatus") + "            ";
						}else{
							status = iface.getString("linkStatus") + "          ";
						}
						status = status + trafficData(iface.getString("id"));
						con.interfaces[j][1] = status;
						j++;
					}
				}
			}
			
		} catch (JSONException e) {
			this.notify("vpnInterfaces: Unable to hadnle response: "+e.toString());
		}
	}
	
	private String trafficData(String id){
		int in = 0;
		int out = 0;
		String unit = "";
		
		JSONObject traffic = new JSONObject();
		try{
			traffic.put("type", "HistogramTwoHours");
			traffic.put("id", "0"+id);
		}catch(JSONException e){
			this.notify("Unable to make JSONObject for traffic data");
			System.out.println("Unable to make JSONObject for traffic data"+e.toString());
		}
		
		JSONObject trafficData = this.client.exec("TrafficStatistics.getHistogram", traffic);
		
		if(trafficData == null){
			this.notify("Unable to get traffic data");
			System.out.println("Unable to get traffic data");
			return "";
		}else{
			try{
				JSONObject hist = trafficData.getJSONObject("hist");
				JSONArray data = hist.getJSONArray("data");
				if(hist.getString("units").equals("Bytes")){
					unit = "B/s";
				}else if(hist.getString("units").equals("KiloBytes")){
					unit = "KB/s";
				}else if(hist.getString("units").equals("MegaBytes")){
					unit = "MB/s";
				}else if(hist.getString("units").equals("GigaBytes")){
					unit = "GB/s";
				}else{
					unit = "Unknown";
				}
				
				JSONObject recentTraffic = data.getJSONObject(0);
				in = recentTraffic.getInt("inbound");
				out = recentTraffic.getInt("outbound");
				
				
			}catch(JSONException e){
				this.notify("Unable to handle traffic data response");
				System.out.println("Unable to handle traffic data response"+e.toString());
			}
		}
		
		return in+" "+unit+" / "+out+" "+unit;
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
			
		interfaces = this.client.exec("Interfaces.get", interfacesArguments);
		if (interfaces == null) {
 			this.notify("Unable to update");
			return false;
		}else{
			return true;
		}
	}

}
