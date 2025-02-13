/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.repository.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.ProcessBean;

import org.apache.geode.cache.Region;

import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.data.repository.cdi.CdiRepositoryExtensionSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GemfireRepositoryExtension class...
 *
 * @author John Blum
 * @see CdiRepositoryExtensionSupport
 * @since 1.8.0
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class GemfireRepositoryExtension extends CdiRepositoryExtensionSupport {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	final Map<Set<Annotation>, Bean<GemfireMappingContext>> mappingContexts = new HashMap<>();

	final Set<Bean<Region>> regionBeans = new HashSet<>();

	public GemfireRepositoryExtension() {
		logger.info("Activating CDI extension for Spring Data Geode Repositories");
	}

	/**
	 * Implementation of an observer that captures GemFire Region beans defined in the CDI container, storing them
	 * along with any defined GemfireMappingContexts for later construction of the Repository beans.
	 *
	 * @param <X> class type of the bean instance.
	 * @param processBean annotated type as defined by CDI.
	 * @see jakarta.enterprise.inject.spi.ProcessBean
	 * @see jakarta.enterprise.event.Observes
	 */
	@SuppressWarnings("unchecked")
	<X> void processBean(@Observes ProcessBean<X> processBean) {

		Bean<X> bean = processBean.getBean();

		for (Type type : bean.getTypes()) {

			Type resolvedType = type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type;

			if (resolvedType instanceof Class<?>) {

				Class<?> classType = (Class<?>) resolvedType;

				if (Region.class.isAssignableFrom(classType)) {

					logger.debug("Found Region bean with name {}", bean.getName());

					this.regionBeans.add((Bean<Region>) bean);
				}
				else if (GemfireMappingContext.class.isAssignableFrom(classType)) {

					logger.debug("Discovered {} bean with types {} having qualifiers {}",
						GemfireMappingContext.class.getName(), bean.getTypes(), bean.getQualifiers());

					this.mappingContexts.put(bean.getQualifiers(), (Bean<GemfireMappingContext>) bean);
				}
			}
		}
	}

	/**
	 * Implementation of an observer that registers beans in the CDI container for the detected Spring Data
	 * Repositories.
	 *
	 * Repository beans are associated to the appropriate GemfireMappingContexts based on their qualifiers.
	 *
	 * @param beanManager the BeanManager instance.
	 * @see jakarta.enterprise.inject.spi.AfterBeanDiscovery
	 * @see jakarta.enterprise.inject.spi.BeanManager
	 * @see jakarta.enterprise.event.Observes
	 */
	void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

		for (Map.Entry<Class<?>, Set<Annotation>> entry : getRepositoryTypes()) {

			Class<?> repositoryType = entry.getKey();

			Set<Annotation> qualifiers = entry.getValue();

			// Create the bean representing the Repository.
			CdiRepositoryBean<?> repositoryBean = createRepositoryBean(beanManager, repositoryType, qualifiers);

			logger.info("Registering bean for '{}' with qualifiers {}.", repositoryType.getName(), qualifiers);

			// Register the bean with the extension and the container.
			registerBean(repositoryBean);
			afterBeanDiscovery.addBean(repositoryBean);
		}
	}

	<T> CdiRepositoryBean<T> createRepositoryBean(BeanManager beanManager, Class<T> repositoryType,
			Set<Annotation> qualifiers) {

		// Determine the GemfireMappingContext bean matching the qualifiers of the Repository.
		Bean<GemfireMappingContext> gemfireMappingContextBean = this.mappingContexts.get(qualifiers);

		// Construct and return a GemfireRepositoryBean.
		return new GemfireRepositoryBean<>(beanManager, repositoryType, qualifiers, getCustomImplementationDetector(),
			gemfireMappingContextBean, this.regionBeans);
	}
}
