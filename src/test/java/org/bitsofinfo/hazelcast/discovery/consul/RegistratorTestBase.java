package org.bitsofinfo.hazelcast.discovery.consul;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;

import com.google.common.net.HostAndPort;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
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

	public String consulHost;
	public int consulPort;
	public String consulAclToken;
	public boolean consulSslEnabled;
	public String consulSslServerCertFilePath;
	public String consulSslServerCertBase64;
	public boolean consulSslServerHostnameVerify;
	public String consulHealthCheckProvider;
	
	private static final ILogger logger = Logger.getLogger(RegistratorTestBase.class);
	
	protected abstract void preConstructHazelcast(int instanceNumber) throws Exception;

	protected void testRegistrator(String hazelcastConfigXmlFilename, String serviceName) {
		
		try {
			
			initSystemProps();
			
			IMap<Object,Object> testMap1 = null;
			IMap<Object,Object> testMap2 = null;
			
			int totalInstancesToTest = 5;
			List<HazelcastInstanceMgr> instances = new ArrayList<HazelcastInstanceMgr>();
			
			System.out.println("#################### IS CONSUL RUNNING @ " +
					consulHost+":"+consulPort+"? IF NOT THIS TEST WILL FAIL! ####################");
			
			
			ConsulBuilder builder = ConsulClientBuilder.class.newInstance();
			Consul consul = builder.buildConsul(consulHost, 
												consulPort, 
												consulSslEnabled, 
												consulSslServerCertFilePath, 
												consulSslServerCertBase64, 
												consulSslServerHostnameVerify);
			
			CatalogClient consulCatalogClient = consul.catalogClient();
	
			HealthClient consulHealthClient = consul.healthClient();
			
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
			ConsulResponse<List<CatalogService>> response = consulCatalogClient.getService(serviceName, ConsulUtility.getAclToken(consulAclToken));
			Assert.assertEquals(totalInstancesToTest,response.getResponse().size());
			
			// validate we have 5 healthy
			ConsulResponse<List<ServiceHealth>> response2 = consulHealthClient.getHealthyServiceInstances(serviceName, ConsulUtility.getAclToken(consulAclToken));
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
			response2 = consulHealthClient.getHealthyServiceInstances(serviceName, ConsulUtility.getAclToken(consulAclToken));
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
	
	protected String determineIpAddress() throws Exception {
		
		InetAddress addr = InetAddress.getLocalHost();
		String ipAdd = addr.getHostAddress();
		
		return ipAdd;

	}
	
	
	protected void initSystemProps(){
		
		consulHost = System.getProperty(ConsulConfig.CONSUL_HOST.getValue(), "localhost");
		consulPort = Integer.valueOf(System.getProperty(ConsulConfig.CONSUL_PORT.getValue(), "8500"));
		consulAclToken = System.getProperty(ConsulConfig.CONSUL_ACL_TOKEN.getValue(), "");
		consulSslEnabled = Boolean.valueOf(System.getProperty(ConsulConfig.CONSUL_SSL_ENABLED.getValue(), "false"));
		consulSslServerCertFilePath = System.getProperty(ConsulConfig.CONSUL_SSL_SERVER_CERT_FILE_PATH.getValue(), "");
		consulSslServerCertBase64 = System.getProperty(ConsulConfig.CONSUL_SSL_SERVER_CERT_BASE64.getValue(), "");
		consulSslServerHostnameVerify = Boolean.valueOf(System.getProperty(ConsulConfig.CONSUL_SSL_SERVER_HOSTNAME_VERIFY.getValue(), "false"));
		consulHealthCheckProvider = System.getProperty(ConsulConfig.CONSUL_HEALTH_CHECK_PROVIDER.getValue(), "org.bitsofinfo.hazelcast.discovery.consul.ScriptHealthCheckBuilder");
		
		System.setProperty(ConsulConfig.CONSUL_HOST.getValue(),consulHost);
		System.setProperty(ConsulConfig.CONSUL_PORT.getValue(),String.valueOf(consulPort));
		System.setProperty(ConsulConfig.CONSUL_ACL_TOKEN.getValue(),consulAclToken);
		System.setProperty(ConsulConfig.CONSUL_SSL_ENABLED.getValue(),String.valueOf(consulSslEnabled));
		System.setProperty(ConsulConfig.CONSUL_SSL_SERVER_CERT_FILE_PATH.getValue(),consulSslServerCertFilePath);
		System.setProperty(ConsulConfig.CONSUL_SSL_SERVER_CERT_BASE64.getValue(),consulSslServerCertBase64);
		System.setProperty(ConsulConfig.CONSUL_SSL_SERVER_HOSTNAME_VERIFY.getValue(),String.valueOf(consulSslServerHostnameVerify));
		System.setProperty(ConsulConfig.CONSUL_HEALTH_CHECK_PROVIDER.getValue(),consulHealthCheckProvider);
		
		
		System.out.println("***** USING SYSTEM PARAMS *****");
		System.out.println("consulHost : " + consulHost);
		System.out.println("consulPort : " + consulPort);
		System.out.println("consulAclToken : " + consulAclToken);
		System.out.println("consulSslEnabled : " + consulSslEnabled);
		System.out.println("consulSslServerCertFilePath : " + consulSslServerCertFilePath);
		System.out.println("consulSslServerCertBase64 : " + consulSslServerCertBase64);
		System.out.println("consulSslServerHostnameVerify : " + consulSslServerHostnameVerify);
		System.out.println("consulHealthCheckProvider : " + consulHealthCheckProvider);
		
		
		
	}
}
