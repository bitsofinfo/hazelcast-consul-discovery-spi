package org.bitsofinfo.hazelcast.discovery.consul;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.config.properties.SimplePropertyDefinition;

/**
 * Defines constants for our supported Properties
 * 
 * @author bitsofinfo
 *
 */
public class ConsulDiscoveryConfiguration {
	
	public static final PropertyDefinition CONSUL_HOST = 
			new SimplePropertyDefinition("consul-host", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_PORT = 
			new SimplePropertyDefinition("consul-port", PropertyTypeConverter.INTEGER);
	
	public static final PropertyDefinition CONSUL_SERVICE_TAGS = 
			new SimplePropertyDefinition("consul-service-tags", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_SERVICE_NAME = 
			new SimplePropertyDefinition("consul-service-name", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_HEALTHY_ONLY = 
			new SimplePropertyDefinition("consul-healthy-only", PropertyTypeConverter.BOOLEAN);
	
	public static final PropertyDefinition CONSUL_REGISTRATOR = 
			new SimplePropertyDefinition("consul-registrator", true, PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_REGISTRATOR_CONFIG = 
			new SimplePropertyDefinition("consul-registrator-config", true, PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_DISCOVERY_DELAY_MS = 
			new SimplePropertyDefinition("consul-discovery-delay-ms", PropertyTypeConverter.INTEGER);
	

}
