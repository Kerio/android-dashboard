package com.kerio.dashboard.config.gui;

import java.security.cert.X509Certificate;

import android.content.Context;
import android.preference.Preference;

public class CertificatePreference extends Preference {
	X509Certificate certificate = null;
	String certificateAlias = null;
	
	public CertificatePreference(Context context) {
		super(context);
	}
}
