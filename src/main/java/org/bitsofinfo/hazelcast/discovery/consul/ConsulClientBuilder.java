package org.bitsofinfo.hazelcast.discovery.consul;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.google.common.net.HostAndPort;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.orbitz.consul.Consul;
import com.orbitz.consul.Consul.Builder;
import org.apache.commons.codec.binary.Base64;
/**
 * An implementation of a Consul client builder.
 * 
 * @author bmudda
 *
 */
public class ConsulClientBuilder implements ConsulBuilder {

	private static final ILogger logger = Logger.getLogger(ConsulClientBuilder.class);	
	
	@Override
	public Consul buildConsul(
			String consulHost,
			Integer consulPort,
			boolean consulSslEnabled,
			String	consulSslServerCertFilePath,
			String consulSslServerCertBase64,
			boolean consulServerHostnameVerify
			) throws Exception {
		
		
		try{
			Builder consulBuilder = Consul.builder();
			
			//If SSL is enabled via xml configuration, then we use SSL context to build our client
			if (consulSslEnabled) {
				consulBuilder.withUrl("https://"+consulHost+":"+consulPort);
				
				if ( (consulSslServerCertFilePath != null && !consulSslServerCertFilePath.trim().isEmpty()) || 
						(consulSslServerCertBase64 != null && !consulSslServerCertBase64.trim().isEmpty()) ) {
					
					consulBuilder.withSslContext(getSSLContext(consulSslServerCertFilePath, consulSslServerCertBase64));
				}
				
				if (!consulServerHostnameVerify) {

					//We don't want verify host name, so we will mark host name as verified
					consulBuilder.withHostnameVerifier(new HostnameVerifier() {
			            public boolean verify(String s, SSLSession sslSession) {
			                return true;
			            }
			        });
				}
			} else {
				//Normal http without TLS
				consulBuilder.withHostAndPort(
						HostAndPort.fromParts(consulHost, consulPort));
			}
			
			Consul consul = consulBuilder.build();
			
			return consul;
		
		}catch(Exception e) {
			
			logger.severe("Unexpected Error occured while buildConsul() - " + e.getMessage(), e);
			throw(e);
		}
			
	}

	/**
	 * Method to build an SSL context to be used by the consul builder
	 * 
	 * @param consulSslServerCertFilePath
	 * @param consulSslServerCertBase64
	 * @return SSLContext object
	 * @throws Exception
	 */
	private SSLContext getSSLContext(String	consulSslServerCertFilePath, String consulSslServerCertBase64) throws Exception{
		
		try{
			
			InputStream is = null;
			
			//Get the self signed cert either from file or base64 encoded string passed through xml config
			if (consulSslServerCertFilePath !=null && !consulSslServerCertFilePath.trim().isEmpty()) {
				is = new FileInputStream(consulSslServerCertFilePath);
			} else {
				is = new ByteArrayInputStream(Base64.decodeBase64(consulSslServerCertBase64));
			}
					
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate caCert = (X509Certificate)cf.generateCertificate(is);
	
			TrustManagerFactory tmf = TrustManagerFactory
			    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
			
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null); // We don't need the KeyStore instance to come from a file.
			ks.setCertificateEntry("caCert", caCert);
	
			tmf.init(ks);
			TrustManager tm[] =tmf.getTrustManagers();
	
			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(null, tm, null);
			
			return sslContext;
			
		} catch(Exception e) {
			
			logger.severe("Unexpected Error getSSLContext() - " + e.getMessage(), e);
			throw(e);
		}
		
	}
	
	
}
