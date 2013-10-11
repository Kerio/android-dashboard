package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.api.ApiClient;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Frame extends Tile {

	private Tile content;
	public static final int TITLE_GRAY = 0xFFD8D8D8;
	
	public Frame(Context context, String text, Tile content) {
		super(context, (ApiClient)null);
		this.content = content;
		
		LinearLayout holder = new LinearLayout(this.getContext());
		
		LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		holder.setLayoutParams(llParams);
		holder.setOrientation(LinearLayout.VERTICAL);
		
		
		LinearLayout title = new LinearLayout(this.getContext());
		LayoutParams titleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		titleParams.bottomMargin = 5;

		title.setBackgroundColor(Frame.TITLE_GRAY);
		title.setPadding(8, 4, 5, 5);
		
		TextView textView = new TextView(this.getContext());
		textView.setTextSize(14);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setText(text);
		title.addView(textView);
		
		holder.addView(title, titleParams);
		

		holder.addView(content);
		//this.setBackgroundColor(Color.LTGRAY);
		this.setPadding(1, 1, 1, 10);
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

	@Override
	public void activate() {
		content.activate();
	}

	@Override
	public void deactivate() {
		content.deactivate();
	}
	

}
