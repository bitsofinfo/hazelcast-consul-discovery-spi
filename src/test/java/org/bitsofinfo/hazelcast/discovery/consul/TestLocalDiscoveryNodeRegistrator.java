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
	protected void preConstructHazelcast(int instanceNumber) {
		// we do nothing
		
	}
	
}
