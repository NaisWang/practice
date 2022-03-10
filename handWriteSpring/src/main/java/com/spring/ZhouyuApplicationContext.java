package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : whz
 */
public class ZhouyuApplicationContext {

	private Class configClass;

	// 单例池
	private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

	private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

	private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

	public ZhouyuApplicationContext(Class configClass) {
		this.configClass = configClass;

		// 解析配置类
		scan(configClass);

		for (String beanName : beanDefinitionMap.keySet()) {
			BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
			if (beanDefinition.getScope().equals("singleton")) {
				// 创建单例Bean, 并放入单例池中
				Object bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
		}
	}

	public Object createBean(String beanName, BeanDefinition beanDefinition) {
		Class clazz = beanDefinition.getClazz();
		try {
			Object instance = clazz.getDeclaredConstructor().newInstance();

			// 处理依赖注入@Autowired
			// 1.遍历该类所有成员变量
			for (Field declaredField : clazz.getDeclaredFields()) {
				// 2.判断该成员变量是否含有Autowired注解
				if (declaredField.isAnnotationPresent(Autowired.class)) {
					// 3. 若有Autowired注解，则给该成员变量赋值
					Object bean = getBean(declaredField.getName());
					declaredField.setAccessible(true);
					declaredField.set(instance, bean);
				}
			}

			// Aware回调，设置BeanName
			// 1. 判断该类是否实现了BeanNameAware接口
			if (instance instanceof BeanNameAware) {
				// 2. 若该类是实现了BeanNameAware接口, 则设置BeanName
				((BeanNameAware) instance).setBeanName(beanName);
			}

			// 调用BeanPostProcessor初始化前的方法
			for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
				instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
			}

			// InitializingBean初始化
			// 1. 判断该类是否实现类InitializingBean接口
			if (instance instanceof InitializingBean) {
				// 2. 若该类是实现了BeanNameAware接口, 则设置BeanName
				try {
					((InitializingBean) instance).afterPropertiesSet();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 调用BeanPostProcessor初始化后的方法
			for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
				instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
			}

			return instance;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析配置上的ComponentScan注解 --> 拿到扫描路径 --> 进行扫描 --> 将扫描到的Component类封装成BeanDefinition --> 存入BeanDefinitionMap中
	 *
	 * @param configClass 配置类
	 */
	private void scan(Class configClass) {
		// 1. 解析配置上的ComponentScan注解
		ComponentScan componentCanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
		// 2. 拿到扫描路径
		String path = componentCanAnnotation.value();
		path = path.replace(".", "/");
		// 3. 进行扫描
		// 3.1 拿到对应的应用程序类加载器
		ClassLoader classLoader = ZhouyuApplicationContext.class.getClassLoader();
		// 3.2 通过类加载器拿到资源
		URL resource = classLoader.getResource(path);
		File file = new File(resource.getFile());

		// 3.3 遍历资源
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				String fileName = f.getAbsolutePath();
				// 判断文件是否为class文件
				if (fileName.endsWith(".class")) {
					// 将D:\IdeaProjects\Spring-Zhouyu\target\classes\com\zhouyu\service\UserService.class文件路径 转换成 com.zhouyu.service.UserService,即将文件路径转换成类全限定名
					String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
					className = className.replace("/", ".");
					try {
						// 根据类全限定名加载类
						Class<?> clazz = classLoader.loadClass(className);
						// 判断该类是否有Component注解
						if (clazz.isAnnotationPresent(Component.class)) {

							// 判断该类是否实现了BeanPostProcessor接口
							if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
								// 如果是则将其实力化，并添加到beanPostProcessorList中
								BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
								beanPostProcessorList.add(instance);
							}

							// 解析类并将其封装为一个BeanDefinition
							Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
							String beanName = componentAnnotation.value();
							//创建一个BeanDefinition
							BeanDefinition beanDefinition = new BeanDefinition();
							beanDefinition.setClazz(clazz);
							if (clazz.isAnnotationPresent(Scope.class)) {
								Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
								beanDefinition.setScope(scopeAnnotation.value());
							} else {
								beanDefinition.setScope("singleton");
							}
							//将得到的beanDefinition存入BeanDefinitionMap中
							beanDefinitionMap.put(beanName, beanDefinition);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public Object getBean(String beanName) {
		if (beanDefinitionMap.containsKey(beanName)) {
			// 根据beanName获取BeanDefinition
			BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
			// 判断该Bean是否单例Bean
			if (beanDefinition.getScope().equals("singleton")) {
				// 当前Bean是单例Bean时，从单例池中获取Bean对象
				Object o = singletonObjects.get(beanName);
				return o;
			} else {
				// 当前Bean不是单例Bean时，则创建一个Bean对象
				Object bean = createBean(beanName, beanDefinition);
				return bean;
			}
		} else {
			// 不存在对应的bean，抛出异常
			throw new NullPointerException();
		}
	}

}
