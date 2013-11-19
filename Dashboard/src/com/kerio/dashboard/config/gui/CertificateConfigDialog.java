package com.kerio.dashboard.config.gui;

import java.security.KeyStoreException;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.kerio.dashboard.R;
import com.kerio.dashboard.gui.CertificateDialog;

public class CertificateConfigDialog extends CertificateDialog {

	private String certificateAlias = null;
	private CertificatePreference pref = null;
	
	@Override
	protected void rejectCertificate() {
    	if(certificateAlias != null && certificateAlias.length() > 0 && this.certChain != null && this.certChain.length >= 1){
    		try {
				this.trustHelper.getKeystore().deleteEntry(certificateAlias);
				
				if(this.pref == null){
					return;
				}
				OnPreferenceChangeListener listener = this.pref.getOnPreferenceChangeListener();
				
				if(listener == null){
					return;
				}
				listener.onPreferenceChange(pref, null);
				
			} catch (KeyStoreException e1) {
				Log.d("CertificateConfigDialog", "Removing certificate from store failed",e1);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)//TODO COMPATIBILITY
	@Override
	protected void trustCertificate() {
		this.getDialog().cancel();
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}
	
	protected int getTitleId() {
		return R.string.cert_Config_header;
	}
	
	protected int getMessageId() {
		return R.string.cert_Config_body;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}

	public CertificatePreference getPref() {
		return pref;
	}

	public void setPref(CertificatePreference pref) {
		this.pref = pref;
	}

}
