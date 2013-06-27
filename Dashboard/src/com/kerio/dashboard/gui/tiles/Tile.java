package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.api.ApiClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class Tile extends LinearLayout {

	protected ApiClient client;
	
	@SuppressLint("NewApi")
	public Tile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	public Tile(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public Tile(Context context) {
		super(context);
	}


	//TODO smazat
	public Tile(Context context, ApiClient client) {
		super(context);
		this.client = client;
	}
	

	public abstract void update();
	public abstract void setData(Object data);
	
}
