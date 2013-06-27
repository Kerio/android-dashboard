package com.kerio.dashboard;

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

	private boolean isUnregisteredTrial;

	public class SystemStatus {
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
	private String computeUptimeString(int uptime) {
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
		JSONObject queryResult = this.client.exec("ProductInfo.getUptime", new JSONObject());

		try {
			if (queryResult != null) { 
				return computeUptimeString(queryResult.getInt("uptime"));
			}
		} catch (JSONException e) {
			Log.d("SystemStatusTile::getValue()", e.toString());
		}

		return "0d 00:00:00";
	}

	private String getControlUpdateStatus() {
		JSONObject queryResult = this.client.exec("UpdateChecker.getStatus", new JSONObject());
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
	
	private String getAntivirusStatus() { return notImplemented; }

	public String getIpsStatus() {
		
		JSONObject queryResult = this.client.exec("IntrusionPrevention.get", new JSONObject());
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
		
		queryResult = this.client.exec("IntrusionPrevention.getUpdateStatus", new JSONObject());

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
	
	private String getWebFilterStatus() {
		
		if (this.isUnregisteredTrial) {
			return notAvaible;
		}
		
		JSONObject queryResult = this.client.exec("HttpPolicy.getUrlFilterConfig", new JSONObject());
		
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
		
		JSONObject queryResult = this.client.exec("Interfaces.get", params);
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
		
		ss.uptime = this.getUptime();
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
