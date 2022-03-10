package com.zhouyu.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.Scope;

import java.util.stream.BaseStream;

/**
 * @author : whz
 */
@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware {

	@Autowired
	private OrderService orderService;

	private String beanName;

	private String name;

	@Override
	public void setBeanName(String name) {
		beanName = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void test() {
		System.out.println(orderService);
		System.out.println(beanName);
		System.out.println(name);
	}

}
