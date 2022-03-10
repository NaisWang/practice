package com.zhouyu.service;

import com.spring.Component;
import com.spring.InitializingBean;

/**
 * @author : whz
 */
@Component("orderService")
public class OrderService implements InitializingBean {
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("OrderService初始化");
	}
}
