package com.kerio.dashboard;

import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kerio.dashboard.api.ApiClient;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

public class SystemStatusUpdater extends PeriodicTask {

	static final String notImplemented = "Not implemented"; 
	static final String checkFailed = "Check failed";
	static final String updateFailed = "Update failed";
	static final String updateAvailable = "Update available";
	static final String upToDate = "Up to date";
	
	static final String working = "Working properly";
	static final String disabled = "Disabled";

	static final String notActivated = "Not activated";
	static final String activating = "Activating";
	static final String unlicensed = "Unlicensed";
	
	static final String notAvaible = "Not available in the unregistered trial";
	static final String notAvaibleUpdates = "Working, but no updates available in the unregistred trial";
	
	static final String antivirusInternalFailed = "Internal antivirus failed";
	static final String antivirusExternalFailed = "External antivirus failed";
	static final String antivirusBothFailed = "Internal and external Antivirus failed";

	private LinkedHashMap<String, JSONObject> response;
	
	private boolean isUnregisteredTrial;
	private Integer uptimeRaw;

	public class SystemStatus {
		public Integer uptimeRaw;
		public String uptime;
		public String update;
		public String antivirus;
		public String ips;
		public String webFilter;
		public String ipsec;
		public String kvpn;
	};
	
	private ApiClient client;
	
