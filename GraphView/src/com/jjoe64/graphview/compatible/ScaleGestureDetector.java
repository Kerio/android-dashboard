package com.jjoe64.graphview.compatible;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
public class ScaleGestureDetector {
	public interface SimpleOnScaleGestureListener {
		boolean onScale(ScaleGestureDetector detector);
	}

	private Object realScaleGestureDetector;
	private Method method_getScaleFactor;
	private Method method_isInProgress;
	private Method method_onTouchEvent;

	/**
	 * @param context
	 * @param simpleOnScaleGestureListener
	 */
	public ScaleGestureDetector(Context context, SimpleOnScaleGestureListener simpleOnScaleGestureListener) {
		try {
			// check if class is available
			Class.forName("android.view.ScaleGestureDetector");

			// load class and methods
			Class<?> classRealScaleGestureDetector = Class.forName("com.jjoe64.graphview.compatible.RealScaleGestureDetector");
			method_getScaleFactor = classRealScaleGestureDetector.getMethod("getScaleFactor");
			method_isInProgress = classRealScaleGestureDetector.getMethod("isInProgress");
			method_onTouchEvent = classRealScaleGestureDetector.getMethod("onTouchEvent", MotionEvent.class);

			// create real ScaleGestureDetector
			Constructor<?> constructor = classRealScaleGestureDetector.getConstructor(Context.class, getClass(), SimpleOnScaleGestureListener.class);
			realScaleGestureDetector = constructor.newInstance(context, this, simpleOnScaleGestureListener);
		} catch (Exception e) {
			// not available
			Log.w("com.jjoe64.graphview", "*** WARNING *** No scaling available for graphs. Exception:");
			e.printStackTrace();
		}
	}

	public double getScaleFactor() {
		if (method_getScaleFactor != null) {
			try {
				return (Float) method_getScaleFactor.invoke(realScaleGestureDetector);
			} catch (Exception e) {
				e.printStackTrace();
				return 1.0;
			}
		}
		return 1.0;
	}

	public boolean isInProgress() {
		if (method_getScaleFactor != null) {
			try {
				return (Boolean) method_isInProgress.invoke(realScaleGestureDetector);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public void onTouchEvent(MotionEvent event) {
		if (method_onTouchEvent != null) {
			try {
				method_onTouchEvent.invoke(realScaleGestureDetector, event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
