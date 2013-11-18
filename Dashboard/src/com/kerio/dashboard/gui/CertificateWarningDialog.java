package com.kerio.dashboard.gui;
import java.security.KeyStoreException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;

import com.kerio.dashboard.R;
import com.kerio.dashboard.ServerStatusUpdater.ServerStatus;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CertificateWarningDialog extends CertificateDialog {

	private ServerStatus serverStatus = null;
	
	protected void rejectCertificate() {
		this.getDialog().cancel();
	}
	
	protected void trustCertificate() {
    	if(this.certChain != null && this.certChain.length >= 1){
    		try {
				this.trustHelper.getKeystore().setCertificateEntry("Trust-" + this.certChain[0].hashCode(), this.certChain[0]);
			} catch (KeyStoreException e1) {
				return;
				// TODO CIMA log at least
			}
    		
    		if(this.serverStatus != null){
    			Thread updateThread = new Thread(this.serverStatus);
    			updateThread.start();
    		}
		}
	}

	protected int getTitleId() {
		return R.string.cert_Warning_header;
	}
	
	protected int getMessageId() {
		return R.string.cert_Warning_body;
	}
	
	public void setServerStatus(ServerStatus status){
		this.serverStatus = status;
	}
}
