package com.company.core.base.utils;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.company.core.base.conf.BaseSysConf;
import com.company.core.base.entity.TokenClaims;
import com.company.core.base.enums.ETokenState;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil extends BaseUtil {

	private static Logger log = LoggerFactory.getLogger(JwtUtil.class);
	public static final String TOKENKEY = "x-access-token";
	
	/**
	 * 由字符串生成加密key
	 * 
	 * @return
	 */
	public static SecretKey generalKey() {
		String stringKey = BaseSysConf.JWT_SECRET;
		byte[] encodedKey = Base64.getEncoder().encode(stringKey.getBytes());
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * 创建jwt,设置过期时间
	 * 
	 * @param userId
	 * @param ttlMillis
	 * @return
	 */
	public static String createJWT(String userId, long ttlMillis) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = DateUtil.Now();
		Date now = new Date(nowMillis);
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().setId(GlobalUtil.getUUID()).setIssuedAt(now).setSubject(userId).signWith(signatureAlgorithm, key);
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}
		return builder.compact();
	}

	/**
	 * 解密jwt
	 * 
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String token) {
		SecretKey key = generalKey();
		Claims claims = null;
		try {
			claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			claims = TokenClaims.getInstance(token);//TokenClaims 自定义类
			log.debug("the user({}) token expired in ({}) ", claims.getSubject(), DateUtil.parseDate(claims.getExpiration(), DateUtil.FORMAT_DATETIME_CHINA));
		} catch (Exception e2) {
			logException(log, e2);
		}
		return claims;
	}

	/**
	 * 验证token
	 * 
	 * @author chenmc
	 * @date 2017年8月10日 上午10:39:41
	 * @param request
	 * @return
	 */
	public static ETokenState validateJWT(HttpServletRequest request) {
		return validateJWT(getTokenFromRequest(request));
	}
	
	/**
	 * 验证token
	 * 
	 * @param token
	 * @return
	 */
	public static ETokenState validateJWT(String token) {
		if (StrUtil.isEmpty(token)) {
			return ETokenState.invalid;
		}
		Claims claims = parseJWT(token);
		if (claims == null || null == claims.getExpiration()) {
			return ETokenState.invalid;
		} else if (claims.getExpiration().before(new Date())) {
			return ETokenState.expired;
		}
		return ETokenState.valid;
	}
	
	/**
	 * token在刷新期
	 * 
	 * @author chenmc
	 * @date 2017年5月9日 下午3:45:48
	 * @param token
	 * @param refreshTime
	 * @return
	 */
	public static boolean withinRefreshTime(String token, Long refreshTime) {
		Claims claims = parseJWT(token);
		if (claims == null)
			return false;
		return DateUtil.NowTime().minus(refreshTime).isBefore(claims.getExpiration().getTime());
	}
	
	/**
	 * 刷新token
	 * 
	 * @author chenmc
	 * @date 2017年5月9日 上午11:38:30
	 * @param token
	 * @return
	 */
	public static String refreshToken(String token, long ttlMillis) {
		String userUid = parseJWT(token).getSubject();
		return createJWT(userUid, ttlMillis);
	}
	
	
	/**
	 * 获取request里的token并取出useruid
	 * 
	 * @author chenmc
	 * @date 2017年8月10日 上午10:50:57
	 * @param token
	 * @return
	 */
	public static String getUseruid(HttpServletRequest request) {
		String token = getTokenFromRequest(request);
		if (token == null)
			return null;
		return parseJWT(token).getSubject();
	}
	
	/**
	 * 获取token里的useruid
	 * 
	 * @author chenmc
	 * @date 2017年8月10日 上午10:50:57
	 * @param token
	 * @return
	 */
	public static String getUseruid(String token) {
		return parseJWT(token).getSubject();
	}
	
	/**
	 * 从request的header里获取token
	 * 
	 * @author chenmc
	 * @date 2017年8月10日 上午10:37:52
	 * @param request
	 * @return
	 */
	public static String getTokenFromRequest(HttpServletRequest request) {
		if (request == null)
			return null;
		return request.getHeader(JwtUtil.TOKENKEY);
	}
}
