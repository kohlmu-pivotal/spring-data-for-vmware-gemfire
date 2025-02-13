/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.data.gemfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The {@link EnableClusterDefinedRegions} annotation marks a Spring {@link Configuration @Configuration} application
 * annotated class to enable the creation of client Proxy-based {@link Region Regions} for all {@link Region Regions}
 * defined in an Apache Geode/Pivotal GemFire cluster.
 *
 * @author John Blum
 * @see Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see Configuration
 * @see Import
 * @see ClusterDefinedRegionsConfiguration
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(ClusterDefinedRegionsConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableClusterDefinedRegions {

	/**
	 * Configures the client {@link Region} data management policy for all client {@link Region Regions} created from
	 * the corresponding server-side {@link Region}.
	 *
	 * Defaults to {@link ClientRegionShortcut#PROXY}.
	 *
	 * @see ClientRegionShortcut
	 */
	ClientRegionShortcut clientRegionShortcut() default ClientRegionShortcut.PROXY;

}
