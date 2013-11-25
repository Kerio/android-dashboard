package com.kerio.dashboard.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.LinkedHashMap;

import javax.net.ssl.SSLHandshakeException;

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

import android.util.Log;

import com.kerio.dashboard.config.ServerConfig;
import com.kerio.dashboard.gui.tiles.TextTile.Pairs;

public class ApiClient{
	
	public class ApiClientException extends Throwable {
		private static final long serialVersionUID = -8038531525676458751L;
		private Throwable cause;
		private String message;

		ApiClientException(String message, Throwable cause) {
			this.cause = cause;
			this.message = message;
		}
		
		public Throwable getCause() {
			return cause;
		}
		
		public String getMessage(){
			return this.message + this.cause != null ? ": " + this.cause.getMessage() : "";
		}
	}
	
	static LocalTrustManagement localTrustManagement = null;
	
	LinkedHashMap<String, JSONObject> response = null;
	private ServerConfig config;

	private int port;
	private String token;
	private String error;
	private HttpClient httpClient = null;
	
	public ApiClient(ServerConfig serverConfig, KeyStore trustStore) throws ApiClientException {
		this(serverConfig, trustStore, 0);
	}
	
	public ApiClient(ServerConfig serverConfig, KeyStore trustStore, int port) throws ApiClientException {
		
		if(serverConfig ==  null){
			throw new ApiClientException("Provided serverConfig is invalid (null)", new NullPointerException());
		}
		
		if(trustStore ==  null){
			throw new ApiClientException("Provided trustStore is invalid (null)", new NullPointerException());
		}
		
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

	    try{
	    	httpClient = getSslClient(new DefaultHttpClient(conMgr, params), serverConfig, trustStore);
	    }catch(KeyManagementException kme){
	    	//depends only on trust management -> should never happen
	    	throw new ApiClientException("Problem with key management during SSL context initialization", kme);
	    }catch(UnrecoverableKeyException uke){
	    	throw new ApiClientException("Initialization of SSLSocketFactory failed", uke);
	    }catch(NoSuchAlgorithmException nsae){
	    	//Should never happen because we do use default algorithm
	    	throw new ApiClientException("Unsupported algorithm was chosen for instantiation of trust management factory", nsae);
	    }catch(KeyStoreException kse){
	    	throw new ApiClientException("Provided trust store is either missing or invalid", kse);
	    }
	}
	
	private HttpClient getSslClient(HttpClient client, ServerConfig serverConfig, KeyStore trustStore) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException{
        SSLSocketFactory ssf =  new TrustSSLSocketFactory(trustStore, serverConfig);
        ssf.setHostnameVerifier(new BrowserCompatHostnameVerifier()); //TODO Not effective
        
        ClientConnectionManager ccm = client.getConnectionManager();

        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", (SocketFactory) ssf, 4081));

        return new DefaultHttpClient(ccm, client.getParams());
	}
	
	public boolean login(String username, String password) throws SSLHandshakeException {
		JSONObject params = new JSONObject();
		JSONObject app = new JSONObject();
		try {
			app.put("name", "Dashboard for Android");
			app.put("vendor", "Kerio");
			app.put("version", "0.9");
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
}
