/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.wan.GatewayEventFilter;
import org.apache.geode.cache.wan.GatewayEventSubstitutionFilter;
import org.apache.geode.cache.wan.GatewayQueueEvent;
import org.apache.geode.cache.wan.GatewaySender;
import org.apache.geode.cache.wan.GatewayTransportFilter;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.wan.GatewaySenderFactoryBean;
import org.springframework.data.gemfire.wan.OrderPolicyType;
import org.springframework.mock.env.MockPropertySource;

/**
 * Integration Tests for {@link EnableGatewaySender} and {@link EnableGatewaySenders} to test the configuration of
 * {@link GatewaySender} using properties.
 *
 * @author Udo Kohlmeyer
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.GatewaySenderConfiguration
 * @see org.springframework.data.gemfire.config.annotation.GatewaySenderConfigurer
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.wan.GatewaySenderFactoryBean
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 2.2.0
 */
public class GatewaySenderPropertiesIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

	private ConfigurableApplicationContext newApplicationContext(PropertySource<?> testPropertySource,
		Class<?>... annotatedClasses) {

		Function<ConfigurableApplicationContext, ConfigurableApplicationContext> applicationContextInitializer =
			applicationContext -> {

				MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();

				propertySources.addFirst(testPropertySource);

				return applicationContext;
			};

		return newApplicationContext(applicationContextInitializer, annotatedClasses);
	}

	@After
	public void cleanupAfterTests() {
		destroyAllGemFireMockObjects();
	}

	@Test
	public void gatewayReceiverPropertiesConfigurationOnMultipleChildren() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.manual-start", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.remote-distributed-system-id", 2)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-synchronous", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-conflation-enabled", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.parallel", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.persistent", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.order-policy", "PARTITION")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.alert-threshold", 1234)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-size", 100)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-time-interval", 2000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.dispatcher-threads", 22)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.maximum-queue-memory", 400)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.socket-buffer-size", 16384)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.socket-read-timeout", 4000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.transport-filters",
				"transportBean2, transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.regions", "Region1,Region2")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.manual-start", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.remote-distributed-system-id", 3)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.disk-synchronous", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-conflation-enabled", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.parallel", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.persistent", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.order-policy", "KEY")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.alert-threshold", 4321)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-size", 1000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-time-interval", 20000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.dispatcher-threads", 2200)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.maximum-queue-memory", 40000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.socket-buffer-size", 1638400)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.socket-read-timeout", 400000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.transport-filters",
				"transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.regions", "Region1");

		newApplicationContext(testPropertySource, BaseGatewaySenderTestConfiguration.class,
			TestConfigurationWithPropertiesMultipleGatewaySenders.class);

		TestGatewaySenderConfigurer gatewaySenderConfigurer = getBean(TestGatewaySenderConfigurer.class);

		GatewaySender gatewaySender = getBean("TestGatewaySender", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(true);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(2);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(22);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(true);
		assertThat(gatewaySender.isParallel()).isEqualTo(true);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(false);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.PARTITION);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(1234);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(100);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(2000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(400);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(16384);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(2);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean2", "transportBean1" });

		gatewaySender = getBean("TestGatewaySender2", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(false);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(3);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender2");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(2200);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(false);
		assertThat(gatewaySender.isParallel()).isEqualTo(false);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(true);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.KEY);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(4321);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(1000);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(20000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(40000);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(400000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(1638400);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(1);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean1" });

		Region<?, ?> region1 = getBean("Region1", Region.class);
		Region<?, ?> region2 = getBean("Region2", Region.class);

		assertThat(region1.getAttributes().getGatewaySenderIds())
			.containsExactlyInAnyOrder("TestGatewaySender", "TestGatewaySender2");
		assertThat(region2.getAttributes().getGatewaySenderIds()) .containsExactlyInAnyOrder("TestGatewaySender");
	}

	@Test
	public void gatewayReceiverPropertiesConfigurationOnMultipleChildrenAndAnnotations() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.gateway.sender.socket-read-timeout", 4000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.manual-start", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.remote-distributed-system-id", 2)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-synchronous", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-conflation-enabled", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.parallel", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.persistent", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.order-policy", "THREAD")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.alert-threshold", 1234)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-size", 1020)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-time-interval", 2300)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.dispatcher-threads", 22)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.maximum-queue-memory", 400)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.socket-buffer-size", 16384)

			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.transport-filters",
				"transportBean2, transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.regions", "Region1,Region2")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.manual-start", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.remote-distributed-system-id", 3)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.disk-synchronous", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-conflation-enabled", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.parallel", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.persistent", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.order-policy", "KEY")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.alert-threshold", 4321)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-size", 1000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.batch-time-interval", 20000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.dispatcher-threads", 2200)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.maximum-queue-memory", 40000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.socket-buffer-size", 1638400)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.transport-filters",
				"transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender2.regions", "");

		newApplicationContext(testPropertySource, BaseGatewaySenderTestConfiguration.class,
			TestConfigurationWithMultipleGatewaySenderAnnotations.class);

		TestGatewaySenderConfigurer gatewaySenderConfigurer = getBean(TestGatewaySenderConfigurer.class);

		GatewaySender gatewaySender = getBean("TestGatewaySender", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(true);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(2);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(22);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(true);
		assertThat(gatewaySender.isParallel()).isEqualTo(true);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(false);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.THREAD);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(1234);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(1020);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(2300);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(400);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(16384);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(2);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean2", "transportBean1" });

		gatewaySender = getBean("TestGatewaySender2", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(false);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(3);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender2");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(2200);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(false);
		assertThat(gatewaySender.isParallel()).isEqualTo(false);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(true);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.KEY);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(4321);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(1000);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(20000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(40000);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(1638400);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(1);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean1" });

		Region<?, ?> region1 = getBean("Region1", Region.class);
		Region<?, ?> region2 = getBean("Region2", Region.class);

		assertThat(region1.getAttributes().getGatewaySenderIds())
			.containsExactlyInAnyOrder("TestGatewaySender", "TestGatewaySender2");
		assertThat(region2.getAttributes().getGatewaySenderIds())
			.containsExactlyInAnyOrder("TestGatewaySender", "TestGatewaySender2");
	}

	@Test
	public void gatewayReceiverPropertiesConfigurationOnChild() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.manual-start", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.remote-distributed-system-id", 2)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-synchronous", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-conflation-enabled", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.parallel", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.persistent", false)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.order-policy", "PARTITION")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.alert-threshold", 1234)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-size", 100)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.batch-time-interval", 2000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.dispatcher-threads", 22)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.maximum-queue-memory", 400)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.socket-buffer-size", 16384)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.socket-read-timeout", 4000)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.transport-filters",
				"transportBean2, transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.regions", "Region1,Region2");

		newApplicationContext(testPropertySource, BaseGatewaySenderTestConfiguration.class,
			TestConfigurationWithProperties.class);

		TestGatewaySenderConfigurer gatewaySenderConfigurer = getBean(TestGatewaySenderConfigurer.class);

		GatewaySender gatewaySender = getBean("TestGatewaySender", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(true);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(2);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(22);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(true);
		assertThat(gatewaySender.isParallel()).isEqualTo(true);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(false);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.PARTITION);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(1234);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(100);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(2000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(400);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(16384);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(2);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean2", "transportBean1" });

		Region<?, ?> region1 = getBean("Region1", Region.class);
		Region<?, ?> region2 = getBean("Region2", Region.class);

		assertThat(region1.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
		assertThat(region2.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
	}

	@Test
	public void gatewayReceiverPropertiesConfigurationOnParent() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.gateway.sender.manual-start", true)
			.withProperty("spring.data.gemfire.gateway.sender.remote-distributed-system-id", 2)
			.withProperty("spring.data.gemfire.gateway.sender.disk-synchronous", true)
			.withProperty("spring.data.gemfire.gateway.sender.batch-conflation-enabled", true)
			.withProperty("spring.data.gemfire.gateway.sender.parallel", true)
			.withProperty("spring.data.gemfire.gateway.sender.persistent", false)
			.withProperty("spring.data.gemfire.gateway.sender.order-policy", "PARTITION")
			.withProperty("spring.data.gemfire.gateway.sender.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.alert-threshold", 1234)
			.withProperty("spring.data.gemfire.gateway.sender.batch-size", 100)
			.withProperty("spring.data.gemfire.gateway.sender.batch-time-interval", 2000)
			.withProperty("spring.data.gemfire.gateway.sender.dispatcher-threads", 22)
			.withProperty("spring.data.gemfire.gateway.sender.maximum-queue-memory", 400)
			.withProperty("spring.data.gemfire.gateway.sender.socket-buffer-size", 16384)
			.withProperty("spring.data.gemfire.gateway.sender.socket-read-timeout", 4000)
			.withProperty("spring.data.gemfire.gateway.sender.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.transport-filters",
				"transportBean2, transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.regions", "Region1,Region2");

		newApplicationContext(testPropertySource, BaseGatewaySenderTestConfiguration.class,
			TestConfigurationWithProperties.class);

		TestGatewaySenderConfigurer gatewaySenderConfigurer = getBean(TestGatewaySenderConfigurer.class);

		GatewaySender gatewaySender = getBean("TestGatewaySender", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(true);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(2);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(22);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(true);
		assertThat(gatewaySender.isParallel()).isEqualTo(true);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(false);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.PARTITION);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(1234);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(100);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(2000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(400);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(16384);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(2);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean2", "transportBean1" });

		Region<?, ?> region1 = getBean("Region1", Region.class);
		Region<?, ?> region2 = getBean("Region2", Region.class);

		assertThat(region1.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
		assertThat(region2.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
	}

	@Test
	public void gatewayReceiverPropertiesConfigurationOnParentWithChildOverride() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.gateway.sender.manual-start", true)
			.withProperty("spring.data.gemfire.gateway.sender.TestGatewaySender.manual-start", false)
			.withProperty("spring.data.gemfire.gateway.sender.remote-distributed-system-id", 2)
			.withProperty("spring.data.gemfire.gateway.sender.disk-synchronous", true)
			.withProperty("spring.data.gemfire.gateway.sender.batch-conflation-enabled", true)
			.withProperty("spring.data.gemfire.gateway.sender.parallel", true)
			.withProperty("spring.data.gemfire.gateway.sender.persistent", false)
			.withProperty("spring.data.gemfire.gateway.sender.order-policy", "PARTITION")
			.withProperty("spring.data.gemfire.gateway.sender.event-substitution-filter",
				"SomeEventSubstitutionFilter")
			.withProperty("spring.data.gemfire.gateway.sender.alert-threshold", 1234)
			.withProperty("spring.data.gemfire.gateway.sender.batch-size", 100)
			.withProperty("spring.data.gemfire.gateway.sender.batch-time-interval", 2000)
			.withProperty("spring.data.gemfire.gateway.sender.dispatcher-threads", 22)
			.withProperty("spring.data.gemfire.gateway.sender.maximum-queue-memory", 400)
			.withProperty("spring.data.gemfire.gateway.sender.socket-buffer-size", 16384)
			.withProperty("spring.data.gemfire.gateway.sender.socket-read-timeout", 4000)
			.withProperty("spring.data.gemfire.gateway.sender.disk-store-reference", "someDiskStore")
			.withProperty("spring.data.gemfire.gateway.sender.event-filters", "SomeEventFilter")
			.withProperty("spring.data.gemfire.gateway.sender.transport-filters",
				"transportBean2, transportBean1")
			.withProperty("spring.data.gemfire.gateway.sender.regions", "Region1,Region2");

		newApplicationContext(testPropertySource, BaseGatewaySenderTestConfiguration.class,
			TestConfigurationWithProperties.class);

		TestGatewaySenderConfigurer gatewaySenderConfigurer = getBean(TestGatewaySenderConfigurer.class);

		GatewaySender gatewaySender = getBean("TestGatewaySender", GatewaySender.class);

		assertThat(gatewaySender.isManualStart()).isEqualTo(false);
		assertThat(gatewaySender.getRemoteDSId()).isEqualTo(2);
		assertThat(gatewaySender.getId()).isEqualTo("TestGatewaySender");
		assertThat(gatewaySender.getDispatcherThreads()).isEqualTo(22);
		assertThat(gatewaySender.isBatchConflationEnabled()).isEqualTo(true);
		assertThat(gatewaySender.isParallel()).isEqualTo(true);
		assertThat(gatewaySender.isPersistenceEnabled()).isEqualTo(false);
		assertThat(gatewaySender.getDiskStoreName()).isEqualTo("someDiskStore");
		assertThat(gatewaySender.getOrderPolicy()).isEqualTo(GatewaySender.OrderPolicy.PARTITION);
		assertThat(((TestGatewayEventSubstitutionFilter) gatewaySender.getGatewayEventSubstitutionFilter()).name)
			.isEqualTo("SomeEventSubstitutionFilter");
		assertThat(gatewaySender.getAlertThreshold()).isEqualTo(1234);
		assertThat(gatewaySender.getBatchSize()).isEqualTo(100);
		assertThat(gatewaySender.getBatchTimeInterval()).isEqualTo(2000);
		assertThat(gatewaySender.getMaximumQueueMemory()).isEqualTo(400);
		assertThat(gatewaySender.getSocketReadTimeout()).isEqualTo(4000);
		assertThat(gatewaySender.getSocketBufferSize()).isEqualTo(16384);

		assertThat(gatewaySender.getGatewayTransportFilters().size()).isEqualTo(2);
		assertThat(gatewaySenderConfigurer.beanNames.get(gatewaySender.getId()).toArray())
			.isEqualTo(new String[] { "transportBean2", "transportBean1" });

		Region<?, ?> region1 = getBean("Region1", Region.class);
		Region<?, ?> region2 = getBean("Region2", Region.class);

		assertThat(region1.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
		assertThat(region2.getAttributes().getGatewaySenderIds()).containsExactlyInAnyOrder("TestGatewaySender");
	}

	@EnableGatewaySenders(gatewaySenders = {
		@EnableGatewaySender(name = "TestGatewaySender", manualStart = true, remoteDistributedSystemId = 2,
			diskSynchronous = true, batchConflationEnabled = true, parallel = true, persistent = false,
			diskStoreReference = "someDiskStore", orderPolicy = OrderPolicyType.PARTITION, alertThreshold = 1234, batchSize = 100,
			batchTimeInterval = 2000, dispatcherThreads = 22, maximumQueueMemory = 400, socketBufferSize = 16384,
			socketReadTimeout = 4000, regions = { "Region1", "Region2" }),
		@EnableGatewaySender(name = "TestGatewaySender2", manualStart = true, remoteDistributedSystemId = 2,
			diskSynchronous = true, batchConflationEnabled = true, parallel = true, persistent = false,
			diskStoreReference = "someDiskStore", orderPolicy = OrderPolicyType.PARTITION, alertThreshold = 1234, batchSize = 100,
			batchTimeInterval = 2000, dispatcherThreads = 22, maximumQueueMemory = 400, socketBufferSize = 16384,
			socketReadTimeout = 4000, regions = { "Region1", "Region2" })
	})
	static class TestConfigurationWithMultipleGatewaySenderAnnotations { }

	@SuppressWarnings("unused")
	@EnableGatewaySender(name = "TestGatewaySender")
	static class TestConfigurationWithProperties {

		@Bean("gatewayConfigurer")
		GatewaySenderConfigurer gatewaySenderConfigurer() {
			return new TestGatewaySenderConfigurer();
		}
	}

	@EnableGatewaySenders(gatewaySenders = {
		@EnableGatewaySender(name = "TestGatewaySender"),
		@EnableGatewaySender(name = "TestGatewaySender2")
	})
	static class TestConfigurationWithPropertiesMultipleGatewaySenders { }

	private static class TestGatewaySenderConfigurer implements GatewaySenderConfigurer {

		private final Map<String, List<?>> beanNames = new TreeMap<>();

		@Override
		public void configure(String beanName, GatewaySenderFactoryBean bean) {

			this.beanNames.put(beanName, bean.getTransportFilters().stream()
				.map(transportFilter -> ((TestGatewayTransportFilter) transportFilter).name)
				.collect(Collectors.toList()));
		}
	}

	@SuppressWarnings("rawtypes")
	private static class TestGatewayEventSubstitutionFilter implements GatewayEventSubstitutionFilter {

		private final String name;

		public TestGatewayEventSubstitutionFilter(String name) {
			this.name = name;
		}

		@Override
		public Object getSubstituteValue(EntryEvent entryEvent) {
			return null;
		}

		@Override
		public void close() { }

	}

	private static class TestGatewayEventFilter implements GatewayEventFilter {

		private final String name;

		public TestGatewayEventFilter(String name) {
			this.name = name;
		}

		@Override
		public boolean beforeEnqueue(GatewayQueueEvent gatewayQueueEvent) {
			return false;
		}

		@Override
		public boolean beforeTransmit(GatewayQueueEvent gatewayQueueEvent) {
			return false;
		}

		@Override
		public void afterAcknowledgement(GatewayQueueEvent gatewayQueueEvent) { }

		@Override
		public String toString() {
			return this.name;
		}
	}

	private static class TestGatewayTransportFilter implements GatewayTransportFilter {

		private final String name;

		public TestGatewayTransportFilter(String name) {
			this.name = name;
		}

		@Override
		public InputStream getInputStream(InputStream inputStream) {
			return null;
		}

		@Override
		public OutputStream getOutputStream(OutputStream outputStream) {
			return null;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		@SuppressWarnings("all")
		public boolean equals(Object obj) {
			return this.name.equals(((TestGatewayTransportFilter) obj).name);
		}
	}

	@PeerCacheApplication
	@EnableGemFireMockObjects
	@SuppressWarnings({ "rawtypes", "unused" })
	static class BaseGatewaySenderTestConfiguration {

		@Bean("Region1")
		PartitionedRegionFactoryBean createRegion1(GemFireCache gemFireCache) {
			return createRegion("Region1", gemFireCache);
		}

		@Bean("Region2")
		PartitionedRegionFactoryBean createRegion2(GemFireCache gemFireCache) {
			return createRegion("Region2", gemFireCache);
		}

		@Bean("gatewayConfigurer")
		GatewaySenderConfigurer gatewaySenderConfigurer() {
			return new TestGatewaySenderConfigurer();
		}

		@Bean("transportBean1")
		GatewayTransportFilter createGatewayTransportBean1() {
			return new TestGatewayTransportFilter("transportBean1");
		}

		@Bean("transportBean2")
		GatewayTransportFilter createGatewayTransportBean2() {
			return new TestGatewayTransportFilter("transportBean2");
		}

		@Bean("SomeEventFilter")
		GatewayEventFilter createGatewayEventFilter() {
			return new TestGatewayEventFilter("SomeEventFilter");
		}

		@Bean("SomeEventSubstitutionFilter")
		GatewayEventSubstitutionFilter createGatewayEventSubstitutionFilter() {
			return new TestGatewayEventSubstitutionFilter("SomeEventSubstitutionFilter");
		}

		public PartitionedRegionFactoryBean createRegion(String name, GemFireCache gemFireCache) {

			PartitionedRegionFactoryBean regionFactoryBean = new PartitionedRegionFactoryBean();
			regionFactoryBean.setCache(gemFireCache);
			regionFactoryBean.setDataPolicy(DataPolicy.PARTITION);
			regionFactoryBean.setName(name);

			return regionFactoryBean;
		}
	}
}
