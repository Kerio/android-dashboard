package com.kerio.dashboard.gui.tiles;

import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerStatusUpdater;
import com.kerio.dashboard.api.ApiClient;
import com.kerio.dashboard.api.NotificationGetter.Notification;
import com.kerio.dashboard.config.ServerConfig;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServerTile extends Tile {

	private LinearLayout holder;
	
	private TextView description;
	private TextView notes;
	private ImageView icon;
	private ProgressBar progress;
	private LinearLayout frame;
	private ImageView appIcon;
	private LinearLayout border;
	
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
		
        if ( ! status.connected) {
        	displayNotes("Unable to connect to the server or authorization failed");
			setState(State.Error);
			return;
        }
    	setState(State.Ok);
        
    	if ((status.notifications == null) || status.notifications.isEmpty()) {
    		this.notes.setText(R.string.note_ok);
    		return;
    	}

		if (status.notifications.size() > 1) {
			displayNotes("" + status.notifications.size() + " notifications pending");
			return;
		}
		
		for (Notification n : status.notifications.values()) {
			displayNotes(n.title);
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
	
	private enum State {
		Ok, Error, Warning, Unknown
	}
	
	private void setState(State state) {
		switch (state) {
		case Ok:
			this.icon.setImageResource(android.R.drawable.presence_online);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			break;
		case Error:
			this.icon.setImageResource(android.R.drawable.presence_busy);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			break;
		case Warning:
			this.icon.setImageResource(android.R.drawable.stat_sys_warning);
			this.icon.setVisibility(View.VISIBLE);
			this.progress.setVisibility(View.GONE);
			break;
		case Unknown:
		default:
			this.progress.setVisibility(View.VISIBLE);
			this.icon.setVisibility(View.GONE);
			break;
		}
	}
	
	// TODO replace with XML
	private boolean initializeStructure()
	{
		border = new LinearLayout(this.getContext());
		border.setBackgroundColor(Color.LTGRAY);
		border.setPadding(0, 0, 0, 1);
		
		// Create main frame
		frame = new LinearLayout(this.getContext());
		LayoutParams frameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		frameParams.height = 120;
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
		
		appIcon.setPadding(0, 1, 5, 1);
		frame.addView(appIcon);
		
		border.addView(frame);
		
		// Create vertical layout		
		holder = new LinearLayout(this.getContext());
		{
//			holder.setBackgroundColor(Color.);
			
			LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			holder.setLayoutParams(llParams);
			holder.setOrientation(LinearLayout.VERTICAL);
			frame.setBackgroundColor(Color.WHITE);
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
				progressParams.height = 22;
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
				iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
				
				this.icon.setLayoutParams(iconParams);
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

}
