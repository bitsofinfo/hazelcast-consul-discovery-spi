package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

public class ConsulDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

	private static final Collection<PropertyDefinition> PROPERTIES =
			Arrays.asList(new PropertyDefinition[]{
						ConsulDiscoveryConfiguration.CONSUL_HOST, 
						ConsulDiscoveryConfiguration.CONSUL_PORT,
						ConsulDiscoveryConfiguration.CONSUL_SERVICE_NAME,
						ConsulDiscoveryConfiguration.CONSUL_HEALTHY_ONLY,
						ConsulDiscoveryConfiguration.CONSUL_SERVICE_TAGS,
						ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR,
						ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR_CONFIG,
						ConsulDiscoveryConfiguration.CONSUL_DISCOVERY_DELAY_MS,
						ConsulDiscoveryConfiguration.CONSUL_ACL_TOKEN,
						ConsulDiscoveryConfiguration.CONSUL_SSL_ENABLED,
						ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_CERT_FILE_PATH,
						ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_CERT_BASE64,
						ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_HOSTNAME_VERIFY
					});

	public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
		// Returns the actual class type of the DiscoveryStrategy
		// implementation, to match it against the configuration
		return ConsulDiscoveryStrategy.class;
	}

	public Collection<PropertyDefinition> getConfigurationProperties() {
		return PROPERTIES;
	}

	public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode,
												  ILogger logger,
												  Map<String, Comparable> properties ) {

		return new ConsulDiscoveryStrategy( discoveryNode, logger, properties );                                      
	}   

}
