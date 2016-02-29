package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;

/**
 * Use this ConsulRegistrator if you manage the registration
 * of your hazelcast nodes manually/externally via a local
 * Consul agent or other means. No registration/deregistration
 * will occur if you use this implementation
 * 
 * @author bitsofinfo
 *
 */
public class DoNothingRegistrator implements ConsulRegistrator {

	@Override
	public String getMyServiceId() {
		return null;
	}

	@Override
	public void init(
			String consulHost, 
			Integer consulPort, 
			String consulServiceName, 
			String[] consulServiceTags, 
			String consulAclToken,  
			boolean consulSslEnabled,
			String	consulSslServerCertFilePath,
			String consulSslServerCertBase64,
			boolean consulServerHostnameVerify,
			DiscoveryNode localDiscoveryNode,
			Map<String, Object> registratorConfig, 
			ILogger logger) {

	}

	@Override
	public void register() throws Exception {
	}

	@Override
	public void deregister() throws Exception {
	}

}
