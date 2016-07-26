package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orbitz.fasterxml.jackson.core.JsonFactory;
import com.orbitz.fasterxml.jackson.core.type.TypeReference;
import com.orbitz.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.ServiceHealth;


/**
 * DiscoveryStrategy for Consul
 * 
 * 
 * 
 * @author bitsofinfo
 *
 */
public class ConsulDiscoveryStrategy extends AbstractDiscoveryStrategy implements Runnable {

	// how we connect to consul
	private final String consulHost;  
	private final Integer consulPort;
	
	// service name we will register under, and tags to apply
	private String[] consulServiceTags = null;
	private String consulServiceName = null;
	
	// if we only discover healthy nodes...
	private boolean consulHealthyOnly = true;
	
	// How we register with Consul
	private ConsulRegistrator registrator = null;
	
	// Consul clients
	private CatalogClient consulCatalogClient = null;
	private HealthClient consulHealthClient = null;
	
	// we set this to track if discoverNodes was ever invoked
	private boolean discoverNodesInvoked = false;
	
	// ACL token to be used for agent, health and catalog clients
	private String consulAclToken  = null;
	

	/**
	 * Constructor
	 * 
	 * @param localDiscoveryNode
	 * @param logger
	 * @param properties
	 */
	public ConsulDiscoveryStrategy(DiscoveryNode localDiscoveryNode, ILogger logger, Map<String, Comparable> properties ) {

		super( logger, properties );
		
		// get basic properites for the strategy
		this.consulHost = getOrDefault("consul-host",  ConsulDiscoveryConfiguration.CONSUL_HOST, "localhost");
		this.consulPort = getOrDefault("consul-port",  ConsulDiscoveryConfiguration.CONSUL_PORT, 8500);
		this.consulServiceTags = getOrDefault("consul-service-tags",  ConsulDiscoveryConfiguration.CONSUL_SERVICE_TAGS, "").split(",");		
		this.consulServiceName = getOrDefault("consul-service-name",  ConsulDiscoveryConfiguration.CONSUL_SERVICE_NAME, "");		
		this.consulHealthyOnly = getOrDefault("consul-healthy-only",  ConsulDiscoveryConfiguration.CONSUL_HEALTHY_ONLY, true);		
		long discoveryDelayMS = getOrDefault("consul-discovery-delay-ms",  ConsulDiscoveryConfiguration.CONSUL_DISCOVERY_DELAY_MS, 30000);		
		this.consulAclToken = getOrDefault("consul-acl-token", ConsulDiscoveryConfiguration.CONSUL_ACL_TOKEN, null);
		
		boolean consulSslEnabled = getOrDefault("consul-ssl-enabled", ConsulDiscoveryConfiguration.CONSUL_SSL_ENABLED, false);
		String consulSslServerCertFilePath = getOrDefault("consul-ssl-server-cert-file-path", ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_CERT_FILE_PATH, null);
		String consulSslServerCertBase64 = getOrDefault("consul-ssl-server-cert-base64", ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_CERT_BASE64, null);
		boolean consulServerHostnameVerify = getOrDefault("consul-ssl-server-hostname-verify", ConsulDiscoveryConfiguration.CONSUL_SSL_SERVER_HOSTNAME_VERIFY, true);
		
		
		
		
		
		// our ConsulRegistrator default is DoNothingRegistrator
		String registratorClassName = getOrDefault("consul-registrator",  
													ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR, 
													DoNothingRegistrator.class.getCanonicalName());
		
		// this is optional, custom properties to configure a registrator
		// @see the ConsulRegistrator for a description of supported options 
		String registratorConfigJSON = getOrDefault("consul-registrator-config",  
													ConsulDiscoveryConfiguration.CONSUL_REGISTRATOR_CONFIG, 
													null);
		
		// if JSON config is present attempt to parse it into a map
		Map<String,Object> registratorConfig = null;
		if (registratorConfigJSON != null && !registratorConfigJSON.trim().isEmpty()) {
			try {
				
				JsonFactory factory = new JsonFactory(); 
			    ObjectMapper mapper = new ObjectMapper(factory); 
			    TypeReference<HashMap<String,Object>> typeRef 
			            = new TypeReference<HashMap<String,Object>>() {};
		
			    registratorConfig = mapper.readValue(registratorConfigJSON.getBytes(), typeRef);
			    
			} catch(Exception e) {
				logger.severe("Unexpected error parsing 'consul-registrator-config' JSON: " + 
							registratorConfigJSON + " error="+e.getMessage(),e);
			}
		}

		
		// Ok, now construct our registrator and register with Consul
		try {
			registrator = (ConsulRegistrator)Class.forName(registratorClassName).newInstance();
			
			logger.info("Using ConsulRegistrator: " + registratorClassName);
			
			registrator.init(consulHost, 
					consulPort, 
					consulServiceName, 
					consulServiceTags, 
					consulAclToken,
					consulSslEnabled,
					consulSslServerCertFilePath,
					consulSslServerCertBase64,
					consulServerHostnameVerify,
					localDiscoveryNode, 
					registratorConfig, 
					logger);
			registrator.register();
			
		} catch(Exception e) {
			logger.severe("Unexpected error attempting to init() ConsulRegistrator and register(): " +e.getMessage(),e);
		}


		try{
					
			ConsulBuilder builder = ConsulClientBuilder.class.newInstance();
			Consul consul = builder.buildConsul(consulHost, 
												consulPort, 
												consulSslEnabled, 
												consulSslServerCertFilePath, 
												consulSslServerCertBase64, 
												consulServerHostnameVerify);
			
			// build our clients
			this.consulCatalogClient = consul.catalogClient();
			this.consulHealthClient = consul.healthClient();
			
		}catch(Exception e) {
			String msg = "Unexpected error in configuring discovery: " + e.getMessage();
			logger.severe(msg,e);
		}
		
		// register our shutdown hook for deregisteration on shutdown...
		Thread shutdownThread = new Thread(this);
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		
		// finally sleep a bit according to the configured discoveryDelayMS
		try {
			logger.info("Registered our service instance w/ Consul OK.. delaying Hazelcast discovery, sleeping: " + discoveryDelayMS + "ms");
			Thread.sleep(discoveryDelayMS);
		} catch(Exception e) {
			logger.severe("Unexpected error sleeping prior to discovery: " + e.getMessage(),e);
		}
									
	}                              

