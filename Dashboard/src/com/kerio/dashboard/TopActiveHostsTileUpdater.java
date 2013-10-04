package com.kerio.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public class TopActiveHostsTileUpdater extends PeriodicTask{
	
	public class TopActiveHosts {
		public String download[][]; //[][0] - computer [][1] - user [][2] speed
		public String upload[][];
	}
	
	JSONObject downloadHosts;
	JSONObject uploadHosts;
	
	protected ApiClient client;
	TopActiveHosts tah = new TopActiveHosts();
	
	public TopActiveHostsTileUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
	}
	
	@Override
	public void execute(){
		if(!sendRequests()){
			return;
		}
		
		parseDownloadHosts();
		parseUploadHosts();
		
		this.notify(tah);
	}
	
	private void parseUploadHosts(){
		try{
			JSONArray list = uploadHosts.getJSONArray("list");
			
			if(list == null || list.length() == 0){
				tah.upload = null;
				return;
			}
			
			tah.upload = new String[list.length()][3];
			for(int i=0;i<list.length();i++){
				JSONObject temp = list.getJSONObject(i);
				JSONObject user = temp.getJSONObject("user");
				tah.upload[i][0] = temp.getString("hostname");
				tah.upload[i][1] = user.getString("name");
				tah.upload[i][2] = formatNumber(temp.getString("currentUpload"));
				
			}
			
		}catch(JSONException e){
			System.out.println(e.toString());
			this.notify("Unable to hadle UploadHosts response");
		}catch(Exception e){
			this.notify(e.toString());
		}
	}
	
	private void parseDownloadHosts(){
		try{
			
			JSONArray list = downloadHosts.getJSONArray("list");
			
			if(list == null || list.length() == 0){
				tah.download = null;
				return;
			}
			
			tah.download = new String[list.length()][3];
			
			for(int i=0;i<list.length();i++){
				JSONObject temp = list.getJSONObject(i);
				JSONObject user = temp.getJSONObject("user");
				tah.download[i][0] = temp.getString("hostname");
				tah.download[i][1] = user.getString("name");
				tah.download[i][2] = formatNumber(temp.getString("currentDownload"));
				
			}
			
		}catch(JSONException e){
			this.notify("Unable to hadle DownloadHosts response");
		}catch(Exception e){
			this.notify(e.toString());
		}
	}
	
	private String formatNumber(String number){
		if(!number.contains(".")){
			return number;
		}
		
		int decimalPoint = number.indexOf(".");
		String result = number.substring(0, decimalPoint+2);
		return result;
			
	}
	
	private boolean sendRequests(){
		JSONObject downloadSort = new JSONObject();
		JSONArray downloadOrderBy = new JSONArray();
		JSONObject downloadQuery = new JSONObject();
		JSONObject downloadParams = new JSONObject();
		
		JSONObject uploadSort = new JSONObject();
		JSONArray uploadOrderBy = new JSONArray();
		JSONObject uploadQuery = new JSONObject();
		JSONObject uploadParams = new JSONObject();
		
		
		
		
		
		try{			
			downloadSort.put("columnName", "currentDownload");
			downloadSort.put("direction", "Desc");
			downloadOrderBy.put(0, downloadSort);
			downloadQuery.put("start", 0);
			downloadQuery.put("limit", 3);
			downloadQuery.put("orderBy", downloadOrderBy);
			downloadParams.put("refresh", false);
			downloadParams.put("query", downloadQuery);
			
			uploadSort.put("columnName", "currentUpload");
			uploadSort.put("direction", "Desc");
			uploadOrderBy.put(0, uploadSort);
			uploadQuery.put("start", 0);
			uploadQuery.put("limit", 3);
			uploadQuery.put("orderBy", uploadOrderBy);
			uploadParams.put("refresh", false);
			uploadParams.put("query", uploadQuery);
			
			
		}catch(JSONException e){
			this.notify("Unable to make JSONObject for vpn tunnels");
		}
			
		downloadHosts = this.client.exec("ActiveHosts.get", downloadParams);
		uploadHosts = this.client.exec("ActiveHosts.get", uploadParams);
		if (downloadHosts == null || uploadHosts == null) {
 			this.notify("Unable to update");
			return false;
		}else{
			return true;
		}
	}

}
