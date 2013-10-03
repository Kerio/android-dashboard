package com.kerio.dashboard;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

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
			String temp[] = controlVersion.split(" ");
			String version = temp[0];
			version = version.replace('.', ','); //I dont know why but split(".") did not work, so I had to do this replace
			temp = version.split(",");
			int i1 = Integer.valueOf(temp[0]);
			int i2 = Integer.valueOf(temp[1]);
			
			if(i1 >= 8 && i2 >=2){ //new API call is applied when version is equal or higher then 8.2.0
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			this.notify("Unable to handle system Information");
			return true;
		}
	}
	
	private boolean sendRequests(){
		JSONObject emptyArguments = new JSONObject();
		
		infoResult = this.client.exec("ProductRegistration.getFullStatus", emptyArguments);
		devices = this.client.exec("ProductInfo.getUsedDevicesCount", emptyArguments);
		av = this.client.exec("Antivirus.get", emptyArguments);
		
		JSONObject productInfo = this.client.exec("ProductInfo.get", emptyArguments);
		if(newVersion(productInfo)){
			webFilter = this.client.exec("ContentFilter.getUrlFilterConfig", emptyArguments);
		}else{
			webFilter = this.client.exec("HttpPolicy.getUrlFilterConfig", emptyArguments);
		}
		
		if ((devices == null) || infoResult == null || av == null || webFilter == null) {
 			this.notify("Unable to update");
			return false;
		}else{
			return true;
		}
	}
	
}
