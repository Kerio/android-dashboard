package com.kerio.dashboard.config.gui;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.kerio.dashboard.R;
import com.kerio.dashboard.config.Config;
import com.kerio.dashboard.config.ServerConfig;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;

public class SettingActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	
	private ServerConfigDialog addFirewallDialog;
	private PreferenceCategory firewallsCategoryComponent;
	private Config configAll;
	
	@SuppressWarnings("deprecation") //for android 2.3
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
        addFirewallDialog = (ServerConfigDialog) findPreference("button_prefAddFirewall");
        addFirewallDialog.setOnPreferenceChangeListener(this);
   
        SharedPreferences settings = this.getSharedPreferences(Config.SERVER_CONFIG, Activity.MODE_PRIVATE);
        configAll = new Config(settings);
        
        
        firewallsCategoryComponent = (PreferenceCategory) findPreference("group_prefFirewallList");
        updateFirewalls();
 
    }
	
	private void updateFirewalls() {
        firewallsCategoryComponent.removeAll();
        
        List<ServerConfig> servers = configAll.get();
        for (ServerConfig serverConfig : servers) {
        	
        	ServerConfigDialog serverPreference = new ServerConfigDialog(firewallsCategoryComponent.getContext(), null);
        	
            serverPreference.setKey("server:" + serverConfig.server + serverConfig.username);
            serverPreference.setTitle(serverConfig.server);
            serverPreference.setSummary(serverConfig.description);
            
            serverPreference.setId(serverConfig.id);
            
            serverPreference.setApplicationType(serverConfig.type);
            serverPreference.setHost(serverConfig.server);

            serverPreference.setUsername(serverConfig.username);
            serverPreference.setPassword(serverConfig.password);
            serverPreference.setDescription(serverConfig.description);
            
            serverPreference.setOnPreferenceChangeListener(this);
            
            firewallsCategoryComponent.addPreference(serverPreference);
		} 		
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		if(preference instanceof ServerConfigDialog){
			List<ServerConfig> servers = configAll.get();
			
			// -------- DELETE -------------
			if(newValue instanceof Integer){
				Integer idToDelete = (Integer)newValue;
				ServerConfig found = null;
				for (ServerConfig serverConfig : servers) {
					if(idToDelete.intValue() == serverConfig.id){
						found = serverConfig;
						break;
					}
				}
				
				servers.remove(found);
			}
			
			// -------- EDIT & ADD -------------
			if(newValue instanceof ServerConfig){
				ServerConfig newConfig = (ServerConfig)newValue;
				
				if(preference == this.addFirewallDialog){
					ServerConfig freshest = null;
					try{
						freshest = Collections.max(servers);
					}catch(NoSuchElementException nse){
						freshest = null; //don't know what else to do here.
					}
					newConfig.id = (freshest == null ? 1 : freshest.id + 1); //Zero must be reserved for unset
					servers.add(newConfig);
					this.addFirewallDialog.reset();
				}else{
					for (ServerConfig serverConfig : servers) {
						if(newConfig.id == serverConfig.id){
							serverConfig.assign(newConfig);
							break;
						}
					}
				}
			}
			
			
			boolean retval = configAll.set(servers);
			updateFirewalls();
			return retval;
		}
		
		return false;
	}
    
}
