package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;
import org.json.JSONArray;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.kerio.dashboard.TrafficChartUpdater;
import com.kerio.dashboard.TrafficChartUpdater.ChartData;
import com.kerio.dashboard.api.ApiClient;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

public class TrafficChartTile extends Tile {
	
	public class TrafficGraph extends TimeGraphTile {

		private GraphViewSeries inSeries;
		private GraphViewSeries outSeries;
		private GraphViewData[] inSeriesData;
		private GraphViewData[] outSeriesData;

		public TrafficGraph(Context context) {
			super(context, 90, 30 * 60, 5 * 60);
		}
		
		@Override
		public void setValues(JSONArray values) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void initSeries()
		{
			this.inSeries = new GraphViewSeries(new GraphViewData[]{});
			this.addSeries(this.inSeries);
			
			this.outSeries = new GraphViewSeries("out", new GraphViewSeriesStyle(Color.RED, 3), new GraphViewData[]{});
			this.addSeries(this.outSeries);
		}
		
		@Override
		public void update() {
			if (this.inSeriesData != null) {
				this.inSeries.resetData(this.inSeriesData);
			}
			if (this.outSeriesData != null) {
				this.outSeries.resetData(this.outSeriesData);
			}
						
			this.updateLabels();
		}		
		
		public void setValues(JSONArray inValues, JSONArray outValues, long currentTime) {
			this.inSeriesData = this.valuesToSeriesData(inValues);
			this.outSeriesData = this.valuesToSeriesData(outValues);
			this.setCurrentTime(currentTime);
		}

		@Override
		public void setData(Object data) {
		}
	}

	// TrafficGraph
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// TraffiChartHandler
	
	public class TrafficChartHandler extends Handler {
		TrafficChartTile tile;
		
		public TrafficChartHandler(TrafficChartTile tile) {
			this.tile = tile;
		}

		@Override
        public void handleMessage(Message msg) {
			
			if (msg.obj instanceof ChartData) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("TrafficChartHandler: unknown object type");
			}
		}
	}
	
	// TrafficChartHandler
	/////////////////////////////////////////////////////////////////////////////////////////
	
	
	private TrafficGraph graph;
	
	private TrafficChartUpdater trafficChartUpdater;
	private TrafficChartHandler trafficChartHandler;

	public TrafficChartTile(Context context, ApiClient client, String chartId) {
		super(context, client);
		this.graph = new TrafficGraph(context);
		this.addView(this.graph);
		this.setOrientation(LinearLayout.VERTICAL);
		
		this.trafficChartHandler = new TrafficChartHandler(this);
        this.trafficChartUpdater = new TrafficChartUpdater(this.trafficChartHandler, client, chartId); // TODO: make it autolaunchable
        this.trafficChartUpdater.activate();
	}

	public void onUpdateError(String error)
	{
		// TODO
	}
	
	@Override
	public void setData(Object data) {
		if (!(data instanceof ChartData)) {
			throw new InvalidParameterException("ChartData expected");
		}
		
		ChartData cd = (ChartData)data;
		this.graph.setValues(cd.in, cd.out, cd.sampleTime);
		this.graph.update();
	}

	@Override
	public void update() {
		// TODO Remove from parent ?
		
	}
}
