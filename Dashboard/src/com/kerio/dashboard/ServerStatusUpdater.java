package com.kerio.dashboard;

import java.util.List;
import java.util.Map;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

public class ServerStatusUpdater extends PeriodicTask {

	private Config config;
	
	public ServerStatusUpdater(Handler handler, SharedPreferences settings) {
		super(handler);
		this.config = new Config(settings);
	}
	
	@Override
	public void execute() {
    	List<ServerConfig> servers = this.config.get();
    	
    	if ( ! servers.isEmpty()) {
    		for (ServerConfig server : servers) {
    			ServerStatus status = new ServerStatus(server, this.getHandler());
    			Thread statusThread = new Thread(status);
    			statusThread.start();
        	}
    	}

    	this.notify(servers);
	}
	
	// ServerStatus updates status of one server, and notifies handler about server's status
	public class ServerStatus implements Runnable {
		
		private Handler handler;
		
		private ServerConfig config;
		public boolean connected = false;
		public Map<String, Notification> notifications = null;
		
		ServerStatus(ServerConfig config, Handler handler) {
			this.config = config;
			this.handler = handler;
		}
		
		@Override
		public void run() {
			ApiClient client = new ApiClient(this.config.server);
			this.connected = client.login(this.config.username, this.config.password);
	 		if (this.connected) {
	 			NotificationGetter notificationGetter = new NotificationGetter(client);
				this.notifications = notificationGetter.getAllNotifications();
			}

			Message msg = new Message();
			msg.obj = this;
		 	this.handler.sendMessage(msg);    	
	 	}	
		
		public ServerConfig getConfig() {
			return config;
		}
	}
	
}
