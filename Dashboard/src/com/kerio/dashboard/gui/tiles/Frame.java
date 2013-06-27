package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.api.ApiClient;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Frame extends Tile {

	private Tile content;
	
	public Frame(Context context, String text, Tile content) {
		super(context, (ApiClient)null);
		this.content = content;
		
		LinearLayout holder = new LinearLayout(this.getContext());
		
		LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		holder.setLayoutParams(llParams);
		holder.setOrientation(LinearLayout.VERTICAL);
		//holder.setBackgroundColor(Color.WHITE);
		
		LinearLayout title = new LinearLayout(this.getContext());
		title.setBackgroundColor(Color.LTGRAY);
		title.setPadding(5, 1, 1, 1);
		holder.addView(title);
		TextView textView = new TextView(this.getContext());
		textView.setText(text);
		title.addView(textView);

		holder.addView(content);
		//this.setBackgroundColor(Color.LTGRAY);
		this.setPadding(1, 1, 1, 5);
		this.addView(holder);
	}

	@Override
	public void update() {
		content.update();
	}

	@Override
	public void setData(Object data) {
		content.setData(data);
		
	}
	

}