	@Override
	public Iterable<DiscoveryNode> discoverNodes() {
		
		List<DiscoveryNode> toReturn = new ArrayList<DiscoveryNode>();
		
		try {
			// discover healthy nodes only? (and its NOT the first invocation...)
			if (this.consulHealthyOnly && discoverNodesInvoked) {
				
				List<ServiceHealth> nodes = consulHealthClient.getHealthyServiceInstances(consulServiceName, ConsulUtility.getAclToken(this.consulAclToken)).getResponse();
				
				for (ServiceHealth node : nodes) {
					toReturn.add(new SimpleDiscoveryNode(
									new Address(node.getService().getAddress(),node.getService().getPort())));
					getLogger().info("Discovered healthy node: " + node.getService().getAddress()+":"+node.getService().getPort());
				}
				
			// discover all services, regardless of health or this is the first invocation
			} else {
				
				ConsulResponse<List<CatalogService>> response = this.consulCatalogClient.getService(consulServiceName, ConsulUtility.getAclToken(this.consulAclToken));
				
				for (CatalogService service : response.getResponse()) {
					toReturn.add(new SimpleDiscoveryNode(
							new Address(service.getServiceAddress(), service.getServicePort())));
					getLogger().info("Discovered healthy node: " + service.getServiceAddress()+":"+service.getServicePort());
				}
			}
			
		} catch(Exception e) {
			getLogger().severe("discoverNodes() unexpected error: " + e.getMessage(),e);
		}

		// flag we were invoked
		discoverNodesInvoked = true;
		
		return toReturn;
	}

	@Override
	public void run() {
		try {
			if (registrator != null) {
				getLogger().info("Deregistering myself from Consul: " + this.registrator.getMyServiceId());
				registrator.deregister();
			}
		} catch(Throwable e) {
			this.getLogger().severe("Unexpected error in ConsulRegistrator.deregister(): " + e.getMessage(),e);
		}
		
	}
}
