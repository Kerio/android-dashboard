package com.kerio.dashboard.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerStatusUpdater;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.config.gui.SettingActivity;
import com.kerio.dashboard.gui.tiles.ServerTile;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private static final int RESULT_SETTINGS = 1;

	private ServerStatusHandler statusHandler = null;
	private ServerStatusUpdater serverStatusUpdater;
	private LinearLayout layout = null;

	private class ServerStatusHandler extends Handler {

		MainActivity mainActivity;

		@SuppressLint("UseSparseArrays")
		private Map<Integer, ServerTile> existingTiles = new HashMap<Integer, ServerTile>(10);
		
		public ServerStatusHandler(MainActivity mainActivity) {
			this.mainActivity = mainActivity;
		}

		@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message msg) {
			if (msg.obj instanceof List<?>) {
				this.updateServerList((List<ServerConfig>)msg.obj);
			} else if (msg.obj instanceof ServerStatusUpdater.ServerStatus) {
				this.updateServer((ServerStatusUpdater.ServerStatus)msg.obj);
			} else {
				throw new RuntimeException("MainActivity: unknown object type");
			}
		}
		
		synchronized private void updateServerList(List<ServerConfig> servers) {
			@SuppressLint("UseSparseArrays")
			Map<Integer, ServerTile> newTiles = new HashMap<Integer, ServerTile>(servers.size());

			for (ServerConfig server : servers) {
				Integer id = Integer.valueOf(server.id);
				ServerTile tile = this.existingTiles.get(id);
				if (tile == null) {
					tile = new ServerTile(this.mainActivity, server);
					this.mainActivity.addServerTile(tile, server);
				}else{
					this.existingTiles.remove(id);
				}
				newTiles.put(id, tile);
			}

			for (ServerTile tile : this.existingTiles.values()) {
				this.mainActivity.removeServerTile(tile);
			}

			this.existingTiles = newTiles;
		}

		synchronized private void updateServer(ServerStatusUpdater.ServerStatus status) {
			Integer id = Integer.valueOf(status.getConfig().id);
			ServerTile tile = this.existingTiles.get(id);
			if (tile == null) {
				tile = new ServerTile(layout.getContext(), status.getConfig());
				this.mainActivity.addServerTile(tile, status.getConfig());
				this.existingTiles.put(id, tile);
			}
			tile.setData(status);
		}
	} // ServerStatusHandler

	// Add/Remove tiles
	private void addServerTile(ServerTile tile, final ServerConfig server) {
        this.layout.addView(tile);
        final MainActivity me = this;

        OnClickListener serverClickListener = new OnClickListener() {
        	@Override
        	public void onClick(View v) {
    			me.showDashboard(server);
        	}
        };
        tile.setOnClickListener(serverClickListener);
    }

    public void removeServerTile(ServerTile tile) {
    	this.layout.removeView(tile);
	}

    // Display dashboard for selected server
    private void showDashboard(ServerConfig server) {
    	Intent myIntent = new Intent(this, ServerActivity.class);
    	myIntent.putExtra("server", server.server);
    	myIntent.putExtra("username", server.username);
    	myIntent.putExtra("password", server.password);
    	myIntent.putExtra("desc", server.description);
    	startActivity(myIntent);
    }
    
    // Different event handlers
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		SharedPreferences settings = this.getSharedPreferences(Config.SERVER_CONFIG, Activity.MODE_PRIVATE);
		
		this.layout = (LinearLayout)findViewById(R.id.serverList);
		this.statusHandler = new ServerStatusHandler(this);
		
		this.serverStatusUpdater = new ServerStatusUpdater(this.statusHandler, settings);
		this.serverStatusUpdater.activate();
    }

	@Override
	protected void onDestroy() {
		this.serverStatusUpdater.deactivate();
		super.onDestroy();
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.action_settings:
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, RESULT_SETTINGS);
            break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SETTINGS:
            break;
        }
    }
}