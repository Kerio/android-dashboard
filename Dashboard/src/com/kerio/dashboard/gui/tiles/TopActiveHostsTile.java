package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.os.Message;

import com.kerio.dashboard.ConnectivityTileUpdater.Connectivity;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.TopActiveHostsTileUpdater;
import com.kerio.dashboard.TopActiveHostsTileUpdater.TopActiveHosts;
import com.kerio.dashboard.api.ApiClient;

public class TopActiveHostsTile extends TextTile{
/////////////////////////////////////////////////////////////////////////////////////////
//SystemStatusHandler
public class TopActiveHostsTileHandler extends TileHandler {
	TopActiveHostsTile tile;

	public TopActiveHostsTileHandler(TopActiveHostsTile tile) {
		super(tile);
		this.tile = tile;
	}

	@Override
	public void handleMsg(Message msg) {

		if (msg.obj instanceof TopActiveHosts) {
			this.tile.setData(msg.obj);
		}
		else if (msg.obj instanceof String) {
			this.tile.onUpdateError((String)msg.obj);
		} else {
			throw new RuntimeException("TopActiveHostsTileHandler: unknown object type");
		}
	}
}
//SystemStatusHandler
/////////////////////////////////////////////////////////////////////////////////////////

	private Pairs data;
	private TopActiveHostsTileHandler topActiveHostsTileHandler;
	private TopActiveHostsTileUpdater topActiveHostsTileUpdater;

	public TopActiveHostsTile(Context context, ApiClient client) {
		super(context, client);

		this.topActiveHostsTileHandler = new TopActiveHostsTileHandler(this);
		this.topActiveHostsTileUpdater = new TopActiveHostsTileUpdater(this.topActiveHostsTileHandler, client); // TODO: make it autolaunchable
		this.topActiveHostsTileUpdater.activate();
	//this.systemStatusHandler.post(this.systemStatusUpdater); 
	}
	
	@Override
	public Pairs getKeyValuePairs() { return this.data; }

	public void onUpdateError(String error) {
		// TODO
	}

	@Override
	public void setData(Object data) {
		if (!(data instanceof TopActiveHosts)) {
			throw new InvalidParameterException("TopActiveHosts expected");
		}

		TopActiveHosts tah = (TopActiveHosts)data;
		this.data = new Pairs();
		
		this.data.put("Download", " ");
		if(tah.download == null){
			this.data.put("No hosts...", "");
		}else{
			for(int i=0;i<tah.download.length;i++){
				this.data.put(tah.download[i][0]+" - "+tah.download[i][1],tah.download[i][2]+" KB/s");
			}
		}
		
		this.data.put("Upload", " ");
		if(tah.upload == null){
			this.data.put("No hosts...", "");
		}else{
			for(int i=0;i<tah.upload.length;i++){
				this.data.put(tah.upload[i][0]+" - "+tah.upload[i][1]+" ", tah.upload[i][2]+" KB/s");
			}
		}
		this.update();
	}
	
	@Override
	public void activate() {
		this.topActiveHostsTileUpdater.activate();		
	}

	@Override
	public void deactivate() {
		this.topActiveHostsTileUpdater.deactivate();		
	}

}
