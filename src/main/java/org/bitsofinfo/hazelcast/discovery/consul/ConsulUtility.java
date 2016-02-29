package org.bitsofinfo.hazelcast.discovery.consul;

import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
/**
 * Utility Class
 * 
 * @author bmudda
 *
 */
public class ConsulUtility {
	
	/**
	 * Method to build an ACL token in a query option.
	 * 
	 * @param token
	 * @return QueryOption
	 */
	public static QueryOptions getAclToken(String token){
		
		if(token == null || token.trim().isEmpty()){
			return ImmutableQueryOptions.BLANK;
		}
		
		//ACL token for registering as a service on consul, check health and get service catalog
		ImmutableQueryOptions.Builder optionBuilder = ImmutableQueryOptions.builder().token(token);
		return optionBuilder.build();
	}

}
