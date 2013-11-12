package com.kerio.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.kerio.dashboard.api.ApiClient;

public class SystemHealthUpdater extends TileUpdater {

	public class HealthData {
		public class Summary {
			public double cpuUsage = 0;
			public long diskTotal = 0;
			public long diskFree = 0;
			public long memoryTotal = 0;
			public long memoryUsed = 0;
		}
		
		public Summary summary = new Summary();
		
		public double cpuLoad[];
		public double memoryLoad[];
		
		public long sampleTime;
	}
	
	public SystemHealthUpdater(Handler handler, ApiClient client) {
		super(handler, client, "SystemHealth.getInc");
	}
	
	@Override
	protected boolean initializeArguments() {
 		try {
 	 		this.arguments = new JSONObject();
 	 		this.arguments.put("startSampleTime", -1840);
 	 		this.arguments.put("type", "HistogramInterval20s");
 	 		return true;
 		} catch (JSONException e) {
 		}
 		return false;
	}
	
	@Override
	protected Object handleResponse(JSONObject response) {
		try 
		{
			JSONObject data = response.getJSONObject("data");
			if (data == null) {
				return null;
			}

			HealthData health = new HealthData();
			health.sampleTime = response.getLong("sampleTime");

			health.summary.diskTotal = data.getLong("diskTotal");
			health.summary.diskFree = data.getLong("diskFree");
			health.summary.memoryTotal = data.getLong("memoryTotal");
			health.summary.memoryUsed = (long) 0;
			health.summary.cpuUsage = (double) 0;

			JSONArray memoryValues = data.getJSONArray("memory");
			if (memoryValues.length() > 0) {
				health.summary.memoryUsed = Double.valueOf(health.summary.memoryTotal * memoryValues.getDouble(0) / 100).longValue();
			}
			
			if(memoryValues.length() < 92){  //graph shows 92 values, if there is not enough data, add 0
				for(int i=(memoryValues.length()-1);i<92;i++){ 
					memoryValues.put(i, 0);
				}
			}else{// if there is more then 92 values, strip the data so gui is not so loaded with computing
				JSONArray temp = new JSONArray();
				for(int i=0;i<92;i++){
					temp.put(i, memoryValues.getDouble(i));
				}
				memoryValues = temp;
			}
			
			
			health.memoryLoad = this.jsonArrayToArray(memoryValues);
			
			JSONArray cpuValues = data.getJSONArray("cpu");
			if (cpuValues.length() > 0) {
				health.summary.cpuUsage = cpuValues.getDouble(0);
			}
			
			if(cpuValues.length() < 92){ //graph shows 92 values, if there is not enough data, add 0
				for(int i=(cpuValues.length()-1);i<92;i++){
					cpuValues.put(i, 0);
				}
			}else{ // if there is more then 92 values, strip the data so gui is not so loaded with computing
				JSONArray temp = new JSONArray();
				for(int i=0;i<92;i++){
					temp.put(i, cpuValues.getDouble(i));
				}
				cpuValues = temp;
			}
			
			health.cpuLoad = this.jsonArrayToArray(cpuValues);

			return health;
		}
		catch (JSONException e) {
		}
		
		return null;
	}
	
	private double[] jsonArrayToArray(JSONArray jsonArray) throws JSONException
	{
		if (jsonArray.length() <= 0) {
			return new double[] {0};
		}
		
		double result[] = new double[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); ++i) {
			result[i] = jsonArray.getDouble(i);
		}

		return result;
	}
}