	public SystemStatusUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.client = client;
		this.isUnregisteredTrial = false;
	}
	
	@SuppressLint("DefaultLocale")
	public static String computeUptimeString(int uptime) {
		int seconds = uptime % 60;
		uptime /= 60;
		int minutes = uptime % 60;
		uptime /= 60;
		int hours = uptime % 24;
		uptime /= 24;
		int days = uptime;
		return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
	}
	private String getUptime()
	{
		JSONObject queryResult = this.response.get("ProductInfo.getUptime");
		
		try {
			if (queryResult != null) { 
				this.uptimeRaw = queryResult.getInt("uptime");
				return computeUptimeString(this.uptimeRaw);
			}
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getValue()", e.toString());
		}

		return "0d 00:00:00";
	}

	private String getControlUpdateStatus() {
		JSONObject queryResult = this.response.get("UpdateChecker.getStatus");
		if (queryResult == null) {
			return checkFailed;
		}
		
		String status;
		boolean newAvailable;
		
		try {
			JSONObject upCheckStatus = queryResult.getJSONObject("status");
			status = upCheckStatus.getString("status");
			newAvailable = upCheckStatus.getBoolean("newVersion");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getValue()", e.toString());
			return checkFailed;
		}

		if (status.equals("UpdateStatusCheckFailed") || status.equals("UpdateStatusUpgradeFailed")) {
			return updateFailed;
		}
		if(newAvailable) {
			return updateAvailable;
		}
		return upToDate;

	}
	
	private String getAntivirusStatus() { 
		JSONObject queryResult = this.response.get("Antivirus.get");
		if (queryResult == null) {
			return checkFailed;
		}
		
		String antivirusStatus;
		
		try {
			JSONObject avConfig = queryResult.getJSONObject("config");
			JSONObject avStatus = avConfig.getJSONObject("antivirus");
			antivirusStatus = avStatus.getString("status");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getAntivirusStatus()", e.toString());
			return checkFailed;
		}
		
		if(antivirusStatus.equals("AntivirusNotActive")){
			return disabled;
		}
		
		if(antivirusStatus.equals("AntivirusInternalFailed")){
			return antivirusInternalFailed;
		}
		
		if(antivirusStatus.equals("AntivirusExternalFailed")){
			return antivirusExternalFailed;
		}
		
		if(antivirusStatus.equals("AntivirusBothFailed")){
			return antivirusBothFailed;
		}
		
		queryResult = this.response.get("Antivirus.getUpdateStatus");
		if (queryResult == null) {
			return checkFailed;
		}
		
		String avUpdateStatus;
		try {
			JSONObject avStatus = queryResult.getJSONObject("status");
			avUpdateStatus = avStatus.getString("phase");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getAntivirusStatus()", e.toString());
			return checkFailed;
		}
		
		if(avUpdateStatus.equals("AntivirusUpdateFailed")){
			return updateFailed;
		}
		
		
		return working;
		}

	public void sendRequests() {
		LinkedHashMap<String, JSONObject> requests = new LinkedHashMap<String, JSONObject>();
		
		requests.put("ProductInfo.getUptime", new JSONObject());
		requests.put("UpdateChecker.getStatus", new JSONObject());
		requests.put("Antivirus.get", new JSONObject());
		requests.put("Antivirus.getUpdateStatus", new JSONObject());
		requests.put("IntrusionPrevention.get", new JSONObject());
		requests.put("IntrusionPrevention.getUpdateStatus", new JSONObject());
		requests.put("ProductInfo.get", new JSONObject());
		
		JSONObject interfaceParams;
		try {
			interfaceParams = new JSONObject(
				"{\"sortByGroup\":true,\"query\":{" +
					"\"conditions\":[{\"fieldName\":\"type\",\"comparator\":\"Eq\",\"value\":\"VpnServer\"}]," +
					"\"combining\":\"Or\",\"orderBy\":[{\"columnName\":\"name\",\"direction\":\"Asc\"}]" +
				"}}"
			);
		} catch (JSONException e) {
			interfaceParams = new JSONObject();
		}
		
		requests.put("Interfaces.get", interfaceParams);
		
		this.response = this.client.execBatch(requests);
	}
	
	public String getIpsStatus() {
		this.sendRequests();
		JSONObject queryResult = this.response.get("IntrusionPrevention.get");
		if (queryResult == null) {
			return checkFailed;
		}
				
		boolean ipsEnabled; // TODO
		String updateStatus;
		
		try {
			JSONObject ipsConfig = queryResult.getJSONObject("config");
			ipsEnabled = ipsConfig.getBoolean("enabled");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getIpsStatus()", e.toString());
			return checkFailed;
		}
		
		queryResult = this.response.get("IntrusionPrevention.getUpdateStatus");
		try {
			updateStatus = queryResult.getString("status");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getIpsStatus()", e.toString());
			return disabled;
		}
		
		// IntrusionPreventionUpdateOk
		
		if (this.isUnregisteredTrial) {
			return notAvaibleUpdates;
		}
		
		if (updateStatus.equals("IntrusionPreventionUpdateError")) {
			return updateFailed;
		}
		
		return working;
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
	
	private String getWebFilterStatus() {
		
		if (this.isUnregisteredTrial) {
			return notAvaible;
		}
		
		JSONObject productInfo = this.response.get("ProductInfo.get");
		JSONObject queryResult;
		if(newVersion(productInfo)){
			queryResult = this.client.exec("ContentFilter.getUrlFilterConfig", new JSONObject());
		}else{
			queryResult = this.client.exec("HttpPolicy.getUrlFilterConfig", new JSONObject());
		}
		
		if (queryResult == null) {
			return checkFailed;
		}
		
		String status;
		boolean enabled;
		
		try {
			JSONObject config = queryResult.getJSONObject("config"); 
			status = config.getString("status");
			enabled = config.getBoolean("enabled");
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getWebFilterStatus()", e.toString());
			return checkFailed;
		}
		
		if (status.equals("UrlFilterNotLicensed")) {
			return unlicensed;
		}
		
		if (status.equals("UrlFilterNotActivated") && enabled) {
			return notActivated;
			
		}
		
		if ( ! enabled) {
			return disabled;
		}
		
		if (status.equals("UrlFilterActivated")) {
			return working;
		}
		
		if (status.equals("UrlFilterActivating")) {
			return activating;
		}
		
		return checkFailed;
	}
	
	private JSONObject getVpnServerJson() {
		
		JSONObject params;
		try {
			params = new JSONObject(
				"{\"sortByGroup\":true,\"query\":{" +
					"\"conditions\":[{\"fieldName\":\"type\",\"comparator\":\"Eq\",\"value\":\"VpnServer\"}]," +
					"\"combining\":\"Or\",\"orderBy\":[{\"columnName\":\"name\",\"direction\":\"Asc\"}]" +
				"}}"
			);
		} catch (JSONException e) {
			params = new JSONObject();
		}
		
		JSONObject queryResult = this.response.get("Interfaces.get");
		if (queryResult == null) {
			return null;
		}
		
		JSONArray ifaceList;
		JSONObject vpnServerJson;
		
		try {
			ifaceList = (JSONArray) queryResult.get("list");
			if (1 != ifaceList.length()) {
				Log.d("getVpnServerJson()", "more than 1 iface returned. Result would be inappropriate.");
			}
			vpnServerJson = ifaceList.getJSONObject(0);
		} catch (JSONException e) {
			vpnServerJson = null;
		}
		
		return vpnServerJson;
	}
	private String getIpsecVpnStatus(JSONObject vpnServerJson) {
		try {
			return vpnServerJson.getJSONObject("server").getBoolean("ipsecVpnEnabled")  ? working : disabled;
		} catch (JSONException e) {
			return checkFailed;
		}
	}
	private String getKerioVpnStatus(JSONObject vpnServerJson) {
		try {
			return vpnServerJson.getJSONObject("server").getBoolean("kerioVpnEnabled")  ? working : disabled;
		} catch (JSONException e) {
			return checkFailed;
		}
	}
	
	@Override
	public void execute() {
		
		SystemStatus ss = new SystemStatus();
		this.sendRequests();
		
		ss.uptime = this.getUptime();
		ss.uptimeRaw = this.uptimeRaw;
		ss.update = this.getControlUpdateStatus();
		ss.antivirus = this.getAntivirusStatus();
		ss.ips = this.getIpsStatus();
		ss.webFilter = this.getWebFilterStatus();
		
		JSONObject vpnServerJson = getVpnServerJson();
		
		ss.ipsec = null == vpnServerJson ? checkFailed : this.getIpsecVpnStatus(vpnServerJson);
		ss.kvpn = null == vpnServerJson ? checkFailed : this.getKerioVpnStatus(vpnServerJson);
		
		this.notify(ss);
	}
}
