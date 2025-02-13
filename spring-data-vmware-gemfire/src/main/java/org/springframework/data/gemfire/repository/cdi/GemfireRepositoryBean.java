/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.repository.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.apache.geode.cache.Region;

import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactory;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.data.repository.config.CustomRepositoryImplementationDetector;

/**
 * A CDI-based bean that represents a GemFire Repository.
 *
 * @author John Blum
 * @author Mark Paluch
 * @param <T> class type of the Repository.
 * @see javax.enterprise.context.spi.CreationalContext
 * @see javax.enterprise.inject.spi.Bean
 * @see javax.enterprise.inject.spi.BeanManager
 * @see GemfireMappingContext
 * @see GemfireRepositoryFactory
 * @see CdiRepositoryBean
 * @see CustomRepositoryImplementationDetector
 * @see Region
 * @since 1.8.0
 */
@SuppressWarnings("rawtypes")
class GemfireRepositoryBean<T> extends CdiRepositoryBean<T> {

	static final GemfireMappingContext DEFAULT_GEMFIRE_MAPPING_CONTEXT = new GemfireMappingContext();

	private final Bean<GemfireMappingContext> gemfireMappingContextBean;

	private final BeanManager beanManager;

	private final Set<Bean<Region>> regionBeans;

	GemfireRepositoryBean(BeanManager beanManager, Class<T> repositoryType, Set<Annotation> qualifiers,
			CustomRepositoryImplementationDetector detector, Bean<GemfireMappingContext> gemfireMappingContextBean,
			Set<Bean<Region>> regionBeans) {

		super(qualifiers, repositoryType, beanManager, Optional.ofNullable(detector));

		this.beanManager = beanManager;
		this.gemfireMappingContextBean = gemfireMappingContextBean;
		this.regionBeans = regionBeans;
	}

	/**
	 * Returns an instance of the given {@link Bean} from the container.
	 *
	 * @param <S> the actual {@link Type class type} of the {@link Bean}.
	 * @param bean the {@link Bean} defining the instance to create.
	 * @param type the expected component type of the instance created from the {@link Bean}.
	 * @return an instance of the given {@link Bean}.
	 * @see javax.enterprise.inject.spi.BeanManager#getReference(javax.enterprise.inject.spi.Bean, Type, javax.enterprise.context.spi.CreationalContext)
	 * @see javax.enterprise.inject.spi.Bean
	 * @see Type
	 */
	@SuppressWarnings("unchecked")
	protected <S> S getDependencyInstance(Bean<S> bean, Type type) {
		return (S) this.beanManager.getReference(bean, type, this.beanManager.createCreationalContext(bean));
	}

	/**
	 * Resolves the desired, actual component type from the {@link Bean} in which the instance is created.
	 *
	 * @param <S> the class type of the component.
	 * @param bean the {@link Bean} from which the types are evaluated and an instance is created.
	 * @param targetType the desired class type of the component.
	 * @return a resolved component {@link Type} of the {@link Bean}instance.
	 * @throws IllegalStateException if the desired class type cannot be resolved.
	 * @see javax.enterprise.inject.spi.Bean#getTypes()
	 * @see Class
	 */
	protected <S> Type resolveType(Bean<S> bean, Class<S> targetType) {

		for (Type type : bean.getTypes()) {

			Type assignableType = (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);

			if (assignableType instanceof Class && targetType.isAssignableFrom((Class<?>) assignableType)) {
				return type;
			}
		}

		throw new IllegalStateException(String.format(
			"unable to resolve bean instance of type [%1$s] from bean definition [%2$s]",
				targetType, bean));
	}

	Iterable<Region<?, ?>> resolveGemfireRegions() {

		Set<Region<?, ?>> regions = new HashSet<>(regionBeans.size());

		for (Bean<Region> regionBean : regionBeans) {
			regions.add(getDependencyInstance(regionBean, resolveType(regionBean, Region.class)));
		}

		return regions;
	}

	GemfireMappingContext resolveGemfireMappingContext() {
		return (gemfireMappingContextBean != null
			? getDependencyInstance(gemfireMappingContextBean, GemfireMappingContext.class)
				: DEFAULT_GEMFIRE_MAPPING_CONTEXT);
	}

	GemfireRepositoryFactory newGemfireRepositoryFactory() {
		return new GemfireRepositoryFactory(resolveGemfireRegions(), resolveGemfireMappingContext());
	}

	/**
	 * Creates an instance of the given Repository type as a bean instance in the CDI container.
	 *
	 * @param creationalContext operations used by the {@link javax.enterprise.context.spi.Contextual} implementation
	 * during creation of the bean instance.
	 * @param repositoryType the actual class type of the SD (GemFire) Repository.
	 * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)
	 * @see #newGemfireRepositoryFactory()
	 */
	@Override
	protected T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {
		return create(this::newGemfireRepositoryFactory, repositoryType);
	}
}
