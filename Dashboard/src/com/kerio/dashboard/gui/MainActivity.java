package com.kerio.dashboard.gui;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerStatusUpdater;
import com.kerio.dashboard.api.TrustStoreHelper;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.config.gui.CertificateStoreActivity;
import com.kerio.dashboard.config.gui.SettingActivity;
import com.kerio.dashboard.gui.tiles.ServerTile;
import com.kerio.dashboard.gui.tiles.ServerTile.State;

public class MainActivity extends Activity {

	private static final int RESULT_SETTINGS = 1;
	private static final int RESULT_CERTIFICATE = 2;
	
	@SuppressWarnings("unused")
	private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

	private ServerStatusHandler statusHandler = null;
	private ServerStatusUpdater serverStatusUpdater;
	private LinearLayout layout = null;
	private TextView notifyText = null;
	
	private TrustStoreHelper trustHelper = null;

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
			
			this.mainActivity.displayText(servers.isEmpty() ? R.string.main_empty : 0);
			
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
        final ServerTile meTile = tile;

        OnClickListener serverClickListener = new OnClickListener() {
        	@Override
        	public void onClick(View v) {
    			meTile.touchFeedback();
    			
        		if (meTile.tileStatus == ServerTile.State.Ok){	
        			me.showDashboard(server);
        		}else if(meTile.tileStatus == State.CertWarning){
        	    	meTile.setServerStatus(MainActivity.this.serverStatusUpdater.new ServerStatus(server, meTile.getHandler(), MainActivity.this.trustHelper.getKeystore()));
        			me.showCertWarning(meTile, server.getCertChain(), meTile.getHandler());
        		}
        	}
        };
        tile.setOnClickListener(serverClickListener);
    }

    public void removeServerTile(ServerTile tile) {
    	this.layout.removeView(tile);
	}
    
    public void displayText(int resId) {
    	if (resId == 0) {
    		this.layout.removeView(this.notifyText);
    	}
    	else {
    		this.notifyText.setText(resId);
    		if (this.layout.getChildAt(0) != this.notifyText) {
    			this.layout.addView(this.notifyText, 0);
    		}
    	}
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
    
	private void showCertWarning(ServerTile tile, X509Certificate certChain[], Handler handler) {
    	CertificateWarningDialog dialog = new CertificateWarningDialog();
    	dialog.setCertChain(certChain);
    	dialog.setServerTile(tile);
    	dialog.setTrustHelper(this.trustHelper);
    	dialog.show(getFragmentManager(), "CertificateWarningDialog");
    }

	// Different event handlers
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		SharedPreferences settings = this.getSharedPreferences(Config.SERVER_CONFIG, Activity.MODE_PRIVATE);
		
		this.notifyText = new TextView(this);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.notifyText.setLayoutParams(params);
		this.notifyText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		this.notifyText.setTypeface(null, Typeface.ITALIC);
		
		this.layout = (LinearLayout)findViewById(R.id.serverList);
		
		displayText(R.string.main_loading);
		
		this.statusHandler = new ServerStatusHandler(this);
		
		if(this.trustHelper == null){
			this.trustHelper = new TrustStoreHelper(this);
		}
		
		this.serverStatusUpdater = new ServerStatusUpdater(this.statusHandler, settings, this.trustHelper.getKeystore());
		this.serverStatusUpdater.activate();
    }
	
	@Override
	protected void onDestroy() {
		this.trustHelper.store();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		this.serverStatusUpdater.activate();
		super.onResume();
	}
	
	protected void onPause() {
		this.serverStatusUpdater.deactivate();
		super.onPause();
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent = null;
    	switch (item.getItemId()) {      
	        case R.id.action_settings:
	            intent = new Intent(this, SettingActivity.class);
	            startActivityForResult(intent, RESULT_SETTINGS);
	            break;
	        case R.id.action_certificates:
	            intent = new Intent(this, CertificateStoreActivity.class);
	            startActivityForResult(intent, RESULT_CERTIFICATE);
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
