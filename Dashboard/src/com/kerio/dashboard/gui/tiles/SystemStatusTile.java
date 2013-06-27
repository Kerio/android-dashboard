package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.SystemStatusUpdater;
import com.kerio.dashboard.SystemStatusUpdater.SystemStatus;
import com.kerio.dashboard.api.ApiClient;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class SystemStatusTile extends TextTile {

	/////////////////////////////////////////////////////////////////////////////////////////
	// SystemStatusHandler
	
	public class SystemStatusHandler extends Handler {
		SystemStatusTile tile;
		
		public SystemStatusHandler(SystemStatusTile tile) {
			this.tile = tile;
		}

		@Override
        public void handleMessage(Message msg) {
			
			if (msg.obj instanceof SystemStatus) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("SystemHealthHandler: unknown object type");
			}
		}
	}
	
	// SystemStatusHandler
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	
	private Pairs data;
	private SystemStatusHandler systemStatusHandler;
	private SystemStatusUpdater systemStatusUpdater;

	
	public SystemStatusTile(Context context, ApiClient client) {
		super(context, client);

		this.systemStatusHandler = new SystemStatusHandler(this);
        this.systemStatusUpdater = new SystemStatusUpdater(this.systemStatusHandler, client); // TODO: make it autolaunchable
        this.systemStatusUpdater.activate();
//        this.systemStatusHandler.post(this.systemStatusUpdater); 
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
		
		SystemStatus ss = (SystemStatus)data;
		
		this.data = new Pairs();

		// why are these damn values computed on the client?
		this.data.put("Uptime", ss.uptime);
		this.data.put("Kerio Control", ss.update);
		this.data.put("Antivirus", ss.antivirus);
		this.data.put("Intrusion Prevention", ss.ips);
		this.data.put("Kerio Control Web Filter", ss.webFilter);

		this.data.put("IPsec VPN Server", ss.ipsec);
		this.data.put("Kerio VPN Server", ss.kvpn);
		
		this.update();
	}

}
