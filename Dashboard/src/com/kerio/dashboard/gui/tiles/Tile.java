package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.api.ApiClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class Tile extends LinearLayout{

	protected ApiClient client;
	private boolean ready = false;
	private Handler finalHandler = null;
	
	@SuppressLint("NewApi")
	public Tile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Tile(Context context, ApiClient client) {
		super(context);
		this.client = client;
	}
	
	public void dataReady() {
		this.ready = true;
		if (this.finalHandler != null) {
			Message msg = new Message();
			msg.obj = "tileUpdated";
			this.finalHandler.sendMessage(msg);
		}
	}
	
	public boolean isReady() {
		return this.finalHandler == null || this.ready;
	}
	
	public void setFinalHandler(Handler handler) {
		this.finalHandler = handler;
	}

	public abstract void update();
	public abstract void setData(Object data);
	public abstract void activate();
	public abstract void deactivate();
	
}
