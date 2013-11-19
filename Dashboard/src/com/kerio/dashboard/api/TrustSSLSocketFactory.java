package com.kerio.dashboard.api;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.kerio.dashboard.config.ServerConfig;

public class TrustSSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    javax.net.ssl.SSLSocketFactory socketFactory = null; 

    public TrustSSLSocketFactory(KeyStore truststore, ServerConfig serverConfig) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
        X509TrustManager tm = new LocalTrustManagement(serverConfig, truststore);
        this.sslContext.init(null, new TrustManager[]{tm}, null);
        this.socketFactory = sslContext.getSocketFactory();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return this.socketFactory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return this.socketFactory.createSocket();
    }
}