package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.cluster.Address;
import com.orbitz.consul.model.agent.Registration.RegCheck;

/**
 * Interface to define health check operations
 * 
 * @author bmudda
 *
 */
public interface HealthCheckBuilder {

	/**
	 * Method to build a registration check object
	 * 
	 * @param registratorConfig
	 * @param localAddress
	 * @return RegCheck object
	 */
	public RegCheck buildRegistrationCheck( Map<String, Object> registratorConfig, Address localAddress);
	
}
