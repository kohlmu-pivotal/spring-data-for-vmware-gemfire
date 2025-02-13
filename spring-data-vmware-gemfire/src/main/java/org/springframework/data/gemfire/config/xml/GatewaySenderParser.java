/*
 * Copyright (c) VMware, Inc. 2022-2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.data.gemfire.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.gemfire.util.SpringExtensions;
import org.springframework.data.gemfire.wan.GatewaySenderFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * Bean definition parser for the &lt;gfe:gateway-sender&gt; SDG XML namespace (XSD) element.
 *
 * @author David Turanski
 * @author John Blum
 * @see AbstractSimpleBeanDefinitionParser
 * @see GatewaySenderFactoryBean
 */
class GatewaySenderParser extends AbstractSimpleBeanDefinitionParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return GatewaySenderFactoryBean.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		String cacheRef = element.getAttribute(ParsingUtils.CACHE_REF_ATTRIBUTE_NAME);

		builder.addConstructorArgReference(SpringExtensions.defaultIfEmpty(
			cacheRef, GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME));

		ParsingUtils.setPropertyValue(element, builder, NAME_ATTRIBUTE);
		ParsingUtils.setPropertyValue(element, builder, "alert-threshold");
		ParsingUtils.setPropertyValue(element, builder, "enable-batch-conflation", "batchConflationEnabled");
		ParsingUtils.setPropertyValue(element, builder, "batch-conflation-enabled");
		ParsingUtils.setPropertyValue(element, builder, "batch-size");
		ParsingUtils.setPropertyValue(element, builder, "batch-time-interval");
		ParsingUtils.setPropertyValue(element, builder, "disk-store-ref");
		ParsingUtils.setPropertyValue(element, builder, "disk-synchronous");
		ParsingUtils.setPropertyValue(element, builder, "dispatcher-threads");
		ParsingUtils.setPropertyValue(element, builder, "manual-start");
		ParsingUtils.setPropertyValue(element, builder, "maximum-queue-memory");
		ParsingUtils.setPropertyValue(element, builder, "order-policy");
		ParsingUtils.setPropertyValue(element, builder, "parallel");
		ParsingUtils.setPropertyValue(element, builder, "persistent");
		ParsingUtils.setPropertyValue(element, builder, "remote-distributed-system-id");
		ParsingUtils.setPropertyValue(element, builder, "socket-buffer-size");
		ParsingUtils.setPropertyValue(element, builder, "socket-read-timeout");

		Element eventFilterElement = DomUtils.getChildElementByTagName(element, "event-filter");

		if (eventFilterElement != null) {
			builder.addPropertyValue("eventFilters", ParsingUtils.parseRefOrNestedBeanDeclaration(
				eventFilterElement, parserContext, builder));
		}

		Element eventSubstitutionFilterElement = DomUtils.getChildElementByTagName(element, "event-substitution-filter");

		if (eventSubstitutionFilterElement != null) {
			builder.addPropertyValue("eventSubstitutionFilter", ParsingUtils.parseRefOrSingleNestedBeanDeclaration(
				eventSubstitutionFilterElement, parserContext, builder));
		}

		ParsingUtils.parseTransportFilters(element, parserContext, builder);

		 // set the name for the GatewaySender as an inner bean
		if (!StringUtils.hasText(element.getAttribute(NAME_ATTRIBUTE))) {
			if (element.getParentNode().getNodeName().endsWith("region")) {
				Element region = (Element) element.getParentNode();

				String regionName = (StringUtils.hasText(region.getAttribute(NAME_ATTRIBUTE))
					? region.getAttribute(NAME_ATTRIBUTE) : region.getAttribute(ID_ATTRIBUTE));

				int index = 0;

				String gatewaySenderName = (regionName + ".gatewaySender#" + index);

				while (parserContext.getRegistry().isBeanNameInUse(gatewaySenderName)) {
					gatewaySenderName = (regionName + ".gatewaySender#" + (++index));
				}

				builder.addPropertyValue("name", gatewaySenderName);
			}
		}
	}
}
