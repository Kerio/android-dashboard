package com.kerio.dashboard.config.gui;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;

import com.kerio.dashboard.R;
import com.kerio.dashboard.api.TrustStoreHelper;

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
					
					pref.setTitle(pref.certificate.getSubjectDN().getName()); //TODO MHAJEK finish texts
					pref.setSummary("issued by:" + pref.certificate.getIssuerDN().toString()); //TODO MHAJEK finish texts
					
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)//TODO COMPATIBILITY
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
