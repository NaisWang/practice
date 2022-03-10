package com.zhouyu.service;

import com.spring.Component;

/**
 * @author : whz
 */
@Component("userServiceImpl")
public class UserServiceImpl implements UserServiceInterface {
	@Override
	public void test() {
		System.out.println("UserServiceImpl test...");
	}
}
