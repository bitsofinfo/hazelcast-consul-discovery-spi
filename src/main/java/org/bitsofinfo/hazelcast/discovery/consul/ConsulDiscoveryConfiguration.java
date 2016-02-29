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
			new SimplePropertyDefinition("consul-service-tags", true, PropertyTypeConverter.STRING);
	
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
	
	public static final PropertyDefinition CONSUL_ACL_TOKEN = 
			new SimplePropertyDefinition("consul-acl-token", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_SSL_ENABLED = 
			new SimplePropertyDefinition("consul-ssl-enabled", PropertyTypeConverter.BOOLEAN);
	
	public static final PropertyDefinition CONSUL_SSL_SERVER_CERT_FILE_PATH = 
			new SimplePropertyDefinition("consul-ssl-server-cert-file-path", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_SSL_SERVER_CERT_BASE64 = 
			new SimplePropertyDefinition("consul-ssl-server-cert-base64", PropertyTypeConverter.STRING);
	
	public static final PropertyDefinition CONSUL_SSL_SERVER_HOSTNAME_VERIFY = 
			new SimplePropertyDefinition("consul-ssl-server-hostname-verify", PropertyTypeConverter.BOOLEAN);

}
