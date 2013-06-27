package com.kerio.dashboard.gui.tiles;

import java.util.LinkedHashMap;

import com.kerio.dashboard.api.ApiClient;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class TextTile extends Tile {

	public TextTile(Context context, ApiClient client) {
		super(context, client);
		// TODO Auto-generated constructor stub
		this.initialize();
	}
	
	@SuppressWarnings("serial")
	public class Pairs extends LinkedHashMap<String, String> {};
	
	public abstract Pairs getKeyValuePairs();

	@Override
	public void update() {
		Log.d("TextTile", "in update() ...");
		
		Pairs pairs = getKeyValuePairs();

		this.table.removeAllViews();
		
		for (Pairs.Entry<String, String> entry : pairs.entrySet()) {
			TableRow row = new TableRow(this.getContext());
			row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			TextView keyView = new TextView(this.getContext());
			TextView valueView = new TextView(this.getContext());
			
			keyView.setText(entry.getKey());
			keyView.setTypeface(null, Typeface.BOLD);
			keyView.setPadding(0, 0, 20, 0);
			valueView.setText(entry.getValue());
			row.addView(keyView);
			row.addView(valueView);
			
			keyView.setVisibility(View.VISIBLE);
			keyView.setTextSize(12);
			valueView.setVisibility(View.VISIBLE);
			valueView.setTextSize(12);
			
			Log.d("TextView", "Adding " + entry.getKey() + ": " + entry.getValue());
			this.table.addView(row);
		}
	}
	
	private void initialize() {
		Log.d("TextTile", "in initialize() ...");
		
		this.table = new TableLayout(this.getContext());
		this.table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.table.setVisibility(View.VISIBLE);
		this.addView(table);
	}
	
	TableLayout table;
	
}
