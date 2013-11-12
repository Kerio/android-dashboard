package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.os.Message;

import com.kerio.dashboard.LicenseTileUpdater;
import com.kerio.dashboard.LicenseTileUpdater.LicenseInfo;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;

public class LicenseTile extends TextTile{
	
/////////////////////////////////////////////////////////////////////////////////////////
// SystemStatusHandler

public class LicenseTileHandler extends TileHandler {
	LicenseTile tile;

	public LicenseTileHandler(LicenseTile tile) {
		super(tile);
		this.tile = tile;
	}

	@Override
	public void handleMsg(Message msg) {

		if (msg.obj instanceof LicenseInfo) {
			this.tile.setData(msg.obj);
		}
		else if (msg.obj instanceof String) {
			this.tile.onUpdateError((String)msg.obj);
		} else {
			throw new RuntimeException("LicenseTileHandler: unknown object type");
		}
	}
}

// SystemStatusHandler
/////////////////////////////////////////////////////////////////////////////////////////

private Pairs data;
private LicenseTileHandler licenseTileHandler;
private LicenseTileUpdater licenseTileUpdater;


public LicenseTile(Context context, ApiClient client) {
	super(context, client);

	this.licenseTileHandler = new LicenseTileHandler(this);
    this.licenseTileUpdater = new LicenseTileUpdater(this.licenseTileHandler, client); // TODO: make it autolaunchable
    this.licenseTileUpdater.activate();
}

@Override
public Pairs getKeyValuePairs() { return this.data; }

public void onUpdateError(String error) {
	// TODO
}

@Override
public void setData(Object data) {
	if (!(data instanceof LicenseInfo)) {
		throw new InvalidParameterException("LicenseInfo expected");
	}
	
	LicenseInfo li = (LicenseInfo)data;
	this.data = new Pairs();
	this.data.put("License Number", li.licenseNumber);
	this.data.put("Company", li.company);
	this.data.put("Licensed users", li.licensedUsers);
	this.data.put("Product Expiration", li.productExpiration);
	this.data.put("Software Maintenance expiration", li.swmExpiration);
	this.data.put("Active users / devices", li.activeUsers+" / "+li.activeDevices);
	this.data.put("Antivirus", li.antivirus);
	this.data.put("Kerio Control Web Filter", li.webfilter);
	this.update();
}

@Override
public void activate() {
	 this.licenseTileUpdater.activate();		
}

@Override
public void deactivate() {
	 this.licenseTileUpdater.deactivate();		
}
}
