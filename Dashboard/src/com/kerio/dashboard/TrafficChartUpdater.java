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
		public String[] labels;
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
			
			if(hist.getString("units").equals("Bytes")){ //Correct the numbers according to units
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
			
			
			if(cd.in.length()<92){ //charts are displaying 92 values. If there is not enough values, complete it with zeros, so the chart is not deformed
				for(int i=(cd.in.length());i<92;i++){ 
					cd.in.put(i, 0);
					cd.out.put(i, 0);
				}
			}else{//charts that have more than 92 values should be stripped, so that gui is not computing that much
				JSONArray tempin = new JSONArray();
				JSONArray tempout = new JSONArray();
				for(int i=0;i<92;i++){
					tempin.put(i, cd.in.getDouble(i));
					tempout.put(i, cd.out.getDouble(i));
				}
				cd.in = tempin;
				cd.out = tempout;
			}
			
			double bin = biggestNumber(cd.in);//Compute labels for charts
			double bout = biggestNumber(cd.out);
			if(bin > bout){
				cd.labels = makeLabels(bin, cd.unit);
			}else{
				cd.labels = makeLabels(bout, cd.unit);
			}
			
			return cd;
		} catch (JSONException e) {
		}
		
		return null;
	}
	
	private String[] makeLabels(double num, String unit){
		String[] result = new String[5];
		double step = num / (double)4;
		
		result[0] = formatNumber(String.valueOf(num))+" "+unit;
		result[1] = formatNumber(String.valueOf(num - step))+" "+unit;
		result[2] = formatNumber(String.valueOf(num - (2*step)))+" "+unit;
		result[3] = formatNumber(String.valueOf(num - (3*step)))+" "+unit;
		result[4] = "0"+" "+unit;
		
		return result;
	}
	
	private String formatNumber(String number){
		if(!number.contains(".")){
			return number;
		}
		
		int decimalPoint = number.indexOf(".");
		String result = number.substring(0, decimalPoint);
		return result;
			
	}
	
	private double biggestNumber(JSONArray array){
		double num = 0;
		try{
			for(int i=0;i<92;i++){ // 92 is number of values which are displayed in graph
				if(array.getDouble(i) > num){
					num = array.getDouble(i);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return num;
	}
}
