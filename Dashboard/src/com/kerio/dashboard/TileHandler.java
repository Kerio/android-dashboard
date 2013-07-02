package com.kerio.dashboard;

import com.kerio.dashboard.gui.tiles.Tile;

import android.os.Handler;
import android.os.Message;

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
