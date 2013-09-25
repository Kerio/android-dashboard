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
			for (int i = 0; i < data.length(); ++i) {
				JSONObject sample = data.getJSONObject(i);
				
				cd.in.put(i, sample.getDouble("inbound"));
				cd.out.put(i, sample.getDouble("outbound"));
			}
			return cd;
		} catch (JSONException e) {
		}
		
		return null;
	}
}
