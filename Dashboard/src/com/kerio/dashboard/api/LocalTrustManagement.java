package com.kerio.dashboard.api;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.kerio.dashboard.R;
import com.kerio.dashboard.config.ServerConfig;

import android.content.Context;
import android.util.Log;

public class LocalTrustManagement implements X509TrustManager {

	private TrustManagerFactory tmf = null;
	private TrustManagerFactory localTmf = null;
	private X509TrustManager origTrustManager = null;
	private X509TrustManager localTrustManager = null;
	private ServerConfig serverConfig = null;
	private KeyStore localKeyStore = null;
	

	public LocalTrustManagement(ServerConfig serverConfig, KeyStore localKeyStore) {
		this.serverConfig = serverConfig;
		this.localKeyStore = localKeyStore;
		initManagers();
	}
	
	private void initManagers(){
		try{
			this.tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			this.localTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			this.tmf.init((KeyStore)null);
			this.localTmf.init(localKeyStore);
			
		}catch(NoSuchAlgorithmException nsae){
			Log.d("LocalTrustManagement", "Algorithm for SSL trust managemetn not found", nsae);
		}catch(KeyStoreException kse){
			Log.d("LocalTrustManagement", "Unable to initialize trustmanegement factory.", kse);
		}
		
		 
		TrustManager[] trustManagers = tmf.getTrustManagers();
		for (TrustManager trustManager : trustManagers) {
			if(trustManager instanceof X509TrustManager){
				this.origTrustManager = (X509TrustManager)trustManager;
				break;
			}
		}
		
		//TODO CIMA duplicita
		trustManagers = this.localTmf.getTrustManagers();
		for (TrustManager trustManager : trustManagers) {
			if(trustManager instanceof X509TrustManager){
				this.localTrustManager = (X509TrustManager)trustManager;
				break;
			}
		}
	}
	
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		this.origTrustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if(this.serverConfig != null){
			this.serverConfig.setCertChain(chain);
		}
		try{
			this.origTrustManager.checkServerTrusted(chain, authType);
		}catch(CertificateException ce){
			initManagers();//TODO just for a test
			this.localTrustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.origTrustManager.getAcceptedIssuers();
	}

}
