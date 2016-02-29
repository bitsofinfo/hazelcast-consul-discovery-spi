package org.bitsofinfo.hazelcast.discovery.consul;

/**
 * Enum for consul configuration for unit test
 * 
 * @author bmudda
 *
 */
public enum ConsulConfig {
	
	CONSUL_HOST("consulHost"),
	CONSUL_PORT("consulPort"),
	CONSUL_ACL_TOKEN("consulAclToken"),
	CONSUL_SSL_ENABLED("consulSslEnabled"),
	CONSUL_SSL_SERVER_CERT_FILE_PATH("consulSslServerCertFilePath"),
	CONSUL_SSL_SERVER_CERT_BASE64("consulSslServerCertBase64"),
	CONSUL_SSL_SERVER_HOSTNAME_VERIFY("consulSslServerHostnameVerify"),
	CONSUL_HEALTH_CHECK_PROVIDER("consulHealthCheckProvider");
	
	private String value;
	
	private ConsulConfig(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public static ConsulConfig fromString(String text){
		
		if(text != null){
			for(ConsulConfig config : ConsulConfig.values()){
				
				if(text.equalsIgnoreCase(config.name())){
					return config;
				}
			}
		}
		
		throw new IllegalArgumentException("No constant with text " + text + " found");
	}

}
