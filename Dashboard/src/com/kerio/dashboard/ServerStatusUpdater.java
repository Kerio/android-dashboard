package com.kerio.dashboard;

import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.ApiClient.ApiClientException;
import com.kerio.dashboard.api.NotificationGetter;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;

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
		Connected, CertificateError, ConnectivityError, Error; 
	}
	
	// ServerStatus updates status of one server, and notifies handler about server's status
	public class ServerStatus implements Runnable {
		
		private Handler handler;
		
		public ConnectionState connected = ConnectionState.Error;
		public Map<String, Notification> notifications = null;
		
		private ServerConfig config;
		private KeyStore trustStore;
		private Throwable errorCause;
		
		public ServerStatus(ServerConfig config, Handler handler, KeyStore trustStore) {
			this.config = config;
			this.handler = handler;
			this.trustStore = trustStore;
		}
		
		@Override
		public void run() {
			ApiClient client = null;
			
			try{
				client = new ApiClient(this.config, this.trustStore);
				try{
					this.connected = client.login(this.config.username, this.config.password) ? ConnectionState.Connected : ConnectionState.Error;
				}catch(SSLHandshakeException se){
					this.connected = ConnectionState.CertificateError;
					this.errorCause = se;
				}	
			}catch (ApiClientException ace) {
				this.connected = ConnectionState.ConnectivityError;
				this.errorCause = ace;
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

		public Throwable getErrorCause() {
			return errorCause;
		}
	}
	
}
