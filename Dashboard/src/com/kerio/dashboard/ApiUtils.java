package com.kerio.dashboard;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;

public class ApiUtils {
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void setUpActionBar(ActionBar actionBar) {
		// Make sure if the NavUtils are available (from Android-support api)
		boolean NavUtilsIsAvailable = true;
		try {
			Class.forName("android.support.v4.app.NavUtils");
		}
		catch (ClassNotFoundException e) {
			NavUtilsIsAvailable = false;
		}
		
	    // Make sure we're running on Honeycomb or higher to use ActionBar APIs
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && NavUtilsIsAvailable) {
	        actionBar.setDisplayHomeAsUpEnabled(true);
	    }
	}
}
