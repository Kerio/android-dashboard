package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.kerio.dashboard.ConnectivityTileUpdater;
import com.kerio.dashboard.ConnectivityTileUpdater.Connectivity;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.gui.tiles.TextTile.Pairs;

public class ConnectivityTile extends TextTile{
/////////////////////////////////////////////////////////////////////////////////////////
//SystemStatusHandler
	public class ConnectivityTileHandler extends TileHandler {
		ConnectivityTile tile;

		public ConnectivityTileHandler(ConnectivityTile tile) {
			super(tile);
			this.tile = tile;
		}

		@Override
		public void handleMsg(Message msg) {

			if (msg.obj instanceof Connectivity) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("ConnectivityTileHandler: unknown object type");
			}
		}
	}
//SystemStatusHandler
/////////////////////////////////////////////////////////////////////////////////////////

	private Pairs data;
	private ConnectivityTileHandler connectivityTileHandler;
	private ConnectivityTileUpdater connectivityTileUpdater;

	public ConnectivityTile(Context context, ApiClient client) {
		super(context, client);

		this.connectivityTileHandler = new ConnectivityTileHandler(this);
		this.connectivityTileUpdater = new ConnectivityTileUpdater(this.connectivityTileHandler, client); // TODO: make it autolaunchable
		this.connectivityTileUpdater.activate();
		//this.systemStatusHandler.post(this.systemStatusUpdater); 
	}

	@Override
	public Pairs getKeyValuePairs() { return this.data; }

	public void onUpdateError(String error) {
		// TODO
	}

	@Override
	public void setData(Object data) {
		if (!(data instanceof Connectivity)) {
			throw new InvalidParameterException("Connectivity expected");
		}

		Connectivity con = (Connectivity)data;
		this.data = new Pairs();
		this.data.put("Name", "Current Rx/Tx");
		if(con.interfaces != null){
			for(int i=0;i<con.interfaces.length;i++){
				if(con.interfaces[i][1].equalsIgnoreCase("up")) {
					this.data.put(con.interfaces[i][0], con.interfaces[i][2]);
				}
				else {
					this.data.put(con.interfaces[i][0], con.interfaces[i][1]);
				}
			}
		}else{
			this.data.put("No Internet connection found", " ");
		}
		this.update();
	}
	
	@Override
	protected TextView renderKeyView(Pairs.Entry<String, String> entry) {
		TextView keyView = super.renderKeyView(entry);

		if(entry.getKey().equalsIgnoreCase("Name")){
			keyView.setTypeface(null, Typeface.BOLD);
		}
		else {
			if (entry.getValue().equalsIgnoreCase("down")) {
				keyView.setTextColor(Color.LTGRAY);
			}
			keyView.setTypeface(null, Typeface.NORMAL);
		}
		
		return keyView;
	}
	
	@Override
	protected TextView renderValueView(Pairs.Entry<String, String> entry) {
		TextView valueView = super.renderValueView(entry);
		
		if(entry.getKey().equalsIgnoreCase("Name")){
			valueView.setTypeface(null, Typeface.BOLD);
		}
		else if (entry.getValue().equalsIgnoreCase("down")) {
			valueView.setTextColor(Color.LTGRAY);
		}
		
		return valueView;
	}

	@Override
	public void activate() {
		this.connectivityTileUpdater.activate();		
	}

	@Override
	public void deactivate() {
		this.connectivityTileUpdater.deactivate();		
	}
}
