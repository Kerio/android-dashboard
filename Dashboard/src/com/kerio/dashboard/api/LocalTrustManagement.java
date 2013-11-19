package com.kerio.dashboard.api;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.kerio.dashboard.config.ServerConfig;

public class LocalTrustManagement implements X509TrustManager {

	private X509TrustManager origTrustManager = null;
	private X509TrustManager localTrustManager = null;
	private ServerConfig serverConfig = null;
	

	public LocalTrustManagement(ServerConfig serverConfig, KeyStore localKeyStore) throws NoSuchAlgorithmException, KeyStoreException {
		this.serverConfig = serverConfig;
		
		this.origTrustManager = initManagement(null);
		this.localTrustManager = initManagement(localKeyStore);
	}
	
	private static X509TrustManager initManagement(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);

		TrustManager[] trustManagers = tmf.getTrustManagers();
		for (TrustManager trustManager : trustManagers) {
			if(trustManager instanceof X509TrustManager){
				return (X509TrustManager)trustManager;
			}
		}
		return null;
	}
		
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if(this.origTrustManager != null){
			this.origTrustManager.checkClientTrusted(chain, authType);
		}else{
			throw new CertificateException("System default trust manager not initialized", new NullPointerException());
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if(this.serverConfig != null){
			this.serverConfig.setCertChain(chain);
		}
		
		try{
			if(this.origTrustManager != null){
				this.origTrustManager.checkServerTrusted(chain, authType);
			}else{
				throw new CertificateException("System default trust manager not initialized", new NullPointerException());
			}
		}catch(CertificateException ce){
			if(this.localTrustManager != null){
				this.localTrustManager.checkServerTrusted(chain, authType);
			}else{
				throw ce;
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.origTrustManager.getAcceptedIssuers();
	}

}
