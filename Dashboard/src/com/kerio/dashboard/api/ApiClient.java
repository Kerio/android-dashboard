package com.kerio.dashboard.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ApiClient {
	public ApiClient(String server) {
		this.server = server;
		this.port = 0;
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 10000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 10000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		httpClient = sslClient(new DefaultHttpClient(httpParameters));
	}
	
	public ApiClient(String server, int port) {
		this.server = server;
		this.port = port;
		httpClient = new DefaultHttpClient();
	}
	
	public boolean login(String username, String password) {
		JSONObject params = new JSONObject();
		JSONObject app = new JSONObject();
		try {
			app.put("name", "Dashboard for android");
			app.put("vendor", "Kerio");
			app.put("version", "0.1");
			params.put("userName", username);
			params.put("password", password);
			params.put("application", app);
		} catch (JSONException e) {
			return false;
		}
		
		JSONObject ret = exec("Session.login", params);
		
		if (ret == null) {
			return false;
		}
		
		try {
			token = ret.getString("token");
		} catch (JSONException e) {
			error = e.toString();
			return false;
		}
		
		return true;
	}
	
	public JSONObject exec(String method, JSONObject arguments) {
		error = "";
		JSONObject data = new JSONObject();
		try {
			data.put("jsonrpc", 2.0);
			data.put("id", 1);
			data.put("method", method);
			data.put("params", arguments);
		} catch (JSONException e) {
			Log.d("ApiClient", error);
			return null;
		}
		
		String query = new String(data.toString());
		try {
			HttpPost httpPost = new HttpPost(getUrl());
			httpPost.setEntity(new StringEntity(query));
			
			if (token != null) {
				httpPost.setHeader("X-Token", token);
			}
			HttpResponse resp = httpClient.execute(httpPost);
			
			StatusLine status = resp.getStatusLine();
		    if (status.getStatusCode() == 200) {
		    	return processEntity(resp.getEntity()).getJSONObject("result");
		    }
		    
		    error = "Invalid response from server: " + status.toString();	
		} catch (Exception e) {
			error = e.toString();
		}
		
		Log.d("ApiClient", error);
		return null;
	}
	
	public String getLastError() {
		return error;
	}
	
	private String getUrl() {
		return "https://" + server + ":" + (port == 0 ? "4081" : Integer.toString(port)) + "/admin/api/jsonrpc/";
	}
	
	private JSONObject processEntity(HttpEntity entity) throws IllegalStateException, IOException, JSONException {
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		String line, result = "";
		while ((line = br.readLine()) != null)
			result += line;
		
		return new JSONObject(result);
	}
	
	private String server;
	private int port;
	private String token;
	private String error;
	private HttpClient httpClient;
	
	private HttpClient sslClient(HttpClient client) {
	    try {
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", ssf, 4081));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	
	public class MySSLSocketFactory extends SSLSocketFactory {
	     SSLContext sslContext = SSLContext.getInstance("TLS");

	     public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	         super(truststore);

	         TrustManager tm = new X509TrustManager() {
	             public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public X509Certificate[] getAcceptedIssuers() {
	                 return null;
	             }
	         };

	         sslContext.init(null, new TrustManager[] { tm }, null);
	     }

	     public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }

	     @Override
	     public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	         return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	     }

	     @Override
	     public Socket createSocket() throws IOException {
	         return sslContext.getSocketFactory().createSocket();
	     }
	}
}
