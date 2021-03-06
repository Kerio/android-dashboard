package com.kerio.dashboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import com.kerio.dashboard.api.ApiClient;

public class LicenseTileUpdater extends PeriodicTask{

	public class LicenseInfo {
		public String licenseNumber;
		public String company;
		public String licensedUsers;
		public String productExpiration;
		public String swmExpiration;
		public String activeUsers;
		public String activeDevices;
		public String antivirus;
		public String webfilter;
	}
	
	protected ApiClient client;

	JSONObject infoResult;
	JSONObject devices;
	JSONObject av;
	JSONObject webFilter;
	LicenseInfo licenseInfo = new LicenseInfo();
	
	public LicenseTileUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
	}
	
	@Override
	public void execute(){
		if(!sendRequests()){
			return;
		}
		
		getActiveUsersDevices();
		getLicenseStatus();
		getExpirations();
		getAntivirusStatus();
		getWebfilterStatus();
		
		this.notify(licenseInfo);
	}
	
	private void getWebfilterStatus(){
		try{
			JSONObject webConfig = webFilter.getJSONObject("config");
			String webFilterStatus = webConfig.getString("status");
			if(!webConfig.getBoolean("enabled")){
				licenseInfo.webfilter = "Disabled";
				return;
			}
			
			licenseInfo.webfilter = "Not activated";
			if(webFilterStatus.equals("UrlFilterActivated")){
				licenseInfo.webfilter = "Licensed";
			}
			if(webFilterStatus.equals("UrlFilterActivating")){
				licenseInfo.webfilter = "Activating";
			}
			if(webFilterStatus.equals("UrlFilterNotLicensed")){
				licenseInfo.webfilter = "Unlicensed";
			}
		}catch(JSONException e){
			this.notify("webfilterStatus: Unable to handle response");
		}
	}
	
	private void getAntivirusStatus(){
		try{
			String avAvailable;
			String avExpired;
			
			JSONObject avConfig = av.getJSONObject("config");
			JSONObject avStatus = avConfig.getJSONObject("antivirus");
			JSONObject antivirusStatus = avStatus.getJSONObject("internal");
			avAvailable = antivirusStatus.getString("available");
			avExpired = antivirusStatus.getString("expired");
			if(avAvailable.equals("false")){
				licenseInfo.antivirus = "N/A";
			}else{
				if(avExpired.equals(true)){
					licenseInfo.antivirus = "License expired";
				}else{
					licenseInfo.antivirus = "Licensed";
				}
			}
		}catch(JSONException e){
			this.notify("antivirusStatus: Unable to handle response");
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void getExpirations(){
		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new Date();
			
			JSONObject temp = infoResult.getJSONObject("status");
			JSONArray expirations = temp.getJSONArray("expirations");
			temp = expirations.getJSONObject(0);
			if(temp.getString("isUnlimited").equals("true")){
				licenseInfo.productExpiration = "Never";
			}else{
				Date expirationDateProduct = new Date(currentDate.getTime()+((Long.valueOf(temp.getString("remainingDays"))-1)*24*60*60*1000));
				licenseInfo.swmExpiration = dateFormat.format(expirationDateProduct);
			}
			
			
			temp = expirations.getJSONObject(1);
			Date expirationDateSWM = new Date(currentDate.getTime()+((Long.valueOf(temp.getString("remainingDays"))-1)*24*60*60*1000));
			licenseInfo.swmExpiration = dateFormat.format(expirationDateSWM);
		}catch(JSONException e){
			this.notify("expirations: Unable to handle response");
		}
	}
	
	private void getLicenseStatus(){
		try{
			JSONObject temp = infoResult.getJSONObject("status");
			licenseInfo.licenseNumber = temp.getString("Id");
			licenseInfo.company = temp.getString("company");
			licenseInfo.licensedUsers = temp.getString("users");
		}catch(JSONException e){
			this.notify("licenseStatus: Unable to handle response");
		}
	}
	
	private void getActiveUsersDevices(){
		try{
			licenseInfo.activeDevices = devices.getString("devices");
			licenseInfo.activeUsers = devices.getString("accounts");
		}catch(JSONException e){
			this.notify("activeUsersDevices: Unable to handle response");
		}
	}
	
	private boolean newVersion(JSONObject object){
		try{
			JSONObject productInfo = object.getJSONObject("productInfo");
			String controlVersion = productInfo.getString("versionString");

				
			Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\s+.*");
			Matcher versionMatcher = versionPattern.matcher(controlVersion);
			if(versionMatcher.find()){
				int major = Integer.valueOf(versionMatcher.group(1));
				int minor = Integer.valueOf(versionMatcher.group(2));
				if(major >= 8 && minor >=2){ //new API call is applied when version is equal or higher then 8.2.0
					return true;
				}
			}
		}catch(JSONException e){
			Log.d("LicenseTileUpdater.newVersion()", "Invalid data from server", e);
			this.notify("Unable to handle system Information");
			return true;
		}catch(NumberFormatException nfe){
			Log.d("LicenseTileUpdater.newVersion()", "Invalid version string", nfe);
			this.notify("Unable to handle system Information");
			return true;
		}
		
		return false;
	}
	
	private boolean sendRequests(){
		JSONObject emptyArguments = new JSONObject();
		LinkedHashMap<String, JSONObject> requests = new LinkedHashMap<String, JSONObject>();
		LinkedHashMap<String, JSONObject> response;
		
		JSONObject productInfo = this.client.exec("ProductInfo.get", emptyArguments);
		
		requests.put("ProductRegistration.getFullStatus", emptyArguments);
		requests.put("ProductInfo.getUsedDevicesCount", emptyArguments);
		requests.put("Antivirus.get", emptyArguments);
		if(newVersion(productInfo)){
			requests.put("ContentFilter.getUrlFilterConfig", emptyArguments);
		}else{
			requests.put("HttpPolicy.getUrlFilterConfig", emptyArguments);
		}
		
		requests.put("Antivirus.getUpdateStatus", new JSONObject());
		requests.put("IntrusionPrevention.get", new JSONObject());
		requests.put("IntrusionPrevention.getUpdateStatus", new JSONObject());
		requests.put("ProductInfo.get", new JSONObject());
		
		response = this.client.execBatch(requests);
		
		
		infoResult = response.get("ProductRegistration.getFullStatus");
		devices = response.get("ProductInfo.getUsedDevicesCount");
		av = response.get("Antivirus.get");
		
		if(newVersion(productInfo)){
			webFilter = response.get("ContentFilter.getUrlFilterConfig");
		}else{
			webFilter = response.get("HttpPolicy.getUrlFilterConfig");
		}
		
		if ((devices == null) || infoResult == null || av == null || webFilter == null) {
 			this.notify("Unable to update");
			return false;
		}else{
			return true;
		}
	}
	
}
