package com.kerio.dashboard.gui.tiles;

import java.security.cert.X509Certificate;

import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerStatusUpdater;
import com.kerio.dashboard.ServerStatusUpdater.ConnectionState;
import com.kerio.dashboard.ServerStatusUpdater.ServerStatus;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import com.kerio.dashboard.api.NotificationGetter.NotificationType;
import com.kerio.dashboard.config.ServerConfig;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServerTile extends Tile {
	public static enum State {
		Ok, Error, Warning, CertWarning, Unknown
	}

	private LinearLayout holder;
	
	private TextView description;
	private TextView notes;
	private ImageView icon;
	private ProgressBar progress;
	private LinearLayout frame;
	private ImageView appIcon;
	private LinearLayout border;
	
	public State tileStatus;
	public X509Certificate certificate;
	private ServerStatus serverStatus;

	private ServerConfig.ServerType type = ServerConfig.ServerType.CONTROL;
	
	public ServerTile(Context context, ServerConfig server) {
		super(context, (ApiClient)null); // TODO: remove ApiClient

		initializeStructure();
		this.setConfig(server);
	}
	
	private void setConfig(ServerConfig config) {
		this.type = config.type;
		this.description.setText((config.description.length() > 0) ? config.description : config.server);
	}
		
	@Override
	public void setData(Object data) {
		
		if ( ! (data instanceof ServerStatusUpdater.ServerStatus)) {
			throw new RuntimeException("ServerTile: Unexpected data type");
		}
		
		ServerStatusUpdater.ServerStatus status = (ServerStatusUpdater.ServerStatus)data;
		this.setConfig(status.getConfig());
		
        switch(status.connected) {
	        case Error:
	        	displayNotes("Unable to connect to the server or authorization failed");
				setState(State.Error);
				return;
				
	        case CertificateError:
	        	displayNotes("Server certificate is invalid or untrusted");
				setState(State.CertWarning);
				return;
				
	        case ConnectivityError:
	        	displayNotes("Application error: " + status.getErrorCause().getMessage());
	        	setState(State.Error);
				break;
	        	
	        case Connected:
	        	setState(State.Ok);
				break;
			default:
				break;
        }
        
    	if ((status.notifications == null) || status.notifications.isEmpty()) {
    		this.notes.setText(R.string.note_ok);
    		return;
    	}

		if (status.notifications.size() > 1) {
			displayNotes("" + status.notifications.size() + " notifications pending");
			return;
		}
		
		for (NotificationType type : NotificationType.values()) {
			for (Notification n : status.notifications.values()) {
				if(n.type == type){
					displayNotes(n.title);
				}
			}	
		}
	}

	@Override
	public void update() {
	}

	private void displayNotes(String text)
	{
		this.notes.setText(text);
		this.notes.setVisibility(View.VISIBLE);
	}
	
	public void reload() {
		if(serverStatus != null){
			Thread updateThread = new Thread(serverStatus);
			updateThread.start();
		}
		setState(State.Unknown);
	}
	
	public void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}
	
	public void setState(State state) {
		this.tileStatus = state;
		switch (state) {
		case Ok:
			this.icon.setImageResource(android.R.drawable.presence_online);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			this.border.setBackgroundColor(0xFF97cc00);
			break;
		case Error:
			this.icon.setImageResource(android.R.drawable.presence_busy);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			this.border.setBackgroundColor(0xFFfa3431);
			break;
		case Warning:
		case CertWarning:
			this.icon.setImageResource(android.R.drawable.stat_sys_warning);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			this.border.setBackgroundColor(Color.LTGRAY);
			break;
		case Unknown:
		default:
			this.progress.setVisibility(View.VISIBLE);
			this.icon.setVisibility(View.GONE);
			this.border.setBackgroundColor(Color.LTGRAY);
			break;
		}
	}
	
	// TODO replace with XML
	private boolean initializeStructure()
	{
		border = new LinearLayout(this.getContext());
		border.setBackgroundColor(Color.LTGRAY);
		border.setPadding(3, 0, 0, 0);
		
		LayoutParams borderParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		borderParams.bottomMargin = 15;
		border.setLayoutParams(borderParams);
		
		// Create main frame
		frame = new LinearLayout(this.getContext());
		LayoutParams frameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		frameParams.height = 90;
		frame.setLayoutParams(frameParams);
		frame.setOrientation(LinearLayout.HORIZONTAL);
		
		appIcon = new ImageView(this.getContext());
		
		switch (this.type) {
			case CONNECT:
				appIcon.setImageResource(R.drawable.ico_connect);
				break;
			case OPERATOR:
				appIcon.setImageResource(R.drawable.ico_operator);
				break;
			default:
				appIcon.setImageResource(R.drawable.ico_control);
				break;
		}

		LayoutParams appIconParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		appIconParams.gravity = Gravity.CENTER_VERTICAL;
		appIcon.setLayoutParams(appIconParams);
		appIcon.setPadding(10, 0, 10, 0);
		frame.addView(appIcon);
		
		border.addView(frame);
		
		// Create vertical layout		
		holder = new LinearLayout(this.getContext());
		{	
			LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			llParams.gravity = Gravity.CENTER_VERTICAL;
			holder.setLayoutParams(llParams);
			holder.setOrientation(LinearLayout.VERTICAL);
			frame.setBackgroundColor(getResources().getColor(R.color.server_tile_background));
		}
		
		// Create Relative layout
		RelativeLayout rl = new RelativeLayout(this.getContext());
		{
			RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			rl.setLayoutParams(rlParams);
			
			// create description
			this.description = new TextView(this.getContext());
			{
				this.description.setTextAppearance(this.getContext(), android.R.style.TextAppearance_Large);
			}
			rl.addView(this.description);
			
			// create progress bar
			{
				this.progress = new ProgressBar(this.getContext());
				
				RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				progressParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				progressParams.addRule(RelativeLayout.CENTER_VERTICAL);
				progressParams.height = 20;
				progressParams.width = 22;
				progressParams.rightMargin = 3;
				
				this.progress.setLayoutParams(progressParams);
				//this.setState(State.Unknown); // Hide icon
			}			
			// create icon
			{
				this.icon = new ImageView(this.getContext());
				
				RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				iconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				iconParams.addRule(RelativeLayout.ALIGN_BASELINE);
				
				this.icon.setLayoutParams(iconParams);
				this.icon.setPadding(10, 10, 10, 10);
				this.setState(State.Unknown); // Hide icon
			}
			rl.addView(this.icon);
			rl.addView(this.progress);
		}
		holder.addView(rl);

		this.notes = new TextView(this.getContext());
		{
			this.notes.setTextAppearance(this.getContext(), android.R.style.TextAppearance_Small);
			this.notes.setText(R.string.note_connecting);
		}
		holder.addView(this.notes);

		frame.addView(holder);
		this.addView(border);
		return true;
	}
	
	
	@SuppressWarnings("deprecation")
	public void touchFeedback() {
		final AnimationDrawable drawable = new AnimationDrawable();
		final Handler handler = new Handler();
		int feedbackColor;
		
		
		if (ServerTile.State.Ok == this.tileStatus) {
			feedbackColor = getResources().getColor(R.color.server_tile_touchfeedback_ok);
		}
		else if (ServerTile.State.Error == this.tileStatus) {
			feedbackColor = getResources().getColor(R.color.server_tile_touchfeedback_error);
		}
		else {
			feedbackColor = getResources().getColor(R.color.server_tile_touchfeedback_loading);
		}
		
		drawable.addFrame(new ColorDrawable(feedbackColor), 200);
		drawable.addFrame(new ColorDrawable(getResources().getColor(R.color.server_tile_background)), 50);
		drawable.setOneShot(true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackgroudNew(drawable);
		} 
		else {
		    this.frame.setBackgroundDrawable(drawable);
		}

		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		        drawable.start();
		    }
		}, 100);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)//TODO COMPATIBILITY
	private void setBackgroudNew(AnimationDrawable drawable){
		this.frame.setBackground(drawable);
	}
	
	
	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}

}
;