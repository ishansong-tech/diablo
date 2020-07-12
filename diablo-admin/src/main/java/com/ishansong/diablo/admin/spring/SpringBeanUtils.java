package com.ishansong.diablo.admin.spring;

import org.springframework.context.ConfigurableApplicationContext;

public final class SpringBeanUtils {

    private static final SpringBeanUtils INSTANCE = new SpringBeanUtils();

    private ConfigurableApplicationContext cfgContext;

    private SpringBeanUtils() {
        if (INSTANCE != null) {
            throw new Error("error");
        }
    }

    public static SpringBeanUtils getInstance() {
        return INSTANCE;
    }

    public <T> T getBean(final Class<T> type) {
        return cfgContext.getBean(type);
    }

    public <T> T getBean(final String beanName) {
        return (T)cfgContext.getBean(beanName);
    }

    public <T> T getBean(final String beanName,Class<T> clazz) {
        return cfgContext.getBean(beanName,clazz);
    }

    public void registerBean(final String beanName, final Object obj) {
        cfgContext.getBeanFactory().registerSingleton(beanName, obj);
    }

    public void setCfgContext(final ConfigurableApplicationContext cfgContext) {
        this.cfgContext = cfgContext;
    }
}
