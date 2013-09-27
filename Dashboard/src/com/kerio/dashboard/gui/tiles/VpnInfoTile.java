package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.os.Message;

import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.VpnInfoTileUpdater;
import com.kerio.dashboard.VpnInfoTileUpdater.VpnInfo;
import com.kerio.dashboard.api.ApiClient;

public class VpnInfoTile extends TextTile{

/////////////////////////////////////////////////////////////////////////////////////////
//SystemStatusHandler
public class VpnInfoTileHandler extends TileHandler {
	VpnInfoTile tile;

	public VpnInfoTileHandler(VpnInfoTile tile) {
		super(tile);
		this.tile = tile;
	}

	@Override
	public void handleMsg(Message msg) {

		if (msg.obj instanceof VpnInfo) {
			this.tile.setData(msg.obj);
		}
		else if (msg.obj instanceof String) {
			this.tile.onUpdateError((String)msg.obj);
		} else {
			throw new RuntimeException("VpnInfoTileHandler: unknown object type");
		}
	}
}
//SystemStatusHandler
/////////////////////////////////////////////////////////////////////////////////////////

private Pairs data;
private VpnInfoTileHandler vpnInfoTileHandler;
private VpnInfoTileUpdater vpnInfoTileUpdater;

public VpnInfoTile(Context context, ApiClient client) {
	super(context, client);

	this.vpnInfoTileHandler = new VpnInfoTileHandler(this);
    this.vpnInfoTileUpdater = new VpnInfoTileUpdater(this.vpnInfoTileHandler, client); // TODO: make it autolaunchable
    this.vpnInfoTileUpdater.activate();
//    this.systemStatusHandler.post(this.systemStatusUpdater); 
}

@Override
public Pairs getKeyValuePairs() { return this.data; }

public void onUpdateError(String error) {
	// TODO
}

@Override
public void setData(Object data) {
	if (!(data instanceof VpnInfo)) {
		throw new InvalidParameterException("VpnInfo expected");
	}
	
	VpnInfo vi = (VpnInfo)data;
	this.data = new Pairs();
	this.data.put("VPN Server", vi.clients+" clients connected.");
	if(vi.tunnels != null){
		for(int i=0;i<vi.tunnels.length;i++){
			this.data.put(vi.tunnels[i][0], vi.tunnels[i][1]);
		}
	}
	this.update();
}

@Override
public void activate() {
	 this.vpnInfoTileUpdater.activate();		
}

@Override
public void deactivate() {
	 this.vpnInfoTileUpdater.deactivate();		
}
}
