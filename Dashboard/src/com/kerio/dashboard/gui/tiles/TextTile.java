package com.kerio.dashboard.gui.tiles;

import java.util.LinkedHashMap;

import com.kerio.dashboard.api.ApiClient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

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
			
			TableLayout.LayoutParams  rowParams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			rowParams.setMargins(0,1,0,1);
			row.setLayoutParams(rowParams);

			TextView keyView = renderKeyView(entry);
			row.addView(keyView);

			TextView valueView = renderValueView(entry);
			row.addView(valueView);
			
			Log.d("TextView", "Adding " + entry.getKey() + ": " + entry.getValue());
			this.table.addView(row);
		}
	}
	
	protected TextView renderKeyView(Pairs.Entry<String, String> entry) {
		TextView keyView = new TextView(this.getContext());
		keyView.setText(entry.getKey());
		keyView.setTypeface(null, Typeface.BOLD);
		keyView.setWidth(190);
		keyView.setPadding(10, 0, 0, 0);
		keyView.setTextSize(12);
		keyView.setVisibility(View.VISIBLE);
		
		return keyView;
	}
	
	protected TextView renderValueView(Pairs.Entry<String, String> entry) {
		TextView valueView = new TextView(this.getContext());
		valueView.setText(entry.getValue());
		valueView.setTextSize(12);
		valueView.setVisibility(View.VISIBLE);
		
		return valueView;
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
