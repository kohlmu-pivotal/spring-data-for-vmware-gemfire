/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocatorFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.support.GemfireBeanFactoryLocator;
import org.springframework.lang.NonNull;

/**
 * The {@link BeanFactoryLocatorConfiguration} class extends the Spring application configuration by enabling
 * Spring Data GemFire/Geode's {@link GemfireBeanFactoryLocator} in order to auto-wire and configure any
 * GemFire/Geode Objects declared in GemFire/Geode config (e.g. XML or properties).
 *
 * @author John Blum
 * @see BeanPostProcessor
 * @see Bean
 * @see Configuration
 * @see CacheFactoryBean
 * @see LocatorFactoryBean
 * @see ClientCacheFactoryBean
 * @see ClientCacheConfigurer
 * @see LocatorConfigurer
 * @see PeerCacheConfigurer
 * @see EnableBeanFactoryLocator
 * @see GemfireBeanFactoryLocator
 * @since 2.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class BeanFactoryLocatorConfiguration {

	/**
	 * Declares and registers a Spring {@link BeanPostProcessor} bean to post process a Spring Data Geode
	 * {@link CacheFactoryBean} or {@link ClientCacheFactoryBean} by setting the {@literal useBeanFactoryLocator}
	 * property to {@literal true}.
	 *
	 * @return a Spring {@link BeanPostProcessor} used to post process an SDG {@link CacheFactoryBean}.
	 * @see BeanPostProcessor
	 */
	@Bean
	public BeanPostProcessor useBeanFactoryLocatorBeanPostProcessor() {

		return new BeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof CacheFactoryBean) {
					((CacheFactoryBean) bean).setUseBeanFactoryLocator(true);
				}

				if (bean instanceof LocatorFactoryBean) {
					((LocatorFactoryBean) bean).setUseBeanFactoryLocator(true);
				}

				return bean;
			}
		};
	}

	/**
	 * Declares and registers a {@link ClientCacheConfigurer} bean to configure a {@link ClientCacheFactoryBean}
	 * by setting the {@literal useBeanFactoryLocator} property to {@literal true}.
	 *
	 * @return a {@link ClientCacheConfigurer} used to configure and set the SDG {@link ClientCacheFactoryBean}
	 * {@literal useBeanFactoryLocator} property to {@literal true}.
	 * @see ClientCacheConfigurer
	 */
	@Bean
	public @NonNull ClientCacheConfigurer useBeanFactoryLocatorClientCacheConfigurer() {
		return (beanName, bean) -> bean.setUseBeanFactoryLocator(true);
	}

	/**
	 * Declares and registers a {@link LocatorConfigurer} bean to configure a {@link LocatorFactoryBean}
	 * by setting the {@literal useBeanFactoryLocator} property to {@literal true}.
	 *
	 * @return a {@link LocatorConfigurer} used to configure and set the SDG {@link LocatorFactoryBean}
	 * {@literal useBeanFactoryLocator} property to {@literal true}.
	 * @see LocatorConfigurer
	 */
	@Bean
	public @NonNull LocatorConfigurer useBeanFactoryLocatorLocatorConfigurer() {
		return (beanName, bean) -> bean.setUseBeanFactoryLocator(true);
	}

	/**
	 * Declares and registers a {@link PeerCacheConfigurer} bean to configure a {@link CacheFactoryBean}
	 * by setting the {@literal useBeanFactoryLocator} property to {@literal true}.
	 *
	 * @return a {@link PeerCacheConfigurer} used to configure and set the SDG {@link CacheFactoryBean}
	 * {@literal useBeanFactoryLocator} property to {@literal true}.
	 * @see PeerCacheConfigurer
	 */
	@Bean
	public @NonNull PeerCacheConfigurer useBeanFactoryLocatorPeerCacheConfigurer() {
		return (beanName, bean) -> bean.setUseBeanFactoryLocator(true);
	}
}
