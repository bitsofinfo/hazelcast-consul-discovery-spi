package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Address;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.agent.Registration.RegCheck;

/**
 * Implementation of the health check builder for HTTP health check
 * 
 * @author bmudda
 *
 */
public class HttpHealthCheckBuilder implements HealthCheckBuilder {

	public static final String CONFIG_PROP_HEALTH_CHECK_HTTP = "healthCheckHttp";
	public static final String CONFIG_PROP_HEALTH_CHECK_HTTP_INTERVAL_SECONDS = "healthCheckHttpIntervalSeconds";
	
	private static final ILogger logger = Logger.getLogger(HttpHealthCheckBuilder.class);
	
	@Override
	public RegCheck buildRegistrationCheck( Map<String, Object> registratorConfig, Address localAddress) {
		
		RegCheck regCheck = null;
		
		try{
			/**
			 * Deal with health check http
			 */
			String healthCheckHttp = (String)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_HTTP);
			
			if (healthCheckHttp != null && !healthCheckHttp.trim().isEmpty()) {
				
				Long healthCheckHttpIntervalSeconds = Long.valueOf((Integer)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_HTTP_INTERVAL_SECONDS));
				regCheck = Registration.RegCheck.http(healthCheckHttp, healthCheckHttpIntervalSeconds);
			}
		
		}catch(Exception e){
			logger.severe("Unexpected error occured trying to build HTTP health check : " + e.getMessage(), e);

		}
		
		return regCheck;
	}

}
