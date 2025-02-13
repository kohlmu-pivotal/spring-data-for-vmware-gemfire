/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.annotation;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.apache.geode.security.SecurableCommunicationChannels;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

/**
 * The {@link EnableSsl} annotation marks a Spring {@link Configuration @Configuration} annotated {@link Class}
 * to configure and enable Pivotal GemFire/Apache Geode's TCP/IP Socket SSL.
 *
 * @author John Blum
 * @author Srikanth Manvi
 * @see java.lang.annotation.Annotation
 * @see SecurableCommunicationChannels
 * @see Import
 * @see SslConfiguration
 * @since 1.9.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(SslConfiguration.class)
@UsesGemFireProperties
@SuppressWarnings("unused")
public @interface EnableSsl {

	/**
	 * Configures the SSL ciphers used for secure Socket connections as an array of valid {@link String cipher names}.
	 *
	 * A setting of {@literal any} uses any ciphers that are enabled by default in the configured JSSE provider.
	 *
	 * Defaults to {@literal any}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.ciphers} property
	 * in {@literal application.properties}.
	 */
	String[] ciphers() default { "any" };

	/**
	 * Configures a list of the SSL protocols to enable on the client-side of the SSL connection.
	 *
	 * The protocols listed must be supported by the available providers.
	 *
	 * Defaults to {@literal empty}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.client.protocols} property
	 * in {@literal application.properties}.
	 */
	String[] clientProtocols() default {};

	/**
	 * Configures the Pivotal GemFire/Apache Geode components for which SSL will be enabled.
	 *
	 * Defaults to {@link Component#ALL}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.components} property
	 * in {@literal application.properties}.
	 *
	 * E.g. {@literal spring.data.gemfire.security.ssl.components=cluster,gateway,locator,server}.
	 */
	Component[] components() default { Component.ALL };

	/**
	 * Configures the {@link String names} of all the individual {@literal Keystore} certificates to use
	 * when configuring SSL for each Pivotal GemFire/Apache Geode {@link Component} separately.
	 *
	 * Each {@link Component} defaults to the configured value of the {@link #defaultCertificateAlias()}
	 * if not individually configured with this attribute.
	 *
	 * Use {@literal spring.data.gemfire.security.ssl.certificate.alias.<component>}
	 * in {@literal application.properties}.
	 *
	 * E.g. {@literal spring.data.gemfire.security.ssl.certificate.alias.gateway=WanCert}.
	 */
	ComponentAlias[] componentCertificateAliases() default {};

	/**
	 * Configures the default {@link String name} of a single {@literal Keystore} certificate to use
	 * when configuring SSL for all Pivotal GemFire/Apache Geode {@link Component components} collectively.
	 *
	 * If {@code defaultAlias} is not specified, then the first certificate in the {@literal Keystore}
	 * acts as the {@literal default} SSL certificate.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.certificate.alias.default} property
	 * in {@literal application.properties}.
	 */
	String defaultCertificateAlias() default "";

	/**
	 * If {@literal true} then causes clients (or Apache Geode servers in cause of P2P) to validate the server's
	 * hostname using the server`s certificate. Set to {@literal true} if {@literal useSSLDefaultDefaultContext}
	 * is {@literal true}.
	 *
	 * Defaults to {@literal false}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.enable-endpoint-identification}
	 * in {@literal application.properties}.
	 */
	boolean enableEndpointIdentification() default false;

	/**
	 * Pathname to the {@literal Keystore} used for SSL communications.
	 *
	 * Defaults to unset.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.keystore} property
	 * in {@literal application.properties}.
	 */
	String keystore() default "";

	/**
	 * Password to access the keys in the {@literal Keystore} used for SSL communications.
	 *
	 * Defaults to unset.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.keystore.password} property
	 * in {@literal application.properties}.
	 */
	String keystorePassword() default "";

	/**
	 * Identifies the type of {@literal Keystore} used for SSL communications.
	 *
	 * For example: {@literal JKS}, {@literal PKCS11}, etc.
	 *
	 * Defaults to {@literal JKS}, or the {@literal Java Keystore}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.keystore.type} property
	 * in {@literal application.properties}.
	 */
	String keystoreType() default "JKS";

	/**
	 * Configures the SSL protocols used for secure Socket connections as an array of
	 * valid {@link String protocol names}.
	 *
	 * A setting of {@literal any} uses any protocol that is enabled by default in the configured JSSE provider.
	 *
	 * Defaults to {@literal any}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.protocols} property
	 * in {@literal application.properties}.
	 */
	String[] protocols() default { "any" };

	/**
	 * Boolean value indicating whether to require authentication for SSL communication between clients, servers,
	 * gateways, etc.
	 *
	 * Defaults to {@literal true}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.require-authentication} property
	 * in {@literal application.properties}.
	 */
	boolean requireAuthentication() default true;

	/**
	 * Configures a list of the SSL protocols to enable on the server-side of the SSL connection.
	 *
	 * The protocols listed must be supported by the available providers.
	 *
	 * Defaults to {@literal empty}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.server.protocols} property
	 * in {@literal application.properties}.
	 */
	String[] serverProtocols() default {};

	/**
	 * Pathname to the truststore used for SSL communications.
	 *
	 * Defaults to unset.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.truststore} property
	 * in {@literal application.properties}.
	 */
	String truststore() default "";

	/**
	 * Password to access the keys in the truststore used for SSL communications.
	 *
	 * Defaults to unset.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.truststore.password} property
	 * in {@literal application.properties}.
	 */
	String truststorePassword() default "";

	/**
	 * Identifies the type of truststore used for SSL communications.
	 *
	 * For example: {@literal JKS}, {@literal PKCS11}, etc.
	 *
	 * Defaults to {@literal JKS}, or the {@literal Java Keystore}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.truststore.type} property
	 * in {@literal application.properties}.
	 */
	String truststoreType() default "JKS";

	/**
	 * If {@literal true} then this will enable the use of default SSL context
	 * and sets {@literal ssl-endpoint-identification-enabled} to {@literal true}.
	 *
	 * Defaults to {@literal false}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.use-default-context}
	 * in {@literal application.properties}.
	 */
	boolean useDefaultContext() default false;

	/**
	 * If {@literal true} then requires two-way authentication for web component.
	 *
	 * Defaults to {@literal false}.
	 *
	 * Use the {@literal spring.data.gemfire.security.ssl.web-require-authentication}
	 * in {@literal application.properties}.
	 */
	boolean webRequireAuthentication() default false;

	enum Component {

		ALL(SecurableCommunicationChannels.ALL),
		CLUSTER(SecurableCommunicationChannels.CLUSTER),
		GATEWAY(SecurableCommunicationChannels.GATEWAY),
		JMX(SecurableCommunicationChannels.JMX),
		LOCATOR(SecurableCommunicationChannels.LOCATOR),
		SERVER(SecurableCommunicationChannels.SERVER),
		WEB(SecurableCommunicationChannels.WEB);

		public static Component valueOfName(String name) {

			return Arrays.stream(values())
				.filter(component -> component.name().equalsIgnoreCase(String.valueOf(name).trim()))
				.findFirst()
				.orElseThrow(() -> newIllegalArgumentException("Name [%s] is not a valid component", name));
		}

		private final String prefix;

		Component(String prefix) {

			Assert.hasText(prefix, "Prefix is required");

			this.prefix = prefix;
		}

		/**
		 * Returns a {@link String} representation of this enumerated value.
		 *
		 * @return a {@link String} describing this enumerated value.
		 * @see Object#toString()
		 */
		@Override
		public String toString() {
			return this.prefix;
		}
	}

	@interface ComponentAlias {

		String alias();

		Component component();

	}
}
