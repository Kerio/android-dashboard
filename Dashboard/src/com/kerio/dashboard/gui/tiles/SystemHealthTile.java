package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import org.json.JSONArray;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.kerio.dashboard.SystemHealthUpdater;
import com.kerio.dashboard.SystemHealthUpdater.HealthData;
import com.kerio.dashboard.SystemHealthUpdater.HealthData.Summary;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.gui.tiles.TextTile.Pairs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SystemHealthTile extends Tile {

	private HealthSummary summary;
	private SystemHealthGraph graph;

	/////////////////////////////////////////////////////////////////////////////////////////
	// SystemHealthGraph

	public class SystemHealthGraph extends TimeGraphTile {

		private GraphViewSeries cpuSeries;
		private GraphViewSeries memorySeries;
		private GraphViewData[] cpuSeriesData;
		private GraphViewData[] memorySeriesData;

		public SystemHealthGraph(Context context) {
			super(context, 90, 30 * 60, 5 * 60);
			this.setYAxisBounds(100, 0);
			this.setVerticalLabels(new String[] {"100%", "75%", "50%", "25%", "0%"});
		}

		@Override
		public void setValues(JSONArray values) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void initSeries() {
			this.cpuSeries = new GraphViewSeries("CPU", new GraphViewSeriesStyle(Color.GREEN, 3), new GraphViewData[]{});
			this.addSeries(this.cpuSeries);

			this.memorySeries = new GraphViewSeries(new GraphViewData[]{});
			this.addSeries(this.memorySeries);
		}

		@Override
		public void update() {
			if (this.cpuSeriesData != null) {
				this.cpuSeries.resetData(this.cpuSeriesData);
			}
			if (this.memorySeriesData != null) {
				this.memorySeries.resetData(this.memorySeriesData);
			}

			this.updateLabels();
		}

		public void setValues(JSONArray cpuValues, JSONArray memValues, long currentTime) {
			this.cpuSeriesData = this.valuesToSeriesData(cpuValues);
			this.memorySeriesData = this.valuesToSeriesData(memValues);
			this.setCurrentTime(currentTime);
		}
		public void setValues(double[] cpuLoad, double[] memoryLoad, long sampleTime) {
			this.cpuSeriesData = this.valuesToSeriesData(cpuLoad);
			this.memorySeriesData = this.valuesToSeriesData(memoryLoad);
			this.setCurrentTime(sampleTime);
		}

		@Override
		public void setData(Object data) {
		}

		@Override
		public void activate() {
		}

		@Override
		public void deactivate() {
			// TODO Auto-generated method stub

		}

	}

	// SystemHealthGraph
	///////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	// HealthSummary

	private class HealthSummary extends TextTile {

		public HealthSummary(Context context) {
			super(context, null);
			init();
		}

		private void init() {
			this.cpuUsage = 0;
			this.diskTotal = 0;
			this.diskFree = 0;
			this.memoryTotal = 0;
			this.memoryUsed = 0;
		}

		@Override
		public Pairs getKeyValuePairs() {
			Pairs result = new Pairs();

			result.put("RAM", String.format("%.2f GB of %.2f GB used", Double.valueOf(this.memoryUsed) / (1024*1024), Double.valueOf(this.memoryTotal) / (1024*1024)));
			result.put("CPU", String.format("%.2f%%", this.cpuUsage));
			result.put("Disk", String.format("%.2f GB of %.2f GB used", Double.valueOf(this.diskTotal - this.diskFree) / (1024*1024*1024), Double.valueOf(this.diskTotal) / (1024*1024*1024)));
			return result;
		}

		@Override
		protected TextView renderKeyView(Pairs.Entry<String, String> entry) {
			TextView keyView = super.renderKeyView(entry);
			keyView.setPadding(10, 0, 20, 0);

			return keyView;
		}

		private double cpuUsage;
		private long diskTotal;
		private long diskFree;
		private long memoryTotal;
		private long memoryUsed;

		@Override
		public void setData(Object data) {
			if (!(data instanceof Summary)) {
				throw new InvalidParameterException("HealthData.Summary expected");
			}

			Summary sm = (Summary)data;

			this.diskTotal = sm.diskTotal;
			this.diskFree = sm.diskFree;
			this.memoryTotal = sm.memoryTotal;
			this.memoryUsed = sm.memoryUsed;
			this.cpuUsage = sm.cpuUsage;
		}

		@Override
		public void activate() {
		}

		@Override
		public void deactivate() {
		}

	}

	// HealthSummary
	///////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	// SystemHealthHandler

	public class SystemHealthHandler extends TileHandler {
		SystemHealthTile tile;

		public SystemHealthHandler(SystemHealthTile tile) {
			super(tile);
			this.tile = tile;
		}

		@Override
        public void handleMsg(Message msg) {
			if (msg.obj instanceof HealthData) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("TrafficChartHandler: unknown object type");
			}
		}
	}

	// SystemHealthHandler
	/////////////////////////////////////////////////////////////////////////////////////////


	private SystemHealthHandler systemHealthHandler;
	private SystemHealthUpdater systemHealthUpdater;

	public SystemHealthTile(Context context, ApiClient client) {
		super(context, client);
		this.summary = new HealthSummary(context);
		this.graph = new SystemHealthGraph(context);

		this.addView(this.summary);
		this.addView(this.graph);
		this.setOrientation(LinearLayout.VERTICAL);

		this.systemHealthHandler = new SystemHealthHandler(this);
        this.systemHealthUpdater = new SystemHealthUpdater(this.systemHealthHandler, client); // TODO: make it autolaunchable
        this.systemHealthUpdater.activate();
	}

	public void onUpdateError(String error)
	{
		// TODO
	}

	@Override
	public void setData(Object data) {
		if (!(data instanceof HealthData)) {
			throw new InvalidParameterException("HealthData expected");
		}

		HealthData hd = (HealthData)data;
		this.graph.setValues(hd.cpuLoad, hd.memoryLoad, hd.sampleTime);
		this.graph.update();

		this.summary.setData(hd.summary);
		this.summary.update();
	}


	@Override
	public void update() {
	}

	@Override
	public void activate() {
		 this.systemHealthUpdater.activate();
	}

	@Override
	public void deactivate() {
		 this.systemHealthUpdater.deactivate();
	}
}
