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
import java.util.LinkedHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.gui.tiles.TextTile.Pairs;

import android.util.Log;

public class ApiClient{
	
	static LocalTrustManagement localTrustManagement = null;
	
	LinkedHashMap<String, JSONObject> response = null;
	private ServerConfig config;

	private int port;
	private String token;
	private String error;
	private HttpClient httpClient;
	
	public ApiClient(String server, ServerConfig serverConfig, KeyStore trustStore) {
		this(server, serverConfig, trustStore, 0);
	}
	
	public ApiClient(String server, ServerConfig serverConfig, KeyStore trustStore, int port) {
		this.config = serverConfig;
		this.port = port;
		
		HttpParams params = new BasicHttpParams();
	    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	    params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.DEFAULT_CONTENT_CHARSET);
	    params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
	    params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30 * 1000);
	    params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30 * 1000);
	    
	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    httpClient = getSslClient(new DefaultHttpClient(conMgr, params), serverConfig, trustStore);
	}
	
	public boolean login(String username, String password) throws SSLHandshakeException {
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
		
		JSONObject ret = execSafe("Session.login", params);
		
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
		try {
			return execSafe(method, arguments);
		}catch(SSLHandshakeException se) {
			Log.d("ApiClient.exec()", "Certificate problem occured", se);
		}
		return null;
	}
	
	public JSONObject execSafe(String method, JSONObject arguments) throws SSLHandshakeException {
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
			Log.d("ApiClient Request", "exec - " + method);
			HttpResponse resp = httpClient.execute(httpPost);
			
			StatusLine status = resp.getStatusLine();
		    if (status.getStatusCode() == 200) {
		    	return processEntity(resp.getEntity()).getJSONObject("result");
		    }
		    
		    error = "Invalid response from server: " + status.toString();	
		}catch(SSLHandshakeException se){
			throw se;
		} catch (Exception e) {
			error = e.toString();
		}
		
		Log.d("ApiClient", error);
		return null;
	}
	
	/**
	 * Send batch of requests to one server
	 * 
	 * @param requests List of request as pair of "Method" and "Params"
	 * @return List of pairs "Method" and "Response"
	 */
	public LinkedHashMap<String, JSONObject> execBatch(LinkedHashMap<String, JSONObject> requests) {
		error = "";
		JSONObject data = new JSONObject();
		JSONObject params = new JSONObject();
		JSONArray commandList = new JSONArray();
		
		try {
			//create list of commands to be sent
			for (Pairs.Entry<String, JSONObject> request : requests.entrySet()) {
				JSONObject oneParam = new JSONObject();
				oneParam.put("method", request.getKey());
				oneParam.put("params", request.getValue());				
				commandList.put(oneParam);
			}
			params.put("commandList", commandList);
			
			//add jsonrpc parameters
			data.put("jsonrpc", 2.0);
			data.put("id", 1);
			data.put("method", "Batch.run");
			data.put("params", params);
		} catch (JSONException e) {
			Log.d("ApiClient", error);
			return null;
		}

		
		try {
			//send http requests
			String query = new String(data.toString());
	
			HttpPost httpPost = new HttpPost(getUrl());
			httpPost.setEntity(new StringEntity(query));			
			
			if (this.token != null) {
				httpPost.setHeader("X-Token", this.token);
			}
			Log.d("ApiClient Request", "execBatch - batch size " + Integer.toString(requests.size()));
			HttpResponse resp = this.httpClient.execute(httpPost);
			
			//handle response
			StatusLine status = resp.getStatusLine();
		    if (status.getStatusCode() == 200) {
		    	
		    	JSONArray response = processEntity(resp.getEntity()).getJSONArray("result");
		    	
		    	//separate responses for each requests
		    	int i = 0;
		    	for (Pairs.Entry<String, JSONObject> request : requests.entrySet()) {
					requests.put(request.getKey(), response.getJSONObject(i).getJSONObject("result"));
					i++;
				}
		    	return requests;
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
		return "https://" + this.config.server + ":" + (port == 0 ? "4081" : Integer.toString(port)) + "/admin/api/jsonrpc/";
	}
	
	private JSONObject processEntity(HttpEntity entity) throws IllegalStateException, IOException, JSONException {
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		String line, result = "";
		while ((line = br.readLine()) != null)
			result += line;
		
		return new JSONObject(result);
	}

	
	private HttpClient getSslClient(HttpClient client, ServerConfig serverConfig, KeyStore trustStore){
	    try {
	        
	        SSLSocketFactory ssf =  new TrustSSLSocketFactory(trustStore, serverConfig);
	        ssf.setHostnameVerifier(new BrowserCompatHostnameVerifier()); //TODO CIMA
	        
	        ClientConnectionManager ccm = client.getConnectionManager();
	        

	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", (SocketFactory) ssf, 4081));

	        
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;//TODO CIMA
	    }
	}
}
