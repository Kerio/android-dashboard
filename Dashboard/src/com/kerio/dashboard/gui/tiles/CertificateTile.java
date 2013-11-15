package com.kerio.dashboard.gui.tiles;

import java.security.cert.X509Certificate;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.kerio.dashboard.R;

public class CertificateTile extends TextTile {

	private Pairs pairs;
	
	public CertificateTile(Context context, X509Certificate cert) {
		super(context, null);
		Pairs pairs = new Pairs();

		try{
			pairs.put(this.getResources().getString(R.string.cert_dn), cert.getSubjectDN().getName());
			pairs.put(this.getResources().getString(R.string.cert_type), cert.getType());
			pairs.put(this.getResources().getString(R.string.cert_issuerDn), cert.getIssuerDN().toString());
			//TODO CIMA complete certificate display
		}catch(NotFoundException nfe){
			//TODO CIMA
		}
		
		this.pairs = pairs;
		super.update();
	}

	@Override
	public Pairs getKeyValuePairs() {
		return this.pairs;
	}

	@Override
	public void setData(Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

}
