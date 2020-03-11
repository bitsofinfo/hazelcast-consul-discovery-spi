package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.cluster.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;

/**
 * @see BaseRegistrator
 * 
 * The IP/PORT that it registers with is that auto detected/determined by Hazelcast
 * itself via Hazelcast's DiscoveryNode's Address that is passed to the ConsulDiscoveryStrategy
 * in its constructor.
 * 
 * Custom options (specified as JSON value for the 'consul-registrator-config')
 * These are in ADDITION to those commonly defined in BaseRegistrator (base-class)
 * 	
 *	 - preferPublicAddress (true|false) : use the public IP determined by
 *	 								      hazelcast (if not null) over the private IP
 *   			           		 
 * @author bitsofinfo
 *
 */
public class LocalDiscoveryNodeRegistrator extends BaseRegistrator {

	// properties that are supported in the JSON value for the 'consul-registrator-config' config property
	// in ADDITION to those defined in BaseRegistrator
	public static final String CONFIG_PROP_PREFER_PUBLIC_ADDRESS = "preferPublicAddress";

	@Override 
	public Address determineMyLocalAddress(DiscoveryNode localDiscoveryNode, Map<String, Object> registratorConfig) {
		
		Address myLocalAddress = localDiscoveryNode.getPrivateAddress();
		 
		Object usePublicAddress = (Object)registratorConfig.get(CONFIG_PROP_PREFER_PUBLIC_ADDRESS);
		if (usePublicAddress != null && usePublicAddress instanceof Boolean && (Boolean)usePublicAddress) {
			logger.info("Registrator config property: " + CONFIG_PROP_PREFER_PUBLIC_ADDRESS +":"+usePublicAddress + " attempting to use it...");
			Address publicAddress = localDiscoveryNode.getPublicAddress();
			if (publicAddress != null) {
				myLocalAddress = publicAddress;
			}
		}
		
		return myLocalAddress;
	}


}
