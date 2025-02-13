/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.support;

import org.springframework.web.client.RestTemplate;

/**
 * Configurer for a {@link RestTemplate}.
 *
 * @author John Blum
 * @see RestTemplate
 * @since 2.3.0
 */
@FunctionalInterface
public interface RestTemplateConfigurer {

	/**
	 * User-defined method and contract for applying custom configuration to the given {@link RestTemplate}.
	 *
	 * @param restTemplate {@link RestTemplate} to customize the configuration for.
	 * @see RestTemplate
	 */
	void configure(RestTemplate restTemplate);

}
