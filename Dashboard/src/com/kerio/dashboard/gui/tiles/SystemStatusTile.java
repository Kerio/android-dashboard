package com.kerio.dashboard.gui.tiles;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.kerio.dashboard.SystemStatusUpdater;
import com.kerio.dashboard.SystemStatusUpdater.SystemStatus;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;

public class SystemStatusTile extends TextTile {

	/////////////////////////////////////////////////////////////////////////////////////////
	// SystemStatusHandler
	
	public class SystemStatusHandler extends TileHandler {
		SystemStatusTile tile;
		
		public SystemStatusHandler(SystemStatusTile tile) {
			super(tile);
			this.tile = tile;
		}

		@Override
        public void handleMsg(Message msg) {
			
			if (msg.obj instanceof SystemStatus) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("SystemStatusHandler: unknown object type");
			}
		}
	}
	
	// SystemStatusHandler
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	
	private Pairs data;
	private SystemStatusHandler systemStatusHandler;
	private SystemStatusUpdater systemStatusUpdater;
	private Handler updateHandler = new Handler();
	private Integer uptimeRaw = null;

	
	public SystemStatusTile(Context context, ApiClient client) {
		super(context, client);

		this.systemStatusHandler = new SystemStatusHandler(this);
        this.systemStatusUpdater = new SystemStatusUpdater(this.systemStatusHandler, client); // TODO: make it autolaunchable
        this.systemStatusUpdater.activate();
	}
	
	public void onUpdateError(String str) {
		// TODO
	}

	@Override
	public Pairs getKeyValuePairs() { return this.data; }

	@Override
	public void setData(Object data) {
		if ( ! (data instanceof SystemStatus)) {
			throw new RuntimeException("SystemStatusTile: Unexpected data type");
		}
		updateHandler.removeCallbacks(updateTimer);
		
		SystemStatus ss = (SystemStatus)data;
		
		this.data = new Pairs();
		
		this.uptimeRaw = ss.uptimeRaw; 

		this.data.put("Uptime", ss.uptime);
		this.data.put("Kerio Control", ss.update);
		this.data.put("Antivirus", ss.antivirus);
		this.data.put("Intrusion Prevention", ss.ips);
		this.data.put("Kerio Control Web Filter", ss.webFilter);

		this.data.put("IPsec VPN Server", ss.ipsec);
		this.data.put("Kerio VPN Server", ss.kvpn);
		
		this.update();
		updateHandler.postDelayed(updateTimer, 1000);
	}
	
	private Runnable updateTimer = new Runnable() {
		@Override
		public void run() {
			uptimeRaw++;
			data.put("Uptime", SystemStatusUpdater.computeUptimeString(uptimeRaw));
			update();
			updateHandler.postDelayed(this, 1000);
		}
	};
	
	@Override
	public void activate() {
		 this.systemStatusUpdater.activate();		
	}

	@Override
	public void deactivate() {
		 this.systemStatusUpdater.deactivate();		
	}
}
