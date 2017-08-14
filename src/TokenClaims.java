package com.company.core.base.entity;

import java.util.Map;

import com.company.core.base.utils.FileUtil;
import com.company.core.base.utils.JsonUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

/**
 * 自定义tokenClaims,因为token过期会导致无法解析，所以手动解析返回TokenClaims
 * 
 * @author chenmc
 * @date 2017年5月10日 下午2:38:00
 */
public class TokenClaims extends DefaultClaims {

	private static TokenClaims instance;
	
	private TokenClaims() {
        super();
    }
	
	private TokenClaims(Map<String, Object> map) {
        super(map);
    }
	
	/**
	 * 创建TokenClaims实体，使用单例模式，用id判断是否为同一个token
	 * 
	 * @author chenmc
	 * @date 2017年5月11日 上午11:00:19
	 * @param token
	 * @return
	 */
	public static synchronized TokenClaims getInstance(String token) {
		Map<String, Object> map = getClaimsMap(token);
        if (instance != null) {
        	String id = (String)map.get(Claims.ID);
        	if (instance.getId().equals(id))
        		return instance;
        }
        instance = new TokenClaims(map);   
        return instance;   
    }   
	
	/**
	 * 获取token中的字段信息
	 * 
	 * @author chenmc
	 * @date 2017年5月10日 下午2:51:55
	 * @param token
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> getClaimsMap(String token) {
		String[] parts = token.split("\\.");
		String headers = new String(FileUtil.fromBase64(parts[0]));
		String payload = new String(FileUtil.fromBase64(parts[1]));
		@SuppressWarnings("unchecked")
		Map<String, Object> headersMap = JsonUtil.fromJson(headers, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> payloadMap = JsonUtil.fromJson(payload, Map.class);
		headersMap.putAll(payloadMap);
		return headersMap;
	}
	
}
