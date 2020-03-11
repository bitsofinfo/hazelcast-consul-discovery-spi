package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Arrays;
import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.cluster.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration.RegCheck;

/**
 * Use derivatives of this ConsulRegistrator if you don't have or don't want to
 * run a separate Consul agent process on the same box where this hazelcast
 * enabled application runs. This ConsulRegistrator will register this 
 * hazelcast instance with Consul as a service and also optional
 * configure a health-check script if you defined it. 
 * 
 * The IP/PORT that it registers with is generally dictated by classes
 * that derive from this class and override the determineMyLocalAddress()
 * method.
 * 
 * It will also de-register the service if invoked to do so.
 * 
 * Common custom options (specified as JSON value for the 'consul-registrator-config')
 * which are available to all derivative classes
 *
 *	 - healthCheckScript: can be anything you want Consul to do to determine health. 
 *	                      Variables #MYIP/#MYPORT will be replaced. https://www.consul.io/docs/agent/checks.html 
 *	                      
 *	 - healthCheckScriptIntervalSeconds: self explanatory
 *
 *  - healthCheckHttp: valid hostname and port to use for health check. You can provide optional
 *   			       path to a specific script.
 *   
 *   - healthCheckHttpIntervalSeconds: self explanatory
 *   			           		
 * 
 * @author bitsofinfo
 *
 */
public abstract class BaseRegistrator implements ConsulRegistrator {
	
	public static final String CONFIG_PROP_HEALTH_CHECK_PROVIDER = "healthCheckProvider";
	
	protected ILogger logger = null;
	protected Address myLocalAddress = null;
	protected String[] tags = null;
	protected String consulServiceName = null;
	protected String consulHost = null;
	protected Integer consulPort = null;
	protected String consulAclToken = null;
	protected Map<String, Object> registratorConfig = null;
	
	private String myServiceId = null;
	
	private AgentClient consulAgentClient;
	
	protected abstract Address determineMyLocalAddress(DiscoveryNode localDiscoveryNode,  
													   Map<String, Object> registratorConfig) throws Exception;

	@Override
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
			         ILogger logger) throws Exception {
		
		this.logger = logger;
		this.tags = consulTags;
		this.consulHost = consulHost;
		this.consulPort = consulPort;
		this.consulServiceName = consulServiceName;
		this.consulAclToken = consulAclToken;
		this.registratorConfig = registratorConfig;
		
		try {
			/**
			 * Determine my local address
			 */
			this.myLocalAddress = determineMyLocalAddress(localDiscoveryNode, registratorConfig);
			logger.info("Determined local DiscoveryNode address to use: " + myLocalAddress);
			
			
			//Build our consul client to use. We pass in optional TLS information
			ConsulBuilder builder = ConsulClientBuilder.class.newInstance();
			Consul consul = builder.buildConsul(consulHost, 
												consulPort, 
												consulSslEnabled, 
												consulSslServerCertFilePath, 
												consulSslServerCertBase64, 
												consulServerHostnameVerify,
												consulAclToken);
			
			 
			// build my Consul agent client that we will register with
			this.consulAgentClient =  consul.agentClient();
			
		} catch(Exception e) {
			String msg = "Unexpected error in configuring LocalDiscoveryNodeRegistration: " + e.getMessage();
			logger.severe(msg,e);
			throw new Exception(msg,e);
		}
		
	}
	
	@Override
	public String getMyServiceId() {
		return this.myServiceId;
	}

	@Override
	public void register() throws Exception {
		
		try {
			this.myServiceId = this.consulServiceName + "-" + 
							   this.myLocalAddress.getInetAddress().getHostAddress() +"-" + 
							   this.myLocalAddress.getHost() + "-" + 
							   this.myLocalAddress.getPort();
			
			
			ImmutableRegistration.Builder builder = ImmutableRegistration.builder()
										.name(this.consulServiceName)
										.id(myServiceId)
										.address(this.myLocalAddress.getInetAddress().getHostAddress())
										.port(this.myLocalAddress.getPort())
										.tags(Arrays.asList(tags));
			
			
			String healthCheckProvider = getHealthCheckProvider( (String)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_PROVIDER) );
			
			HealthCheckBuilder healthBuilder = (HealthCheckBuilder)Class.forName(healthCheckProvider).newInstance();
			RegCheck regCheck =healthBuilder.buildRegistrationCheck(registratorConfig, this.myLocalAddress);
		
			if (regCheck != null) {
				builder.check(regCheck);
			}
			
			// register...
			this.consulAgentClient.register(builder.build(), ConsulUtility.getAclToken(this.consulAclToken));
			
			this.logger.info("Registered with Consul["+this.consulHost+":"+this.consulPort+"] serviceId:"+myServiceId);
			
		} catch(Exception e) {
			String msg = "Unexpected error in register(serviceId:"+myServiceId+"): " + e.getMessage();
			logger.severe(msg,e);
			throw new Exception(msg,e);
		}
	}
	
	@Override
	public void deregister() throws Exception {
		try {
			this.consulAgentClient.deregister(this.myServiceId);
			
		} catch(Exception e) {
			String msg = "Unexpected error in deregister(serviceId:"+myServiceId+"): " + e.getMessage();
			logger.severe(msg,e);
			throw new Exception(msg,e);
		}
	}
	
	/**
	 * Helper method to get the health check provider class name.
	 * This is for backward compatibility support. (It defaults to script based health check)
	 * 
	 * @param checkProvider
	 * @return String - name of the health check class
	 */
	private String getHealthCheckProvider(String checkProvider){
		
		return (checkProvider == null || checkProvider.trim().isEmpty()) ? ScriptHealthCheckBuilder.class.getCanonicalName() : checkProvider;
	}


}
