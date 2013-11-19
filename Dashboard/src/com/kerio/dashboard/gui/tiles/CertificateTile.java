package com.kerio.dashboard.gui.tiles;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.kerio.dashboard.R;

public class CertificateTile extends TextTile {

	private Pairs pairs;
	
	public CertificateTile(Context context, X509Certificate cert) {
		super(context, null);
		Pairs pairs = new Pairs();

		try{
			
			String dn = cert.getSubjectDN().getName();
			putDn(pairs, "", dn);
			
			
			pairs.put(this.getResources().getString(R.string.cert_type), cert.getType());
			pairs.put(this.getResources().getString(R.string.cert_notAfter), cert.getNotAfter().toString());
			pairs.put(this.getResources().getString(R.string.cert_notBefore), cert.getNotBefore().toString());
			pairs.put(this.getResources().getString(R.string.cert_serial), cert.getSerialNumber().toString());
			pairs.put(this.getResources().getString(R.string.cert_version), Integer.toString(cert.getVersion()));
			pairs.put(this.getResources().getString(R.string.cert_sigAlg), cert.getSigAlgName());
			pairs.put(this.getResources().getString(R.string.cert_sigOid), cert.getSigAlgOID());
			
			
			String issuerDn = cert.getIssuerDN().getName();
			putDn(pairs, this.getResources().getString(R.string.cert_issuer) + " ", issuerDn);
			
		}catch(NotFoundException nfe){
			//Should never happen since we use generated IDs not blind search.
			Log.d("CertificateTile", "Some pregenerated text wasnt found", nfe);
		}
		
		this.pairs = pairs;
		super.update();
	}

	private void putDn(Pairs pairs, String prefix, String dn){	
		Map<String, String> items = CertificateTile.parseDn(dn);
		if( ! items.isEmpty()){
			for(Entry<String, String> entry : items.entrySet()) {
			    pairs.put(prefix + entry.getKey(), entry.getValue());
			}
		}else{
			pairs.put(prefix + this.getResources().getString(R.string.cert_dn), dn);
		}
	}
	
	public static Map<String,String> parseDn(String dn){
		Map<String, String> dnMap = new TreeMap<String, String>();
		
		Pattern dnPattern = Pattern.compile("(?:(?:([A-Z]+)\\s*=\\s*([^,]+)\\s*,?\\s*))");
		Matcher crossMatcher = dnPattern.matcher(dn);
		while(crossMatcher.find()){
			dnMap.put(crossMatcher.group(1), crossMatcher.group(2));
		}
		
		return dnMap;
	}
	
	@Override
	public Pairs getKeyValuePairs() {
		return this.pairs;
	}

	@Override
	public void setData(Object data) {
		// Not a tile, just using its layout posibilities
	}

	@Override
	public void activate() {
		// Not a tile, just using its layout posibilities
	}

	@Override
	public void deactivate() {
		// Not a tile, just using its layout posibilities
	}

}
