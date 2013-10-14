package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kerio.dashboard.ConnectivityTileUpdater.Connectivity;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.TopActiveHostsTileUpdater;
import com.kerio.dashboard.TopActiveHostsTileUpdater.TopActiveHosts;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.gui.tiles.TextTile.Pairs;

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
		
		String hostName = "";
		TopActiveHosts tah = (TopActiveHosts)data;
		this.data = new Pairs();
		
		this.data.put("Download", "Current Rx");
		if(tah.download == null || 0 == tah.download.length){
			this.data.put("No hosts...", "");
		}else{
			for(int i=0;i<tah.download.length;i++){
				hostName = tah.download[i][0];
				hostName += 0 != tah.download[i][1].length() ? " - " + tah.download[i][1] : "";
				this.data.put(hostName,tah.download[i][2]+" KB/s");
			}
		}
		
		this.data.put("Upload", "Current Tx");
		if (tah.upload == null){
			this.data.put("No hosts...", "");
		}
		else {
			for (int i = 0; i < tah.upload.length; i++) {
				hostName = tah.upload[i][0];
				hostName += 0 != tah.upload[i][1].length() ? " - " + tah.upload[i][1] : "";
				this.data.put(hostName, tah.upload[i][2] + " KB/s");
			}
		}
		this.update();
	}
	
	@Override
	protected TextView renderKeyView(Pairs.Entry<String, String> entry) {
		TextView keyView = super.renderKeyView(entry);

		if(entry.getKey().equalsIgnoreCase("download") || entry.getKey().equalsIgnoreCase("upload")){
			keyView.setTypeface(null, Typeface.BOLD);
		}
		else {
			keyView.setTypeface(null, Typeface.NORMAL);
		}
		
		return keyView;
	}
	
	@Override
	protected TextView renderValueView(Pairs.Entry<String, String> entry) {
		TextView valueView = super.renderValueView(entry);
		
		if(entry.getKey().equalsIgnoreCase("download") || entry.getKey().equalsIgnoreCase("upload")){
			valueView.setTypeface(null, Typeface.BOLD);
		}
		
		return valueView;
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
