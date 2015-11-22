package org.bitsofinfo.hazelcast.discovery.consul;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.HostAndPort;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.Address;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.ServiceHealth;

/**
 * Base test class for the Hazelcast Consul Discovery SPI strategies
 * writing registrators
 * 
 * @author bitsofinfo
 *
 */
public abstract class RegistratorTestBase {
	
	public static final String CONSUL_HOST = "localhost";
	public static final int CONSUL_PORT = 8500;
	
	protected abstract void preConstructHazelcast(int instanceNumber);

	protected void testRegistrator(String hazelcastConfigXmlFilename, String serviceName) {
		
		try {
			
			IMap<Object,Object> testMap1 = null;
			IMap<Object,Object> testMap2 = null;
			
			int totalInstancesToTest = 5;
			List<HazelcastInstanceMgr> instances = new ArrayList<HazelcastInstanceMgr>();
			
			System.out.println("#################### IS CONSUL RUNNING @ " +
					CONSUL_HOST+":"+CONSUL_PORT+"? IF NOT THIS TEST WILL FAIL! ####################");
			
			CatalogClient consulCatalogClient = Consul.builder().withHostAndPort(
					HostAndPort.fromParts(CONSUL_HOST, CONSUL_PORT))
				.build().catalogClient();
	
			HealthClient consulHealthClient = Consul.builder().withHostAndPort(
					HostAndPort.fromParts(CONSUL_HOST, CONSUL_PORT))
				.build().healthClient();
			
			for (int i=0; i<totalInstancesToTest; i++) {
				
				preConstructHazelcast(i);
				
				HazelcastInstanceMgr mgr = new HazelcastInstanceMgr(hazelcastConfigXmlFilename);
				instances.add(mgr);
				mgr.start();
				
				// create testMap1 in first instance and populate it w/ 10 entries
				if (i == 0) {
					testMap1 = mgr.getInstance().getMap("testMap1");
					for(int j=0; j<10; j++) {
						testMap1.put(j, j);
					}
				}
				
			}
			
			Thread.currentThread().sleep(20000);
			
			// validate we have 5 registered...(regardless of health)
			ConsulResponse<List<CatalogService>> response = consulCatalogClient.getService(serviceName);
			Assert.assertEquals(totalInstancesToTest,response.getResponse().size());
			
			// validate we have 5 healthy
			ConsulResponse<List<ServiceHealth>> response2 = consulHealthClient.getHealthyServiceInstances(serviceName);
			Assert.assertEquals(totalInstancesToTest,response2.getResponse().size());
			
			// get the map via each instance and 
			// validate it ensuring they are all talking to one another
			for (HazelcastInstanceMgr mgr : instances) {
				Assert.assertEquals(10, mgr.getInstance().getMap("testMap1").size());
			}
			
			// pick random instance add new map, verify its everywhere
			Random rand = new Random();
			testMap2 = instances.get(rand.nextInt(instances.size()-1)).getInstance().getMap("testMap2");
			for(int j=0; j<10; j++) {
				testMap2.put(j, j);
			}
			
			for (HazelcastInstanceMgr mgr : instances) {
				Assert.assertEquals(10, mgr.getInstance().getMap("testMap2").size());
			}
		
			
			// shutdown one node
			HazelcastInstanceMgr deadInstance = instances.iterator().next();
			deadInstance.shutdown();
			
			// let consul healthcheck fail
			Thread.currentThread().sleep(60000);
			
			// healthy is total -1 now...
			response2 = consulHealthClient.getHealthyServiceInstances(serviceName);
			Assert.assertEquals((totalInstancesToTest-1),response2.getResponse().size());
			
			// pick a random instance, add some entries in map, verify
			instances.get(rand.nextInt(instances.size()-1)).getInstance().getMap("testMap2").put("extra1", "extra1");
			
			// should be 11 now
			for (HazelcastInstanceMgr mgr : instances) {
				if (mgr != deadInstance) {
					Assert.assertEquals((10+1), mgr.getInstance().getMap("testMap2").size());
				}
			}
			
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
		
		public HazelcastInstance getInstance() {
			return hazelcastInstance;
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
