package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.cluster.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;

/**
 * @see BaseRegistrator
 * 
 * The IP/PORT that it registers with is whatever is specified by
 * in the `consul-registrator-config` config options `ipAddress` and `port`
 * described below.
 * 
 * Custom options (specified as JSON value for the 'consul-registrator-config')
 * These are in ADDITION to those commonly defined in BaseRegistrator (base-class)
 * 	
 *   			           
 *	 - registerWithIpAddress: the explicit IP address that this node should be registered
 *                			  with Consul as its ServiceAddress 
 *                
 *   - registerWithPort: the explicit PORT that this node should be registered
 *            			 with Consul as its ServiceAddress
 *   			           		 
 * @author bitsofinfo
 *
 */
public class ExplicitIpPortRegistrator extends BaseRegistrator {
	
	// properties that are supported in the JSON value for the 'consul-registrator-config' config property
	// in ADDITION to those defined in BaseRegistrator
	public static final String CONFIG_PROP_REGISTER_WITH_IP_ADDRESS = "registerWithIpAddress";
	public static final String CONFIG_PROP_REGISTER_WITH_PORT = "registerWithPort";

	@Override 
	public Address determineMyLocalAddress(DiscoveryNode localDiscoveryNode, Map<String, Object> registratorConfig) throws Exception {

		String registerWithIpAddress = (String)registratorConfig.get(CONFIG_PROP_REGISTER_WITH_IP_ADDRESS);
		Integer registerWithPort = (Integer)registratorConfig.get(CONFIG_PROP_REGISTER_WITH_PORT);
		
		logger.info("Registrator config properties: " + CONFIG_PROP_REGISTER_WITH_IP_ADDRESS +":"+registerWithIpAddress 
											    + " " +  CONFIG_PROP_REGISTER_WITH_PORT + ":" + registerWithPort +
											    ", will attempt to register with this IP/PORT...");

		
		
		return new Address(registerWithIpAddress, registerWithPort);
	}
}
