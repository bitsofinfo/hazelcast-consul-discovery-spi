package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;

/**
 * Defines an interface for an object who's responsibility
 * it is to register (and deregister) this hazelcast instance as a service
 * node with Consul.
 * 
 * @author bitsofinfo
 *
 */
public interface ConsulRegistrator {
	
	/**
	 * Return the service id as registered with Consul
	 * 
	 * @return
	 */
	public String getMyServiceId();

	/**
	 * Initialize the registrator
	 * 
	 * @param consulHost
	 * @param consulPort
	 * @param consulServiceName
	 * @param consulTags
	 * @param consulAclToken
	 * @param consulSslEnabled
	 * @param consulSslServerCertFilePath
	 * @param consulSslServerCertBase64
	 * @param consulServerHostnameVerify
	 * @param localDiscoveryNode
	 * @param registratorConfig
	 * @param logger
	 * @throws Exception
	 */
	public void init(String consulHost, 
			         Integer consulPort,
			         String consulServiceName,
			         String[] consulTags,
			         String consulAclToken,
			         boolean consulSslEnabled,
					 String	consulSslServerCertFilePath,
					 String consulSslServerCertBase64,
					 boolean consulServerHostnameVerify,
			         DiscoveryNode localDiscoveryNode,
			         Map<String, Object> registratorConfig,
			         ILogger logger) throws Exception;
	
	/**
	 * Register this hazelcast instance as a service node
	 * with Consul
	 * 
	 * @throws Exception
	 */
	public void register() throws Exception;
	
	/**
	 * Deregister this hazelcast instance as a service node
	 * with Consul
	 * 
	 * @throws Exception
	 */
	public void deregister() throws Exception;
	
}
