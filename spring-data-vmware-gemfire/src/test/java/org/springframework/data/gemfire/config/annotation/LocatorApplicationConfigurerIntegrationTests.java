/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.LocatorFactoryBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for {@link LocatorApplication} and {@link LocatorConfigurer}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.config.annotation.LocatorApplication
 * @see org.springframework.data.gemfire.config.annotation.LocatorApplicationConfiguration
 * @see org.springframework.data.gemfire.config.annotation.LocatorConfigurer
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 2.2.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class LocatorApplicationConfigurerIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	private LocatorFactoryBean locatorFactoryBean;

	@Autowired
	@Qualifier("locatorConfigurerOne")
	private LocatorConfigurer locatorConfigurerOne;

	@Autowired
	@Qualifier("locatorConfigurerTwo")
	private LocatorConfigurer locatorConfigurerTwo;

	@Test
	public void locatorConfigurersInvoked() {

		assertThat(this.locatorFactoryBean).isNotNull();
		assertThat(this.locatorConfigurerOne).isNotNull();
		assertThat(this.locatorConfigurerTwo).isNotNull();

		Arrays.asList(this.locatorConfigurerOne, this.locatorConfigurerTwo).forEach(locatorConfigurer ->
			verify(locatorConfigurer, times(1))
				.configure(eq("locatorApplication"), eq(this.locatorFactoryBean)));
	}

	@EnableGemFireMockObjects
	@LocatorApplication(logLevel = GEMFIRE_LOG_LEVEL, port = 0)
	static class TestConfiguration {

		@Bean
		BeanPostProcessor locatorFactoryBeanPostProcessor() {

			return new BeanPostProcessor() {

				@Nullable @Override
				public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

					if (bean instanceof LocatorFactoryBean) {

						LocatorFactoryBean locatorFactoryBean = spy((LocatorFactoryBean) bean);

						doNothing().when(locatorFactoryBean).init();

						bean = locatorFactoryBean;

					}

					return bean;
				}
			};
		}

		@Bean
		LocatorConfigurer locatorConfigurerOne() {
			return mock(LocatorConfigurer.class);
		}

		@Bean
		LocatorConfigurer locatorConfigurerTwo() {
			return mock(LocatorConfigurer.class);
		}
	}
}
