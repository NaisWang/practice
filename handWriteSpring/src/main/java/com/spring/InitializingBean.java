package com.spring;

/**
 * @author : whz
 */
public interface InitializingBean {
	void afterPropertiesSet() throws Exception;
}
