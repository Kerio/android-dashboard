package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.widget.TextView;

import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.TopActiveHostsTileUpdater;
import com.kerio.dashboard.TopActiveHostsTileUpdater.TopActiveHosts;
import com.kerio.dashboard.api.ApiClient;

public class TopActiveHostsTile extends TextTile{
	private static String DOWNLOAD_PREFIX = "DOWNLOAD";
	private static String UPLOAD_PREFIX = "UPLOAD";
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
	private final TopActiveHostsTileHandler topActiveHostsTileHandler;
	private final TopActiveHostsTileUpdater topActiveHostsTileUpdater;

	public TopActiveHostsTile(Context context, ApiClient client) {
		super(context, client);

		this.topActiveHostsTileHandler = new TopActiveHostsTileHandler(this);
		this.topActiveHostsTileUpdater = new TopActiveHostsTileUpdater(this.topActiveHostsTileHandler, client); // TODO: make it autolaunchable
		this.topActiveHostsTileUpdater.activate();
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

		String hostName;
		TopActiveHosts tah = (TopActiveHosts)data;
		this.data = new Pairs();

		this.data.put(TopActiveHostsTile.DOWNLOAD_PREFIX + "Download", "Current Rx");
		if(tah.download == null || 0 == tah.download.length){
			this.data.put("No hosts...", "");
		}else{
			for (String[] download : tah.download) {
				hostName = download[0];
				hostName += 0 != download[1].length() ? " - " + download[1] : "";
				this.data.put(TopActiveHostsTile.DOWNLOAD_PREFIX + hostName, download[2] + " KB/s");
			}
		}

		this.data.put(TopActiveHostsTile.UPLOAD_PREFIX + "Upload", "Current Tx");
		if (tah.upload == null){
			this.data.put("No hosts...", "");
		}
		else {
			for (String[] upload : tah.upload) {
				hostName = upload[0];
				hostName += 0 != upload[1].length() ? " - " + upload[1] : "";
				this.data.put(TopActiveHostsTile.UPLOAD_PREFIX + hostName, upload[2] + " KB/s");
			}
		}
		this.update();
	}

	@Override
	protected TextView renderKeyView(Pairs.Entry<String, String> entry) {
		TextView keyView = super.renderKeyView(entry);
		String key = entry.getKey();

		if (0 == key.indexOf(TopActiveHostsTile.DOWNLOAD_PREFIX)) {
			key = key.substring(TopActiveHostsTile.DOWNLOAD_PREFIX.length());
		}
		else if (0 == key.indexOf(TopActiveHostsTile.UPLOAD_PREFIX)) {
			key = key.substring(TopActiveHostsTile.UPLOAD_PREFIX.length());
		}
		keyView.setText(key);

		if(key.equalsIgnoreCase("download") || key.equalsIgnoreCase("upload")){
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

		if(entry.getKey().equalsIgnoreCase(TopActiveHostsTile.DOWNLOAD_PREFIX + "download") || entry.getKey().equalsIgnoreCase(TopActiveHostsTile.UPLOAD_PREFIX + "upload")){
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
