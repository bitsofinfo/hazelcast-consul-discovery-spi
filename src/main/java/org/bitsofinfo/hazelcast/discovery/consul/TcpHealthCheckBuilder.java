package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.Map;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.cluster.Address;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.agent.Registration.RegCheck;

/**
 * Implementation of the health check builder for TCP health check
 * 
 * @author bmudda
 *
 */
public class TcpHealthCheckBuilder implements HealthCheckBuilder {

	// variables for http template support
	private static final String TCP_TEMPLATE_MYPORT = "#MYPORT";
	private static final String TCP_TEMPLATE_MYIP = "#MYIP";

	public static final String CONFIG_PROP_HEALTH_CHECK_TCP = "healthCheckTcp";
	public static final String CONFIG_PROP_HEALTH_CHECK_TCP_INTERVAL_SECONDS = "healthCheckTcpIntervalSeconds";
	
	private static final ILogger logger = Logger.getLogger(TcpHealthCheckBuilder.class);
		
	@Override
	public RegCheck buildRegistrationCheck(Map<String, Object> registratorConfig, Address localAddress) {
		
		RegCheck regCheck = null;
		
		try {
			/**
			 * Deal with health check tcp
			 */
			String healthCheckTcp = (String)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_TCP);
			
			if (healthCheckTcp != null && !healthCheckTcp.trim().isEmpty()) {
				
				healthCheckTcp = healthCheckTcp.replaceAll(TCP_TEMPLATE_MYIP, localAddress.getInetAddress().getHostAddress())
						  						 .replaceAll(TCP_TEMPLATE_MYPORT, String.valueOf(localAddress.getPort()));
				
				Long healthCheckTcpIntervalSeconds = Long.valueOf((Integer)registratorConfig.get(CONFIG_PROP_HEALTH_CHECK_TCP_INTERVAL_SECONDS));
				regCheck = Registration.RegCheck.tcp(healthCheckTcp, healthCheckTcpIntervalSeconds);
			}
			
		} catch(Exception e) {
			logger.severe("Unexpected error occured trying to build TCP health check : " + e.getMessage(), e);
		}
		
		return regCheck;
	}

	
	
}
