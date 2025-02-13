/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.springframework.data.gemfire.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.gemfire.util.SpringExtensions;
import org.springframework.data.gemfire.wan.AsyncEventQueueFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * {@link BeanDefinitionParser} for &lt;gfe:async-event-queue&gt; SDG XML Namespace (XSD) Elements.
 *
 * @author David Turanski
 * @author John Blum
 * @see Element
 * @see BeanDefinitionBuilder
 * @see AbstractSingleBeanDefinitionParser
 * @see AsyncEventQueueFactoryBean
 */
class AsyncEventQueueParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return AsyncEventQueueFactoryBean.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		builder.setLazyInit(false);

		parseAsyncEventListener(element, parserContext, builder);
		parseCache(element, builder);
		parseDiskStore(element, builder);

		ParsingUtils.setPropertyValue(element, builder, "enable-batch-conflation", "batchConflationEnabled");
		ParsingUtils.setPropertyValue(element, builder, "batch-conflation-enabled");
		ParsingUtils.setPropertyValue(element, builder, "batch-size");
		ParsingUtils.setPropertyValue(element, builder, "batch-time-interval");
		ParsingUtils.setPropertyValue(element, builder, "disk-synchronous");
		ParsingUtils.setPropertyValue(element, builder, "dispatcher-threads");
		ParsingUtils.setPropertyValue(element, builder, "forward-expiration-destroy");
		ParsingUtils.setPropertyValue(element, builder, "maximum-queue-memory");
		ParsingUtils.setPropertyValue(element, builder, "order-policy");
		ParsingUtils.setPropertyValue(element, builder, "parallel");
		ParsingUtils.setPropertyValue(element, builder, "pause-event-dispatching");
		ParsingUtils.setPropertyValue(element, builder, "persistent");

		Element eventFilterElement = DomUtils.getChildElementByTagName(element, "event-filter");

		if (eventFilterElement != null) {
			builder.addPropertyValue("gatewayEventFilters",
				ParsingUtils.parseRefOrNestedBeanDeclaration(eventFilterElement, parserContext, builder));
		}

		Element eventSubstitutionFilterElement =
			DomUtils.getChildElementByTagName(element, "event-substitution-filter");

		if (eventSubstitutionFilterElement != null) {
			builder.addPropertyValue("gatewayEventSubstitutionFilter",
				ParsingUtils.parseRefOrSingleNestedBeanDeclaration(eventSubstitutionFilterElement, parserContext, builder));
		}

		ParsingUtils.setPropertyValue(element, builder, NAME_ATTRIBUTE);

		if (!StringUtils.hasText(element.getAttribute(NAME_ATTRIBUTE))) {
			if (element.getParentNode().getNodeName().endsWith("region")) {

				Element region = (Element) element.getParentNode();

				String regionName = StringUtils.hasText(region.getAttribute(NAME_ATTRIBUTE))
					? region.getAttribute(NAME_ATTRIBUTE)
					: region.getAttribute(ID_ATTRIBUTE);

				int index = 0;

				String name = regionName + ".asyncEventQueue#" + index;

				while (parserContext.getRegistry().isBeanNameInUse(name)) {
					name = regionName + ".asyncEventQueue#" + (++index);
				}

				builder.addPropertyValue("name", name);
			}
		}
	}

	private void parseAsyncEventListener(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		Element asyncEventListenerElement = DomUtils.getChildElementByTagName(element, "async-event-listener");

		Object asyncEventListener =
			ParsingUtils.parseRefOrSingleNestedBeanDeclaration(asyncEventListenerElement, parserContext, builder);

		builder.addPropertyValue("asyncEventListener", asyncEventListener);

		if (asyncEventListener instanceof RuntimeBeanReference) {
			builder.addDependsOn(((RuntimeBeanReference) asyncEventListener).getBeanName());
		}
	}

	private void parseCache(Element element, BeanDefinitionBuilder builder) {

		String cacheRefAttribute = element.getAttribute("cache-ref");
		String cacheName = SpringExtensions.defaultIfEmpty(cacheRefAttribute, GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME);

		builder.addConstructorArgReference(cacheName);
	}

	private void parseDiskStore(Element element, BeanDefinitionBuilder builder) {

		ParsingUtils.setPropertyValue(element, builder, "disk-store-ref");

		String diskStoreRef = element.getAttribute("disk-store-ref");

		if (StringUtils.hasText(diskStoreRef)) {
			builder.addDependsOn(diskStoreRef);
		}
	}
}
