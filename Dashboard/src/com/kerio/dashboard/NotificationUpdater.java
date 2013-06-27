package com.kerio.dashboard;

import java.util.HashMap;

import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter;
import com.kerio.dashboard.api.NotificationGetter.Notification;

import android.os.Handler;

public class NotificationUpdater extends PeriodicTask {

	private NotificationGetter getter;
	public NotificationUpdater(Handler handler, ApiClient client) {
		super(handler);
		this.getter = new NotificationGetter(client);
	}

	@Override
	public void execute() {
		HashMap<String, Notification> newNotifications = this.getter.getAllNotifications();
		if (newNotifications == null) {
			newNotifications = new HashMap<String, Notification>();
		}
		
		this.notify(newNotifications);
	}

}
