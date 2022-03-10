package com.zhouyu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;
import com.zhouyu.service.UserService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author : whz
 */
@Component("zhouyuBeanPostProcessor")
public class ZhouyuBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		System.out.println(beanName + "初始化前");
		if (beanName.equals("userService")) {
			((UserService) bean).setName("测试");
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		System.out.println(beanName + "初始化后");

		// 模拟spring中的AOP
		if (beanName.equals("userServiceImpl")) {
			Object proxyInstance = Proxy.newProxyInstance(ZhouyuBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					System.out.println("代理逻辑");
					return method.invoke(bean, args);
				}
			});
			// 返回代理对象
			return proxyInstance;
		}
		return bean;
	}
}
