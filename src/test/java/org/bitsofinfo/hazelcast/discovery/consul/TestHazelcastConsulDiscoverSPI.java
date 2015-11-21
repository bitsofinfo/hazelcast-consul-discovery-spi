package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.HostAndPort;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.ServiceHealth;

/**
 * Tests for the Hazelcast Consul Discovery SPI strategy
 * 
 * @author bitsofinfo
 *
 */
public class TestHazelcastConsulDiscoverSPI {
	
	public static final String CONSUL_HOST = "localhost";
	public static final int CONSUL_PORT = 8500;

	/**
	 * Tests LocalDiscoveryNodeRegistrator functionality
	 * 
	 * Multithreaded
	 * 
	 */
	@Test
	public void testLocalDiscoveryNodeRegistrator() {
		
		try {
			int totalInstancesToTest = 5;
			List<HazelcastInstanceMgr> instances = new ArrayList<HazelcastInstanceMgr>();
			
			System.out.println("#################### IS CONSUL RUNNING @ " +
					CONSUL_HOST+":"+CONSUL_PORT+"? IF NOT THIS TEST WILL FAIL! ####################");
			
			CatalogClient consulCatalogClient = Consul.builder().withHostAndPort(
					HostAndPort.fromParts("localhost", 8500))
				.build().catalogClient();
	
	
			HealthClient consulHealthClient = Consul.builder().withHostAndPort(
					HostAndPort.fromParts("localhost", 8500))
				.build().healthClient();
			
			for (int i=0; i<totalInstancesToTest; i++) {
				HazelcastInstanceMgr mgr = new HazelcastInstanceMgr("test-LocalDiscoveryNodeRegistrator.xml");
				instances.add(mgr);
				mgr.start();
				
			}
			
			Thread.currentThread().sleep(20000);
			
			// validate we have 5 registered...(regardless of health)
			ConsulResponse<List<CatalogService>> response = consulCatalogClient.getService("test-LocalDiscoveryNodeRegistrator");
			Assert.assertEquals(totalInstancesToTest,response.getResponse().size());
			
			// validate we have 5 healthy
			ConsulResponse<List<ServiceHealth>> response2 = consulHealthClient.getHealthyServiceInstances("test-LocalDiscoveryNodeRegistrator");
			Assert.assertEquals(totalInstancesToTest,response2.getResponse().size());
			
			// shutdown one node
			instances.iterator().next().shutdown();
			
			// let consul healthcheck fail
			Thread.currentThread().sleep(45000);
			
			// healthy is total -1 now...
			response2 = consulHealthClient.getHealthyServiceInstances("test-LocalDiscoveryNodeRegistrator");
			Assert.assertEquals((totalInstancesToTest-1),response2.getResponse().size());
			
			
			// shutdown everything
			for (HazelcastInstanceMgr instance : instances) {
				instance.shutdown();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			Assert.assertFalse("Unexpected error in test: " + e.getMessage(),false);
		}
		
	}
	
	
	private class HazelcastInstanceMgr {
		
		private HazelcastInstance hazelcastInstance = null;
		private Config conf = null;
		
		public HazelcastInstanceMgr(String hazelcastConfigFile) {
			this.conf =new ClasspathXmlConfig(hazelcastConfigFile);
		}
		
		public void start() {
			hazelcastInstance = Hazelcast.newHazelcastInstance(conf);
		}
		
		public void shutdown() {
			this.hazelcastInstance.shutdown();
		}
		
		public Address getAddress() {
			return this.hazelcastInstance.getCluster().getLocalMember().getAddress();
		}
		
	}
}
