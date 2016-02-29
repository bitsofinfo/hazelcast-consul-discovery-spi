package org.bitsofinfo.hazelcast.discovery.consul;

import com.orbitz.consul.Consul;

/**
 * Interface for a client Consul builder. We use this to build all consul clients.
 * Currently supported clients are: agent, health and catalog
 * 
 * @author bmudda
 *
 */
public interface ConsulBuilder{
	
	/**
	 * Method to build a consul client given optional TLS information
	 * 
	 * @param consulHost
	 * @param consulPort
	 * @param consulSslEnabled
	 * @param consulSslServerCertFilePath
	 * @param consulSslServerCertBase64
	 * @param consulServerHostnameVerify
	 * @return a Consul client
	 * @throws Exception
	 */
	public Consul buildConsul (
				String consulHost,
				Integer consulPort,
				boolean consulSslEnabled,
				String	consulSslServerCertFilePath,
				String consulSslServerCertBase64,
				boolean consulServerHostnameVerify
			) throws Exception;
	
}

