package com.kerio.dashboard.gui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerDashboardUpdater;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.gui.tiles.NotificationTile;
import com.kerio.dashboard.gui.tiles.Tile;
import com.kerio.dashboard.gui.tiles.TileFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

//public class ServerActivity extends Activity implements Runnable {
public class ServerActivity extends Activity {

	private class ServerDashboardHandler extends Handler {

		ServerActivity activity;
		Map<String, Tile> tiles;
		TileFactory tileFactory;
		
		public ServerDashboardHandler(ServerActivity activity, ApiClient client) {
			this.activity = activity;
			this.tileFactory = new TileFactory(activity, client);
			this.tiles = new HashMap<String, Tile>();
		}

		@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message msg) {

			if (msg.obj instanceof String) {
				String str = (String)msg.obj;
				if (str == "UpdateStarted") {
					this.activity.onUpdateStarted();
				} else if (str == "UpdateDone") {
					this.activity.setText(getString(R.string.loadingText));
				} else if (str == "tileUpdated") {
					this.onUpdateTile();
				} else {
					this.activity.setText(str);
				}
			} else if (msg.obj instanceof Map<?, ?>) {
				this.updateTiles((Map<String, JSONObject>)msg.obj);
			} else {
				throw new RuntimeException("ServerActivity: unknown object type");
			}
		}
		
		private String createKey(String tileType, JSONObject tileConfig) {
			if (tileConfig == null) {
				return tileType;
			}
			
			return tileType + tileConfig.toString();
		}
		
		private Tile getOrCreateTile(String tileKey, String tileType, JSONObject tileConfig)
		{
			Tile existingTile = this.tiles.get(tileKey);
			if (existingTile != null) {
				this.tiles.remove(tileKey);
				return existingTile;
			}
			
			existingTile = this.tileFactory.create(tileType, tileConfig);
			
			if (existingTile != null) {
				if ( ! existingTile.isReady()) {
					existingTile.setFinalHandler(dashboardSettingsHandler);
				}
				this.activity.addTile(existingTile);
			}
			
			return existingTile;
		}

		synchronized private void updateTiles(Map<String, JSONObject> tiles) {
			
			Map<String, Tile> newTiles = new LinkedHashMap<String, Tile>(tiles.size());

			for (String tileType : tiles.keySet()) {
				
				JSONObject tileConfig = tiles.get(tileType);
				
				String tileKey = this.createKey(tileType, tileConfig);
				Tile newTile = this.getOrCreateTile(tileKey, tileType, tileConfig);
				if (newTile != null) {
					newTiles.put(tileKey, newTile);
				}
			}

			for (Tile tile : this.tiles.values()) {
				this.activity.removeTile(tile);
			}

			this.tiles = newTiles;
		}
		
		synchronized private void onUpdateTile() {
			boolean done = true;
			for (Map.Entry<String, Tile> entry : this.tiles.entrySet()) {
				if ( ! entry.getValue().isReady()) {
					done = false;
					Log.d("ServerActivity", "Tile not ready: " + entry.getKey());
					break;
				}
			}
			
			if (done) {
				this.activity.onUpdateDone();
			}
		}
	}
	
	private ServerDashboardHandler dashboardSettingsHandler;
	private ServerDashboardUpdater dashboardUpdater;
	private LinearLayout dashboard;
	private LinearLayout loading; // initial loading indicator
	private NotificationTile notifications; // Notifications tile

	public void onUpdateStarted()
	{
	}

	public void onUpdateDone()
	{
		this.loading.setVisibility(View.GONE);
		this.notifications.setVisibility(View.VISIBLE);
		this.dashboard.setVisibility(View.VISIBLE);
	}

	public void setText(String error)
	{
		loadingSetText(error);
	}

	// Add or remove tile to the dashboard
	public void addTile(Tile tile) {
		this.dashboard.addView(tile);
	}
	
	public void removeTile(Tile tile) {
		this.dashboard.removeView(tile);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

        ServerConfig config = new ServerConfig();
		Intent intent = getIntent();
		
		config.server = intent.getStringExtra("server");
		config.username = intent.getStringExtra("username");
		config.password = intent.getStringExtra("password");
		config.description = intent.getStringExtra("desc");

    	this.dashboard = (LinearLayout)findViewById(R.id.dashboard);
    	this.dashboard.setVisibility(View.GONE);
        this.loading = (LinearLayout)findViewById(R.id.loading);
        
        ApiClient apiClient = new ApiClient(config.server);

        // Notifications tile should present always
        this.notifications = new NotificationTile(this, apiClient);
        this.notifications.setVisibility(View.GONE);
        
        this.dashboard.addView(this.notifications);
        
		this.dashboardSettingsHandler = new ServerDashboardHandler(this, apiClient);
		this.notifications.setFinalHandler(this.dashboardSettingsHandler);
        this.dashboardUpdater = new ServerDashboardUpdater(this.dashboardSettingsHandler, apiClient, config); // TODO: make it autolaunchable
        this.dashboardUpdater.activate();

		loadingSetText(getString(R.string.connectingText));
		setTitle(config.description);
	}
	
	@Override
	protected void onDestroy() {
		this.dashboardUpdater.deactivate();
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		this.dashboardUpdater.deactivate();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server, menu);
		return true;
	}
	
	public void loadingSetText(String text) {
		TextView textView = (TextView)findViewById(R.id.loadingText);
		if (textView != null) {
			textView.setText(text);
		}
	}

//	private void showError(String msg) {
//		TextView c = new TextView(this);
//    	c.setText(msg);
//    	LinearLayout dashboard = (LinearLayout)findViewById(R.id.dashboard);
//    	dashboard.addView(c);
//	}
}
