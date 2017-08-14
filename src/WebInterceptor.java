package com.company.core.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.company.core.base.utils.CookieUtil;
import com.company.core.base.utils.JwtUtil;
import com.company.global.conf.SysConf;
import com.company.server.base.interceptor.BaseWebInterceptor;

/**
 * @author chenmc
 * 
 */
public class WebInterceptor extends BaseWebInterceptor {

	/**
	 * 刷新token
	 */
	@Override
	protected boolean refreshTokenHandler(HttpServletRequest request, HttpServletResponse response, String token) {
		if (JwtUtil.withinRefreshTime(token, SysConf.JWT_REFRESH_TTL)) {
			response.setStatus(210);
			CookieUtil.addCookieSess(response, JwtUtil.TOKENKEY, token);
			return true;
		}
		return false;
	}
	
	
}
