package com.kerio.dashboard.gui;
import java.security.KeyStoreException;

import android.annotation.TargetApi;
import android.os.Build;

import com.kerio.dashboard.R;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CertificateWarningDialog extends CertificateDialog {

	protected void rejectCertificate() {
		this.getDialog().cancel();
	}
	
	protected void trustCertificate() {
    	if(this.certChain != null && this.certChain.length >= 1){
    		try {
				this.trustHelper.getKeystore().setCertificateEntry("Trust-" + this.certChain[0].hashCode(), this.certChain[0]);
			} catch (KeyStoreException e1) {
				// TODO CIMA log at least
			}
		}
	}

	protected int getTitleId() {
		return R.string.cert_Warning_header;
	}
	
	protected int getMessageId() {
		return R.string.cert_Warning_body;
	}
}
