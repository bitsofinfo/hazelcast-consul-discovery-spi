package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Arrays;
import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.cluster.Address;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.agent.Registration.RegCheck;

/**
 * Implementation of a script based health check builder
 * 
 * @author bmudda
 *
 */
public class ScriptHealthCheckBuilder implements HealthCheckBuilder {

	// variables for health-script template support
	private static final String HEALTH_SCRIPT_TEMPLATE_MYPORT = "#MYPORT";
	private static final String HEALTH_SCRIPT_TEMPLATE_MYIP = "#MYIP";
	
	// standard properties that are supported in the JSON value for the 'consul-registrator-config' config property
	public static final String CONFIG_PROP_HEALTH_CHECK_SCRIPT = "healthCheckScript";
	public static final String CONFIG_PROP_HEALTH_CHECK_SCRIPT_INTERVAL_SECONDS = "healthCheckScriptIntervalSeconds";
		
	private static final ILogger logger = Logger.getLogger(ScriptHealthCheckBuilder.class);	
		
	@Override
	public RegCheck buildRegistrationCheck(Map<String, Object> registratorConfig, Address localAddress) {
		
		RegCheck regCheck = null;
		
		try{
			/**
			 * Deal with health check script
			 */
			String rawScript = (String)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_SCRIPT);
			
			if (rawScript != null && !rawScript.trim().isEmpty()) {
				
				Long healthCheckScriptIntervalSeconds = Long.valueOf((Integer)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_SCRIPT_INTERVAL_SECONDS));
				String healthCheckScript = rawScript.replaceAll(HEALTH_SCRIPT_TEMPLATE_MYIP, localAddress.getInetAddress().getHostAddress())
												    .replaceAll(HEALTH_SCRIPT_TEMPLATE_MYPORT, String.valueOf(localAddress.getPort()));
			
				regCheck = Registration.RegCheck.args(Arrays.asList(healthCheckScript.split(" ")), healthCheckScriptIntervalSeconds);
			}
		
		}catch(Exception e){
			logger.severe("Unexpected error occured trying to build HTTP health check : " + e.getMessage(), e);
		}
		
		return regCheck;
		
		
		
	}

}
