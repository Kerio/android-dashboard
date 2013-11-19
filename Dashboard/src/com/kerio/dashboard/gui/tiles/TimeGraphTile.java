package com.kerio.dashboard.gui.tiles;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

public abstract class TimeGraphTile extends GraphTile {

	private int timeInterval;
	private int timeRange;

	/** timeRange = graph capacity in seconds
	timeInterval = grid step in seconds */
	public TimeGraphTile(Context context, int capacity, int timeRange, int timeInterval) {
		super(context, capacity);
		this.timeInterval = timeInterval;
		this.timeRange = timeRange;
	}


	@SuppressLint("SimpleDateFormat")
	public void setCurrentTime(long timestamp)
	{
		int sectionsCount = this.timeRange / this.timeInterval;
		String[] labels = new String[sectionsCount + 1];
		labels[sectionsCount] = "Now";

		SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
		long t = timestamp;
		for (int i = sectionsCount - 1; i >= 0; --i) {
			t -= this.timeInterval;
			Date dt = new Date(t * 1000);
			labels[i] = sdf.format(dt);
		}
		
		this.setHorizontalLabels(labels);
	}
}
