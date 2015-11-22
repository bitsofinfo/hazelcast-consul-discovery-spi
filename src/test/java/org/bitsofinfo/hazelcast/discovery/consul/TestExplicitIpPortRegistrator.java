package org.bitsofinfo.hazelcast.discovery.consul;

import org.junit.Test;

/**
 * Tests for the Hazelcast Consul Discovery SPI strategy
 * 
 * @author bitsofinfo
 *
 */
public class TestExplicitIpPortRegistrator extends RegistratorTestBase {
	
	/**
	 * Tests ExplicitIpPortRegistrator functionality
	 * 
	 * NOTE: Adjust the IP variable as appropriate 
	 * for your local box this test is running on
	 * 
	 */
	@Test
	public void testExplicitIpPortRegistrator() {
		testRegistrator("test-ExplicitIpPortRegistrator.xml","test-ExplicitIpPortRegistrator");
	}

	@Override
	protected void preConstructHazelcast(int instanceNumber) {
		String ip = "192.168.0.208";
		
		// these variables are subsituted for the ${vars} in the hazelcast config XML 
		System.setProperty("registerWithIp", ip);
		System.setProperty("registerWithPort","570"+(instanceNumber+1)); // start at 5701 - 5705
		
	}
	
}
