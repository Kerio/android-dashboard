package com.kerio.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kerio.dashboard.api.ApiClient;
import android.os.Handler;

public class TrafficChartUpdater extends TileUpdater {

	public class ChartData {
		public long sampleTime;
		public JSONArray in;
		public JSONArray out;
		public String unit;
	};
	
	String chartId;
	
	public TrafficChartUpdater(Handler handler, ApiClient client, String chartId) {
		super(handler, client);
		this.chartId = chartId;
		this.method = "TrafficStatistics.getHistogramInc";
	}

	@Override
	protected boolean initializeArguments() {
		try {
			this.arguments = new JSONObject();
			this.arguments.put("startSampleTime", -1840);
			this.arguments.put("type", "HistogramInterval20s");
			this.arguments.put("id", this.chartId);
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected Object handleResponse(JSONObject response) {
		try {
			
			JSONObject hist = response.getJSONObject("hist");
			JSONArray data = hist.getJSONArray("data");
			
			if (data == null) {
				return null;
			}

			ChartData cd = new ChartData();
			
			cd.sampleTime  = response.getLong("sampleTime");
			cd.in = new JSONArray();
			cd.out = new JSONArray();
			
			if(hist.getString("units").equals("Bytes")){
				cd.unit = "B/s";
				for (int i = 0; i < data.length(); ++i) {
					JSONObject sample = data.getJSONObject(i);
					
					cd.in.put(i, sample.getDouble("inbound"));
					cd.out.put(i, sample.getDouble("outbound"));
				}
			}else if(hist.getString("units").equals("KiloBytes")){
				cd.unit = "KB/s";
				for (int i = 0; i < data.length(); ++i) {
					JSONObject sample = data.getJSONObject(i);
					
					cd.in.put(i, sample.getDouble("inbound"));
					cd.out.put(i, sample.getDouble("outbound"));
				}
			}else if(hist.getString("units").equals("MegaBytes")){
				cd.unit = "KB/s";
				for (int i = 0; i < data.length(); ++i) {
					JSONObject sample = data.getJSONObject(i);
					
					cd.in.put(i, sample.getDouble("inbound")*1024); //I want the units, to be always  B/s or KB/s!
					cd.out.put(i, sample.getDouble("outbound")*1024);
				}
			}else if(hist.getString("units").equals("GigaBytes")){
				cd.unit = "KB/s";
				for (int i = 0; i < data.length(); ++i) {
					JSONObject sample = data.getJSONObject(i);
					
					cd.in.put(i, sample.getDouble("inbound")*1024*1024);
					cd.out.put(i, sample.getDouble("outbound")*1024*1024);
				}
			}else{
				cd.unit = "Unknown";
			}
			
			for(int i=(cd.in.length());i<92;i++){ //charts are displaying 92 values. If there is not values, complete it with zeros, so the chart is not deformed
				cd.in.put(i, 0);
				cd.out.put(i, 0);
			}
			return cd;
		} catch (JSONException e) {
		}
		
		return null;
	}
}
