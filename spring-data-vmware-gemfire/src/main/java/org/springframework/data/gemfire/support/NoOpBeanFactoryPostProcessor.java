/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * The {@link NoOpBeanFactoryPostProcessor} class is a Spring {@link BeanFactoryPostProcessor} implementation
 * that performs no operation.
 *
 * @author John Blum
 * @see BeanFactoryPostProcessor
 * @see ConfigurableListableBeanFactory
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class NoOpBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	public static final NoOpBeanFactoryPostProcessor INSTANCE = new NoOpBeanFactoryPostProcessor();

	private NoOpBeanFactoryPostProcessor() { }

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException { }

}
