package com.kerio.dashboard.config.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.kerio.dashboard.R;
import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.config.ServerConfig.ServerType;


public class ServerConfigDialog extends DialogPreference implements OnClickListener {
	
	private Spinner applicationType;
	private EditText firewallHostText;
	private EditText firewallUsernameText;
	private EditText firewallPasswordText;
	private EditText firewallDescriptionText;
	private Button deleteFirewallButton;
	
	private ServerConfig config = new ServerConfig();
	
    public ServerConfigDialog(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		 setDialogLayoutResource(R.layout.firewall_config);
	}

	public ServerConfigDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.firewall_config);
	}

	@Override
    protected View onCreateDialogView() { 
		 View root = super.onCreateDialogView();
		 
		 applicationType = (Spinner) root.findViewById(R.id.pref_ApplicationType);

		 ArrayAdapter<CharSequence> adapter 
		 	= ArrayAdapter.createFromResource(this.getContext(), R.array.pref_ServerType, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		applicationType.setAdapter(adapter);
			
		 firewallHostText = (EditText) root.findViewById(R.id.pref_FirewallHost);
		 firewallUsernameText = (EditText) root.findViewById(R.id.pref_FirewallUsername);
		 firewallPasswordText = (EditText) root.findViewById(R.id.pref_FirewallPassword);
		 firewallDescriptionText = (EditText) root.findViewById(R.id.pref_FirewallDescription);

		 if(this.config.id != 0){
			 deleteFirewallButton = (Button) root.findViewById(R.id.pref_DeleteFirewall_button);
			 deleteFirewallButton.setVisibility(Button.VISIBLE);
			 deleteFirewallButton.setOnClickListener(this);
		 }
		 
		 return root;
    }
	
	@Override
	protected void onBindDialogView(View view)
	{
		applicationType.setSelection( (config.type != null) ? config.type.ordinal() : 0);
		
		firewallHostText.setText(config.server);
		firewallUsernameText.setText(config.username);
		firewallPasswordText.setText(config.password);
		firewallDescriptionText.setText(config.description);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
			case DialogInterface.BUTTON_POSITIVE: // User clicked OK!
				this.config.type = getApplicationType();
				this.config.server = getHost();
				this.config.username = getUsername();
				this.config.password = getPassword();
				this.config.description = getDescription();
				callChangeListener(config);
			break;
		}
		super.onClick(dialog, which);
	}
	
	@Override
	public void onClick(View v) {
		callChangeListener(Integer.valueOf(this.config.id));
		super.onClick(this.getDialog(), DialogInterface.BUTTON_POSITIVE);
		this.getDialog().dismiss();
	}
	
	public void reset(){
		this.config = new ServerConfig();
	}
	
	public ServerType getApplicationType() {
		return ServerType.values()[applicationType.getSelectedItemPosition()];
	}

	public String getHost() {
		return firewallHostText.getText().toString();
	}

	public String getUsername() {
		return firewallUsernameText.getText().toString();
	}

	public String getPassword() {
		return firewallPasswordText.getText().toString();
	}
	
	public String getDescription() {
		return firewallDescriptionText.getText().toString();
	}
	
	public void setId(int id) {
		this.config.id = id;
	}
	
	public void setApplicationType(ServerType type) {
		this.config.type = type;
	}

	public void setHost(String host) {
		this.config.server = host;
	}

	public void setUsername(String username) {
		this.config.username = username;
	}

	public void setPassword(String password) {
		this.config.password = password;
	}
	
	public void setDescription(String description) {
		this.config.description = description;
	}

}
