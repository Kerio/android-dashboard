package com.kerio.dashboard.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import android.content.Context;

public class TrustStoreHelper {
	private static final String KeyStoreFilename = "dashboardtruststore.bks";
	private static final String KeyStorePassword = "Dashboard2185480";
	
	private static KeyStore trustStoreInstance = null;
	
	Context context = null;
	
	public TrustStoreHelper(Context context) {
		this.context = context;  
	}
	
    private File getTrustStoreFile() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	File home = this.context.getFilesDir();
    	File certStoreFile = new File(home, KeyStoreFilename); 
    	if(certStoreFile.exists()){
    		return certStoreFile;
    	}
    	
		KeyStore trustStore = KeyStore.getInstance("BKS");
		trustStore.load(null, KeyStorePassword.toCharArray());
		FileOutputStream outFile = new FileOutputStream(certStoreFile);
		trustStore.store(outFile, KeyStorePassword.toCharArray());
		outFile.close();
		
		return certStoreFile;
    }
    
	public synchronized KeyStore getKeystore(){
		if(trustStoreInstance != null){
			return trustStoreInstance;
		}
			
		try{
			KeyStore trustStore = KeyStore.getInstance("BKS");
			FileInputStream stream = new FileInputStream(getTrustStoreFile());
			trustStore.load(stream, KeyStorePassword.toCharArray());
			trustStoreInstance = trustStore;
			return trustStoreInstance;
		}catch(IOException ioe){
			
		}catch(CertificateException ce){
			
		}catch (KeyStoreException kse) {

		}catch (NoSuchAlgorithmException nsae) {

		}
		
		return null;
	}
	
	 public synchronized void store(){
    	if(trustStoreInstance == null){
    		return;
    	}
    	
    	try{
    		FileOutputStream outFile = new FileOutputStream(getTrustStoreFile());
    		trustStoreInstance.store(outFile, KeyStorePassword.toCharArray());
    		outFile.close();
    		
		}catch(IOException ioe){
			
		}catch(CertificateException ce){
			
		}catch (KeyStoreException kse) {

		}catch (NoSuchAlgorithmException nsae) {

		}
    }
}
