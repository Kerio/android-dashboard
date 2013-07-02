package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import com.kerio.dashboard.SystemUpdater;
import com.kerio.dashboard.SystemUpdater.SystemInfo;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;
import android.content.Context;
import android.os.Message;

public class SystemTile extends TextTile {

	/////////////////////////////////////////////////////////////////////////////////////////
	// SystemHandler
	
	public class SystemHandler extends TileHandler {
		SystemTile tile;
		
		public SystemHandler(SystemTile tile) {
			super(tile);
			this.tile = tile;
		}

		@Override
        public void handleMsg(Message msg) {
			if (msg.obj instanceof SystemInfo) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("SystemHandler: unknown object type");
			}
		}
	}
	
	// SystemHandler
	/////////////////////////////////////////////////////////////////////////////////////////
	
	
	private Pairs data;
	private SystemUpdater systemUpdater;
	private SystemHandler systemHandler;
	
	public SystemTile(Context context, ApiClient client) {
		super(context, client);

		this.systemHandler = new SystemHandler(this);
        this.systemUpdater = new SystemUpdater(this.systemHandler, client); // TODO: make it autolaunchable
        this.systemUpdater.activate();
	}

	@Override
	public Pairs getKeyValuePairs() { return this.data; }
	
	public void onUpdateError(String error) {
		// TODO
	}

	@Override
	public void setData(Object data) {
		if (!(data instanceof SystemInfo)) {
			throw new InvalidParameterException("SystemInfo expected");
		}
		
		SystemInfo si = (SystemInfo)data;
		this.data = new Pairs();
		this.data.put("Kerio Control", si.versionString);
		this.data.put("Hostname", si.hostname);
		this.data.put("Operating System", si.osDescription);
		this.update();
	}

}
