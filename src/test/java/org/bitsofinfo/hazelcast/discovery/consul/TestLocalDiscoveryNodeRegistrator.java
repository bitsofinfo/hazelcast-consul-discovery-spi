package org.bitsofinfo.hazelcast.discovery.consul;

import org.junit.Test;

/**
 * Tests for the Hazelcast Consul Discovery SPI strategy
 * 
 * @author bitsofinfo
 *
 */
public class TestLocalDiscoveryNodeRegistrator extends RegistratorTestBase {
	
	/**
	 * Tests LocalDiscoveryNodeRegistrator functionality
	 * 
	 */
	@Test
	public void testLocalDiscoveryNodeRegistrator() {
		testRegistrator("test-LocalDiscoveryNodeRegistrator.xml","test-LocalDiscoveryNodeRegistrator");
	}

	@Override
	protected void preConstructHazelcast(int instanceNumber) throws Exception {
		String ip = super.determineIpAddress();
		
		// these variables are subsituted for the ${vars} in the hazelcast config XML 
		System.setProperty("hz.public.address.ip", ip);
		System.setProperty("hz.public.address.port", "570"+(instanceNumber+1));
		
	}
	
}
