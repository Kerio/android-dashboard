package com.kerio.dashboard.config.gui;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;

import com.kerio.dashboard.R;
import com.kerio.dashboard.api.TrustStoreHelper;
import com.kerio.dashboard.gui.tiles.CertificateTile;

public class CertificateStoreActivity extends PreferenceActivity implements	OnPreferenceChangeListener, OnPreferenceClickListener {

	TrustStoreHelper trusthelper = null;
	private PreferenceCategory certificatesCategoryComponent;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.certificates);
		certificatesCategoryComponent = (PreferenceCategory) findPreference("group_prefCertificates");
		this.trusthelper = new TrustStoreHelper(this);	
		
		loadCertificates();
		
	}
	
	private void loadCertificates() {
		KeyStore store = this.trusthelper.getKeystore();
		Enumeration<String> aliases = null;
		
		try{	
			aliases = store.aliases();
		}catch(KeyStoreException kse){
			Log.d("CertificateStoreActivity", "Loading certificate IDs from store failed", kse);
			return;
		}
		
		this.certificatesCategoryComponent.removeAll();
		
		
		
		while(aliases.hasMoreElements()){
			try{
				String alias = aliases.nextElement();
				Certificate cert = store.getCertificate(alias);
				if(cert instanceof X509Certificate){
					CertificatePreference pref = new CertificatePreference(this);
					
					pref.certificate = (X509Certificate)cert;
					pref.certificateAlias = alias;
					
		
					String dn = pref.certificate.getSubjectDN().getName();
					Map<String, String> items = CertificateTile.parseDn(dn);				
					if( ! items.isEmpty() && items.containsKey("CN")){
						pref.setTitle(items.get("CN"));
					}else{
						pref.setTitle(dn);
					}
					
					String issuerDn = pref.certificate.getSubjectDN().getName();
					Map<String, String> issuerItems = CertificateTile.parseDn(issuerDn);				
					if( ! issuerItems.isEmpty() && issuerItems.containsKey("CN")){
						pref.setSummary("issued by:" + issuerItems.get("CN"));
					}else{
						pref.setSummary("issued by:" + issuerDn);
					}
					
					
					pref.setOnPreferenceClickListener(this);
					pref.setOnPreferenceChangeListener(this);
					
					this.certificatesCategoryComponent.addPreference(pref);
				}
			}catch(KeyStoreException kse){
				Log.d("CertificateStoreActivity", "Obtaining certificate for given alias failed", kse);
			}
		}
	}
	
	@Override
	public boolean onPreferenceChange(Preference orig, Object newValue) {
		if(orig instanceof CertificatePreference && newValue == null){
			loadCertificates();
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof CertificatePreference){
			CertificateConfigDialog dialog = new CertificateConfigDialog();
			dialog.setPref((CertificatePreference)preference);
			dialog.setCertChain(new X509Certificate[]{((CertificatePreference)preference).certificate});
			dialog.setTrustHelper(this.trusthelper);
			dialog.setCertificateAlias(((CertificatePreference)preference).certificateAlias);
			dialog.show(getFragmentManager(), "CertificateConfigDialog");
			return true;
		}
		return false;
	}

}
