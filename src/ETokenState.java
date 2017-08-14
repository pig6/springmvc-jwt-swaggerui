package com.company.core.base.enums;

import java.util.Locale;

import com.company.core.base.utils.I18N;

/**
 * token验证状态
 * 
 * @author chenmc
 */
public enum ETokenState {

	expired("expired"), //过期的
	invalid("invalid"), //无效的
	valid("valid");  //有效的
	
	private final String value;

	private ETokenState(String v) {
		this.value = v;
	}

	public String toString() {
		return this.value;
	}

	public static ETokenState get(String value) {
		for (ETokenState e : values()) {
			if (e.toString().equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public String getName() {
		return I18N.getEnumName(this, Locale.CHINA);
	}
	
	public String getName(Locale locale) {
		return I18N.getEnumName(this, locale);
	}
}
