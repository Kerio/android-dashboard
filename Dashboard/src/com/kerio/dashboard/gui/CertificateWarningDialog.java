package com.kerio.dashboard.gui;
import java.security.KeyStoreException;

import android.util.Log;

import com.kerio.dashboard.R;
import com.kerio.dashboard.gui.tiles.ServerTile;


public class CertificateWarningDialog extends CertificateDialog {

	//TODO this is definitely spaghetti. Use observer or listener or somethinke like that.
	private ServerTile serverTile = null;
	
	protected void rejectCertificate() {
		this.getDialog().cancel();
	}
	
	protected void trustCertificate() {
    	if(this.certChain != null && this.certChain.length >= 1){
    		try {
				this.trustHelper.getKeystore().setCertificateEntry("Trust-" + this.certChain[0].hashCode(), this.certChain[0]);
			} catch (KeyStoreException e1) {
				Log.d("CertificateWarningDialog", "It wasn't possible to add certificate to the store.", e1);
				return;
			}
    		
    		if(this.serverTile != null){
    			this.serverTile.reload();
    		}
		}
	}

	protected int getTitleId() {
		return R.string.cert_Warning_header;
	}
	
	protected int getMessageId() {
		return R.string.cert_Warning_body;
	}
	
	public void setServerTile(ServerTile tile){
		this.serverTile = tile;
	}
}
