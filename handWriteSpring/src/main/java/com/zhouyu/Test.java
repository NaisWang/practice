package com.zhouyu;

import com.spring.ZhouyuApplicationContext;
import com.zhouyu.service.UserService;
import com.zhouyu.service.UserServiceImpl;
import com.zhouyu.service.UserServiceInterface;

public class Test {
	public static void main(String[] args) {
		ZhouyuApplicationContext applicationContext = new ZhouyuApplicationContext(AppConfig.class);
		UserService userService = (UserService) applicationContext.getBean("userService");
		userService.test();

		// 测试AOP
		UserServiceInterface userServiceInterface = (UserServiceInterface) applicationContext.getBean("userServiceImpl");
		userServiceInterface.test();
	}
}
