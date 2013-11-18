package com.kerio.dashboard;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

public class ServerStatusUpdater extends PeriodicTask {

	private Config config;
	private KeyStore trustStore;
	
	public ServerStatusUpdater(Handler handler, SharedPreferences settings, KeyStore trustStore) {
		super(handler);
		this.config = new Config(settings);
		this.trustStore = trustStore;
	}
	
	@Override
	public void execute() {
    	List<ServerConfig> servers = this.config.get();
    	
    	if ( ! servers.isEmpty()) {
    		for (ServerConfig server : servers) {
    			ServerStatus status = new ServerStatus(server, this.getHandler(), this.trustStore);
    			Thread statusThread = new Thread(status);
    			statusThread.start();
        	}
    	}

    	this.notify(servers);
	}
	
	public enum ConnectionState {
		Connected, CertificateError, Error; 
	}
	
	// ServerStatus updates status of one server, and notifies handler about server's status
	public class ServerStatus implements Runnable {
		
		private Handler handler;
		
		private ServerConfig config;
		public ConnectionState connected = ConnectionState.Error;
		public Map<String, Notification> notifications = null;
		private KeyStore trustStore;
		
		public ServerStatus(ServerConfig config, Handler handler, KeyStore trustStore) {
			this.config = config;
			this.handler = handler;
			this.trustStore = trustStore;
		}
		
		@Override
		public void run() {
			ApiClient client = new ApiClient(this.config.server, this.config, this.trustStore);
			try{
				this.connected = client.login(this.config.username, this.config.password) ? ConnectionState.Connected : ConnectionState.Error;
			}catch(SSLHandshakeException se){
				this.connected = ConnectionState.CertificateError;
				if(se.getCause() instanceof CertificateException) {
					//TODO CIMA
				}
			}

	 		if (this.connected == ConnectionState.Connected) {
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
