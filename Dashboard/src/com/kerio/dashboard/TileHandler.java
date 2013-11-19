package com.kerio.dashboard;

import android.os.Handler;
import android.os.Message;

import com.kerio.dashboard.gui.tiles.Tile;

public abstract class TileHandler extends Handler {
	private Tile tile;
	
	public TileHandler(Tile tile) {
		this.tile = tile;
	}
	
	@Override
    public void handleMessage(Message msg) {
		this.tile.dataReady();
		handleMsg(msg);
	}
	
	public abstract void handleMsg(Message msg);
}
