package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Arrays;
import java.util.Map;

import com.google.common.net.HostAndPort;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;

/**
 * Use this ConsulRegistrator if you don't have or don't want to
 * run a separate Consul agent process on the same box where this hazelcast
 * enabled application runs. This ConsulRegistrator will register this 
 * hazelcast instance with Consul as a service and also optional
 * configure a health-check script if you defined it. 
 * 
 * It will also de-register the service if invoked to do so.
 * 
 * Custom options (specified as JSON value for the 'consul-registrator-config')
 * 	
 *   			           
 *	 - preferPublicAddress (true|false) : use the public IP determined by
 *	 								      hazelcast (if not null) over the private IP
 *	 								      
 *	 - healthCheckScript: can be anything you want Consul to do to determine health. 
 *	                      Variables #MYIP/#MYPORT will be replaced 
 *	                      @see https://www.consul.io/docs/agent/checks.html
 *	                      
 *	 - healthCheckScriptIntervalSeconds: self explanatory
 *   			           		 
 * @author bitsofinfo
 *
 */
public class LocalDiscoveryNodeRegistrator implements ConsulRegistrator {
	
	// variables for health-script template support
	private static final String HEALTH_SCRIPT_TEMPLATE_MYPORT = "#MYPORT";
	private static final String HEALTH_SCRIPT_TEMPLATE_MYIP = "#MYIP";
	
	// properties that are supported in the JSON value for the 'consul-registrator-config' config property
	public static final String CONFIG_PROP_PREFER_PUBLIC_ADDRESS = "preferPublicAddress";
	public static final String CONFIG_PROP_HEALTH_CHECK_SCRIPT = "healthCheckScript";
	public static final String CONFIG_PROP_HEALTH_CHECK_SCRIPT_INTERVAL_SECONDS = "healthCheckScriptIntervalSeconds";

	private ILogger logger = null;
	private Address myLocalAddress = null;
	private String[] tags = null;
	private String consulServiceName = null;
	private String consulHost = null;
	private Integer consulPort = null;
	private String healthCheckScript = null;
	private Long healthCheckScriptIntervalSeconds = null;
	
	private String myServiceId = null;
	
	private AgentClient consulAgentClient;

	@Override
	public void init(String consulHost, 
			         Integer consulPort,
			         String consulServiceName,
			         String[] consulTags,
			         DiscoveryNode localDiscoveryNode,
			         Map<String, Object> registratorConfig,
			         ILogger logger) throws Exception {
		
		this.logger = logger;
		this.tags = consulTags;
		this.consulHost = consulHost;
		this.consulPort = consulPort;
		this.consulServiceName = consulServiceName;
		
		
		try {
			/**
			 * Determine my local address
			 */
			this.myLocalAddress = localDiscoveryNode.getPrivateAddress();
			 
			Object usePublicAddress = (Object)registratorConfig.get(CONFIG_PROP_PREFER_PUBLIC_ADDRESS);
			if (usePublicAddress != null && usePublicAddress instanceof Boolean && (Boolean)usePublicAddress) {
				logger.info("Registrator config property: " + CONFIG_PROP_PREFER_PUBLIC_ADDRESS +":"+usePublicAddress + " attempting to use it...");
				Address publicAddress = localDiscoveryNode.getPublicAddress();
				if (publicAddress != null) {
					myLocalAddress = publicAddress;
				}
			}
			
			logger.info("Determined local DiscoveryNode address to use: " + myLocalAddress);
			
			
			/**
			 * Deal with health check script
			 */
			String rawScript = (String)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_SCRIPT);
			if (rawScript != null && !rawScript.trim().isEmpty()) {
				this.healthCheckScriptIntervalSeconds = Long.valueOf((Integer)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_SCRIPT_INTERVAL_SECONDS));
				this.healthCheckScript = rawScript.replaceAll(HEALTH_SCRIPT_TEMPLATE_MYIP, myLocalAddress.getInetAddress().getHostAddress())
												  .replaceAll(HEALTH_SCRIPT_TEMPLATE_MYPORT, String.valueOf(myLocalAddress.getPort()));
			}
			
			
			// build my Consul agent client that we will register with
			this.consulAgentClient =  Consul.builder().withHostAndPort(
													HostAndPort.fromParts(consulHost, consulPort))
												.build().agentClient();
			
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
			
			if (this.healthCheckScript != null) {
				builder.check(Registration.RegCheck.script(this.healthCheckScript, this.healthCheckScriptIntervalSeconds));
			}
			
			// register...
			this.consulAgentClient.register(builder.build());
			
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


}
