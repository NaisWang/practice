package com.spring;

/**
 * @author : whz
 */
public interface BeanPostProcessor {

	Object postProcessBeforeInitialization(Object bean, String beanName);

	Object postProcessAfterInitialization(Object bean, String beanName);

}
