package com.kerio.dashboard.gui.tiles;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.kerio.dashboard.api.ApiClient;

public abstract class GraphTile extends Tile {

	/**  
	 * @param context
	 * @param capacity max amount of values to display
	 */
	public GraphTile(Context context, int capacity) {
		super(context, (ApiClient)null);
		this.capacity = capacity;

		this.seriesData = new GraphViewData[this.capacity];
		this.graph = new LineGraphView(this.getContext(), "");
		((LineGraphView)this.graph).setDrawBackground(false);
		
		this.graph.getGraphViewStyle().setGridColor(Color.LTGRAY);
		this.graph.getGraphViewStyle().setHorizontalLabelsColor(Color.GRAY);
		this.graph.getGraphViewStyle().setVerticalLabelsColor(Color.GRAY);
		
		this.graph.getGraphViewStyle().setTextSize(12);
		
		this.setVerticalLabels(null);
		this.setHorizontalLabels(null);
		this.initSeries();
		
		this.addView(this.graph);
		this.setOrientation(LinearLayout.VERTICAL);
		this.setMinimumHeight(200);
	}
	
	protected void setYAxisBounds(int max, int min) {
		this.graph.setManualYAxisBounds(max, min);
	}
	
	protected void initSeries()
	{
		this.defaultSeries = new GraphViewSeries(new GraphViewData[]{});
		this.addSeries(this.defaultSeries);
	}
	
	protected void addSeries(GraphViewSeries series) {
		this.graph.addSeries(series);
	}
	
	protected void setVerticalLabels(String[] verticalLabels) {
		this.verticalLabels = verticalLabels; 
	}
	
	protected void setHorizontalLabels(String[] horizontalLabels) {
		this.horizontalLabels = horizontalLabels;
	}
	
	@Override
	public void update() {
		if (this.seriesData != null) {
			this.defaultSeries.resetData(seriesData);
		}
		
		this.updateLabels();
	}
	
	protected void updateLabels()
	{
		if (this.horizontalLabels != null) {
			this.graph.setHorizontalLabels(this.horizontalLabels);
		}
		
		if (this.verticalLabels != null) {
			this.graph.setVerticalLabels(this.verticalLabels);
		}
	}
	
	protected GraphViewData[] valuesToSeriesData(double[] values)
	{
		if (values.length == 0) {
			return null;
		}
		
		int len = Math.min(this.capacity, values.length);
		GraphViewData[] data = new GraphViewData[len];
		
		for (int i = 0; i < len; ++i) {
			data[len - i - 1] = new GraphViewData(i, values[i]);
		}
		
		return data;
	}
	
	protected GraphViewData[] valuesToSeriesData(JSONArray values)
	{
		if (values.length() == 0) {
			return null;
		}
		
		try {
			int len = Math.min(this.capacity, values.length());
			GraphViewData[] data = new GraphViewData[len];
			
			for (int i = 0; i < len; ++i) {
				data[len - i - 1] = new GraphViewData(i, values.getDouble(i));
			}
			
			return data;
			
		} catch (JSONException e) {
		}
		return null;
	}
	
	public void setValues(JSONArray values)
	{
		this.seriesData = this.valuesToSeriesData(values);
	}
	
	protected int getCapacity() {
		return this.capacity;
	}
	
	private int capacity;
	private String[] verticalLabels;
	private String[] horizontalLabels;
	private GraphViewData[] seriesData;
	private GraphViewSeries defaultSeries;
	private GraphView graph;
}
