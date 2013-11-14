package com.kerio.dashboard.gui.tiles;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;


import com.kerio.dashboard.NotificationUpdater;
import com.kerio.dashboard.R;
import com.kerio.dashboard.TileHandler;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationTile extends Tile {
	
	public class NotificationHandler extends TileHandler {
		private NotificationTile tile;
		
		public NotificationHandler(NotificationTile tile) {
			super(tile);
			this.tile = tile;
		}

		@Override
        public void handleMsg(Message msg) {
			if (msg.obj instanceof Map<?, ?>) {
				this.tile.setData(msg.obj);
			}
			else if (msg.obj instanceof String) {
				this.tile.onUpdateError((String)msg.obj);
			} else {
				throw new RuntimeException("NotificationHandler: unknown object type");
			}
		}
	}
	
	private NotificationHandler notificationHandler;
	private NotificationUpdater notificationUpdater;
	private Map<String, View> notificationsMap;

	public NotificationTile(Context context, ApiClient client) {
		super(context, client);
		
		this.notificationsMap = new HashMap<String, View>();
		this.setOrientation(LinearLayout.VERTICAL);
		
		this.notificationHandler = new NotificationHandler(this);
        this.notificationUpdater = new NotificationUpdater(this.notificationHandler, client); // TODO: make it autolaunchable
        this.notificationUpdater.activate();
	}

	@Override
	public void update() {
	}
	
	public void onUpdateError(String error)
	{
		// TODO
	}

	private View createNotificationView(Notification notification)
	{
		// Create vertical layout
		LinearLayout holder = new LinearLayout(this.getContext());
		{
			LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			holder.setLayoutParams(llParams);
			holder.setOrientation(LinearLayout.VERTICAL);
			holder.setPadding(3, 0, 5, 10);
		}

		// Create Relative layout
		LinearLayout rl = new LinearLayout(this.getContext());
		{
			LayoutParams rlParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			rl.setLayoutParams(rlParams);
			rl.setOrientation(LinearLayout.HORIZONTAL);
			
			// create icon
			ImageView icon = new ImageView(this.getContext());
			{
				RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
				
				icon.setLayoutParams(iconParams);
				switch (notification.type) {
				case Info:
					icon.setImageResource(R.drawable.notification_info);
					break;
				case Error:
					icon.setImageResource(R.drawable.notification_error);
					break;
				case Warning:
					icon.setImageResource(R.drawable.notification_warning);
					break;
				default:
					break;
				}
			}
			icon.setPadding(0, 2, 5, 0);
			rl.addView(icon);

			// create description
			TextView title = new TextView(this.getContext());
			{
				title.setTypeface(null, Typeface.BOLD);
				title.setText(notification.title);
				title.setTextSize(14);
			}
			rl.addView(title);
		}
		holder.addView(rl);
		
		if (0 != notification.description.length()) {
			TextView description = new TextView(this.getContext());
			{
				description.setTextSize(12);
				description.setText(notification.description);
				description.setPadding(22, 0, 0, 0);
			}
			holder.addView(description);
		}

		return holder;
	}
	
	public void removeNotificationView(View v)
	{
		this.removeView(v);
	}
	
	@Override
	public void setData(Object data) {
		if(data == null) {
			Log.d("NotificationTile.setData()", "Provided data is null.");
		}
		if (!(data instanceof Map<?, ?>)) {
			throw new InvalidParameterException("HashMap<String, Notification> expected");
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Notification> allNotifications = (Map<String, Notification>)data;
		
		Map<String, View> newNotifications = new HashMap<String, View>();
		
		for (String key : allNotifications.keySet()) {
			
			if (this.notificationsMap.containsKey(key)) {
				newNotifications.put(key, this.notificationsMap.get(key)); // keep existing notification
				this.notificationsMap.remove(key);
			}
			else {
				Notification notification = allNotifications.get(key);

				View v = this.createNotificationView(notification);
				if (v != null) {
					this.addView(v);
					newNotifications.put(key,  v);
				}
			}
		}

		for (View v : this.notificationsMap.values()) {
			this.removeView(v);
		}
		
		this.notificationsMap = newNotifications;
	}

	@Override
	public void activate() {
		 this.notificationUpdater.activate();		
	}

	@Override
	public void deactivate() {
		 this.notificationUpdater.deactivate();		
	}
}
