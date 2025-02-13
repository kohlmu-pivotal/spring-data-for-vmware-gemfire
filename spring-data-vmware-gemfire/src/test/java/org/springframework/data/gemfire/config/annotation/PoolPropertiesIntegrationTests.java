/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.annotation;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.SocketFactory;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.mock.env.MockPropertySource;

import java.net.InetSocketAddress;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration Tests for {@link EnablePool} and {@link EnablePools}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolFactory
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.data.gemfire.config.annotation.AddPoolConfiguration
 * @see org.springframework.data.gemfire.config.annotation.AddPoolsConfiguration
 * @see org.springframework.data.gemfire.config.annotation.EnablePool
 * @see org.springframework.data.gemfire.config.annotation.EnablePools
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class PoolPropertiesIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

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

	private void assertPool(Pool pool, int freeConnectionTimeout, long idleTimeout, int loadConditioningInterval,
			int maxConnections, int minConnections, int maxConnectionsPerServer, int minConnectionsPerServer,
			boolean multiUserAuthentication, String name, long pingInterval, boolean prSinglehopEnabled,
			int readTimeout, int retryAttempts, int serverConnectionTimeout, String serverGroup, int socketBufferSize,
			int socketConnectTimeout, SocketFactory socketFactory, int statisticInterval, int subscriptionAckInterval,
			boolean subscriptionEnabled, int subscriptionMessageTrackingTimeout, int subscriptionRedundancy) {

		assertThat(pool).isNotNull();
		assertThat(pool.getFreeConnectionTimeout()).isEqualTo(freeConnectionTimeout);
		assertThat(pool.getIdleTimeout()).isEqualTo(idleTimeout);
		assertThat(pool.getLoadConditioningInterval()).isEqualTo(loadConditioningInterval);
		assertThat(pool.getMaxConnections()).isEqualTo(maxConnections);
		assertThat(pool.getMinConnections()).isEqualTo(minConnections);
		assertThat(pool.getMaxConnectionsPerServer()).isEqualTo(maxConnectionsPerServer);
		assertThat(pool.getMinConnectionsPerServer()).isEqualTo(minConnectionsPerServer);
		assertThat(pool.getMultiuserAuthentication()).isEqualTo(multiUserAuthentication);
		assertThat(pool.getName()).isEqualTo(name);
		assertThat(pool.getPingInterval()).isEqualTo(pingInterval);
		assertThat(pool.getPRSingleHopEnabled()).isEqualTo(prSinglehopEnabled);
		assertThat(pool.getReadTimeout()).isEqualTo(readTimeout);
		assertThat(pool.getRetryAttempts()).isEqualTo(retryAttempts);
		assertThat(pool.getServerConnectionTimeout()).isEqualTo(serverConnectionTimeout);
		assertThat(pool.getServerGroup()).isEqualTo(serverGroup);
		assertThat(pool.getSocketBufferSize()).isEqualTo(socketBufferSize);
		assertThat(pool.getSocketConnectTimeout()).isEqualTo(socketConnectTimeout);
		assertThat(pool.getSocketFactory()).isEqualTo(socketFactory);
		assertThat(pool.getStatisticInterval()).isEqualTo(statisticInterval);
		assertThat(pool.getSubscriptionAckInterval()).isEqualTo(subscriptionAckInterval);
		assertThat(pool.getSubscriptionEnabled()).isEqualTo(subscriptionEnabled);
		assertThat(pool.getSubscriptionMessageTrackingTimeout()).isEqualTo(subscriptionMessageTrackingTimeout);
		assertThat(pool.getSubscriptionRedundancy()).isEqualTo(subscriptionRedundancy);
	}

	@Test
	public void poolConfiguration() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.pool.free-connection-timeout", 5000)
			.withProperty("spring.data.gemfire.pool.locators", "skullbox[11235]")
			.withProperty("spring.data.gemfire.pool.max-connections", 400)
			.withProperty("spring.data.gemfire.pool.min-connections", 10)
				.withProperty("spring.data.gemfire.pool.max-connections-per-server", 40)
				.withProperty("spring.data.gemfire.pool.min-connections-per-server", 2)
			.withProperty("spring.data.gemfire.pool.ping-interval", 5000L)
			.withProperty("spring.data.gemfire.pool.pr-single-hop-enabled", false)
			.withProperty("spring.data.gemfire.pool.read-timeout", 15000)
			.withProperty("spring.data.gemfire.pool.default.read-timeout", 5000L)
			.withProperty("spring.data.gemfire.pool.retry-attempts", 2)
			.withProperty("spring.data.gemfire.pool.server-group", "TestGroup")
			.withProperty("spring.data.gemfire.pool.default.socket-connect-timeout", 5000)
			.withProperty("spring.data.gemfire.pool.subscription-enabled", true)
			.withProperty("spring.data.gemfire.pool.TestPool.subscription-redundancy", 2);

		newApplicationContext(testPropertySource, TestPoolConfiguration.class);

		assertThat(containsBean("gemfireCache")).isTrue();
		assertThat(containsBean("TestPool")).isTrue();

		Pool testPool = getBean("TestPool", Pool.class);

		SocketFactory mockSocketFactory = getBean("mockSocketFactory", SocketFactory.class);

		assertThat(testPool).isNotNull();
		assertThat(mockSocketFactory).isNotNull();
		assertThat(testPool.getFreeConnectionTimeout()).isEqualTo(5000);
		assertThat(testPool.getIdleTimeout()).isEqualTo(10000L);
		assertThat(testPool.getLoadConditioningInterval()).isEqualTo(100000);
		assertThat(testPool.getLocators()).contains(new InetSocketAddress("skullbox", 11235));
		assertThat(testPool.getMaxConnections()).isEqualTo(400);
		assertThat(testPool.getMinConnections()).isEqualTo(10);
		assertThat(testPool.getMaxConnectionsPerServer()).isEqualTo(40);
		assertThat(testPool.getMinConnectionsPerServer()).isEqualTo(2);
		assertThat(testPool.getMultiuserAuthentication()).isFalse();
		assertThat(testPool.getName()).isEqualTo("TestPool");
		assertThat(testPool.getPingInterval()).isEqualTo(5000L);
		assertThat(testPool.getPRSingleHopEnabled()).isFalse();
		assertThat(testPool.getReadTimeout()).isEqualTo(15000);
		assertThat(testPool.getRetryAttempts()).isEqualTo(1);
		assertThat(testPool.getServerConnectionTimeout()).isEqualTo(60000);
		assertThat(testPool.getServerGroup()).isEqualTo("TestGroup");
		assertThat(testPool.getSocketBufferSize()).isEqualTo(PoolFactory.DEFAULT_SOCKET_BUFFER_SIZE);
		assertThat(testPool.getSocketConnectTimeout()).isEqualTo(PoolFactory.DEFAULT_SOCKET_CONNECT_TIMEOUT);
		assertThat(testPool.getSocketFactory()).isEqualTo(mockSocketFactory);
		assertThat(testPool.getSubscriptionAckInterval()).isEqualTo(PoolFactory.DEFAULT_SUBSCRIPTION_ACK_INTERVAL);
		assertThat(testPool.getSubscriptionEnabled()).isEqualTo(true);
		assertThat(testPool.getSubscriptionMessageTrackingTimeout()).isEqualTo(PoolFactory.DEFAULT_SUBSCRIPTION_MESSAGE_TRACKING_TIMEOUT);
		assertThat(testPool.getSubscriptionRedundancy()).isEqualTo(2);
	}

	@Test
	public void multiPoolConfiguration() {

		MockPropertySource testPropertySource = new MockPropertySource()
			.withProperty("spring.data.gemfire.pool.free-connection-timeout", 30000)
			.withProperty("spring.data.gemfire.pool.idle-timeout", 300000L)
			.withProperty("spring.data.gemfire.pool.load-conditioning-interval", 120000)
			.withProperty("spring.data.gemfire.pool.locators", "localhost[10334]")
			.withProperty("spring.data.gemfire.pool.max-connections", 500)
			.withProperty("spring.data.gemfire.pool.min-connections", 50)
			.withProperty("spring.data.gemfire.pool.multi-user-authentication", true)
			.withProperty("spring.data.gemfire.pool.ping-interval", 5000L)
			.withProperty("spring.data.gemfire.pool.pr-single-hop-enabled", false)
			.withProperty("spring.data.gemfire.pool.read-timeout", 15000L)
			.withProperty("spring.data.gemfire.pool.retry-attempts", 2)
			.withProperty("spring.data.gemfire.pool.server-connection-timeout", 30000)
			.withProperty("spring.data.gemfire.pool.server-group", "testGroup")
			.withProperty("spring.data.gemfire.pool.socket-buffer-size", 8192)
			.withProperty("spring.data.gemfire.pool.socket-connect-timeout", 5000)
			.withProperty("spring.data.gemfire.pool.socket-factory-bean-name", "mockSocketFactoryOne")
			.withProperty("spring.data.gemfire.pool.statistic-interval", 1000)
			.withProperty("spring.data.gemfire.pool.subscription-ack-interval", 5000)
			.withProperty("spring.data.gemfire.pool.subscription-enabled", true)
			.withProperty("spring.data.gemfire.pool.subscription-message-tracking-timeout", 180000)
			.withProperty("spring.data.gemfire.pool.subscription-redundancy", 2)
			.withProperty("spring.data.gemfire.pool.default.free-connection-timeout", 15000)
			.withProperty("spring.data.gemfire.pool.default.idle-timeout", 20000L)
			.withProperty("spring.data.gemfire.pool.default.load-conditioning-interval", 180000)
			.withProperty("spring.data.gemfire.pool.default.locators", "skullbox[11235]")
			.withProperty("spring.data.gemfire.pool.default.max-connections", 275)
			.withProperty("spring.data.gemfire.pool.default.min-connections", 27)
			.withProperty("spring.data.gemfire.pool.default.multi-user-authentication", true)
			.withProperty("spring.data.gemfire.pool.default.ping-interval", 15000L)
			.withProperty("spring.data.gemfire.pool.default.pr-single-hop-enabled", false)
			.withProperty("spring.data.gemfire.pool.default.read-timeout", 2000L)
			.withProperty("spring.data.gemfire.pool.default.retry-attempts", 1)
			.withProperty("spring.data.gemfire.pool.default.server-connection-timeout", 60000)
			.withProperty("spring.data.gemfire.pool.default.server-group", "testDefaultGroup")
			.withProperty("spring.data.gemfire.pool.default.socket-buffer-size", 16384)
			.withProperty("spring.data.gemfire.pool.default.socket-connect-timeout", 10000)
			.withProperty("spring.data.gemfire.pool.default.socket-factory-bean-name", "")
			.withProperty("spring.data.gemfire.pool.default.statistic-interval", 500)
			.withProperty("spring.data.gemfire.pool.default.subscription-ack-interval", 250)
			.withProperty("spring.data.gemfire.pool.default.subscription-enabled", true)
			.withProperty("spring.data.gemfire.pool.default.subscription-message-tracking-timeout", 300000)
			.withProperty("spring.data.gemfire.pool.default.subscription-redundancy", 3)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.free-connection-timeout", 20000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.idle-timeout", 15000L)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.load-conditioning-interval", 60000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.servers", "jambox[12480]")
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.max-connections", 1000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.min-connections", 100)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.multi-user-authentication", true)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.ping-interval", 20000L)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.pr-single-hop-enabled", false)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.read-timeout", 5000L)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.retry-attempts", 4)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.server-group", "testTwoGroup")
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.socket-buffer-size", 65536)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.socket-connect-timeout", 15000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.socket-factory-bean-name", "mockSocketFactoryTwo")
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.statistic-interval", 2000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.subscription-ack-interval", 500)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.subscription-enabled", true)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.subscription-message-tracking-timeout", 300000)
			.withProperty("spring.data.gemfire.pool.TestPoolTwo.subscription-redundancy", 4)
			.withProperty("spring.data.gemfire.pool.TestPoolThree.max-connections-per-server", 300)
			.withProperty("spring.data.gemfire.pool.TestPoolThree.min-connections-per-server", 3);

		newApplicationContext(testPropertySource, TestPoolsConfiguration.class);

		assertThat(containsBean("gemfireCache")).isTrue();
		assertThat(containsBean("TestPoolOne")).isTrue();
		assertThat(containsBean("TestPoolTwo")).isTrue();

		ClientCache gemfireCache = getBean(ClientCache.class);

		assertThat(gemfireCache).isNotNull();

		Pool defaultPool = gemfireCache.getDefaultPool();

		SocketFactory mockSocketFactoryOne = getBean("mockSocketFactoryOne", SocketFactory.class);
		SocketFactory mockSocketFactoryTwo = getBean("mockSocketFactoryTwo", SocketFactory.class);

		assertThat(mockSocketFactoryOne).isNotNull();
		assertThat(mockSocketFactoryTwo).isNotNull();
		assertThat(mockSocketFactoryOne).isNotSameAs(mockSocketFactoryTwo);

		assertPool(defaultPool, 15000, 20000L, 180000,
			275, 27, -1, 1, true, "DEFAULT", 15000L,
			false, 2000, 1, 60000, "testDefaultGroup",
			16384, 10000, SocketFactory.DEFAULT, 500, 250,
			true, 300000, 3);

		Pool testPoolOne = getBean("TestPoolOne", Pool.class);

		assertPool(testPoolOne, 30000, 300000L, 120000,
			500, 50, -1, 1, true, "TestPoolOne", 5000L,
			false, 15000, 2, 30000, "testGroup",
			8192, 5000, mockSocketFactoryOne,1000, 5000,
			true, 180000, 2);

		Pool testPoolTwo = getBean("TestPoolTwo", Pool.class);

		assertPool(testPoolTwo, 20000, 15000L, 60000,
			1000, 100, -1, 1, true, "TestPoolTwo", 20000L,
			false, 5000, 4, 30000, "testTwoGroup",
			65536, 15000, mockSocketFactoryTwo, 2000, 500,
			true, 300000, 4);

		Pool testPoolThree = getBean("TestPoolThree", Pool.class);

		assertPool(testPoolTwo, 20000, 15000L, 60000,
				1000, 100, -1, 1, true, "TestPoolTwo", 20000L,
				false, 5000, 4, 30000, "testTwoGroup",
				65536, 15000, mockSocketFactoryTwo, 2000, 500,
				true, 300000, 4);
	}

	@EnableGemFireMockObjects
	@ClientCacheApplication
	@EnablePool(name = "TestPool", idleTimeout = 10000L, maxConnections = 200, minConnections = 20,
		serverConnectionTimeout = 60000, socketFactoryBeanName = "mockSocketFactory")
	static class TestPoolConfiguration {

		@Bean
		PoolConfigurer testPoolConfigurer() {
			return (beanName, beanFactory) -> {
				beanFactory.setLoadConditioningInterval(100000);
				beanFactory.setRetryAttempts(1);
			};
		}

		@Bean
		SocketFactory mockSocketFactory() {
			return mock(SocketFactory.class);
		}
	}
	@EnableGemFireMockObjects
	@ClientCacheApplication
	@EnablePool(name = "TestPool", idleTimeout = 10000L, maxConnectionsPerServer = 250, minConnectionsPerServer = 25,
			serverConnectionTimeout = 60000, socketFactoryBeanName = "mockSocketFactory")
	static class PerServerTestPoolConfiguration {

		@Bean
		PoolConfigurer testPoolConfigurer() {
			return (beanName, beanFactory) -> {
				beanFactory.setLoadConditioningInterval(100000);
				beanFactory.setRetryAttempts(1);
			};
		}

		@Bean
		SocketFactory mockSocketFactory() {
			return mock(SocketFactory.class);
		}
	}

	@EnableGemFireMockObjects
	@ClientCacheApplication
	@EnablePools(pools = {
		@EnablePool(name = "TestPoolOne"),
		@EnablePool(name = "TestPoolTwo"),
		@EnablePool(name = "TestPoolThree")
	})
	static class TestPoolsConfiguration {

		@Bean
		SocketFactory mockSocketFactoryOne() {
			return mock(SocketFactory.class);
		}

		@Bean
		SocketFactory mockSocketFactoryTwo() {
			return mock(SocketFactory.class);
		}
	}
}
