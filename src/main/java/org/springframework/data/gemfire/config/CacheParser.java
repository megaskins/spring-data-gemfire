/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.config;

import java.util.List;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for &lt;cache;gt; definitions.
 * 
 * @author Costin Leau
 * @author Oliver Gierke
 * @author David Turanski
 */
class CacheParser extends AbstractSimpleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return CacheFactoryBean.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, builder);

		ParsingUtils.setPropertyValue(element, builder, "cache-xml-location", "cacheXml");
		ParsingUtils.setPropertyReference(element, builder, "properties-ref", "properties");
		ParsingUtils.setPropertyReference(element, builder, "pdx-serializer", "pdxSerializer");
		ParsingUtils.setPropertyValue(element, builder, "pdx-disk-store", "pdxDiskStoreName");
		ParsingUtils.setPropertyValue(element, builder, "pdx-persistent", "pdxPersistent");
		ParsingUtils.setPropertyValue(element, builder, "pdx-read-serialized", "pdxReadSerialized");
		ParsingUtils.setPropertyValue(element, builder, "pdx-ignore-unread-fields", "pdxIgnoreUnreadFields");
		ParsingUtils.setPropertyValue(element, builder, "use-bean-factory-locator", "useBeanFactoryLocator");
		ParsingUtils.setPropertyValue(element, builder, "copy-on-read", "copyOnRead");
		ParsingUtils.setPropertyValue(element, builder, "lock-timeout", "lockTimeout");
		ParsingUtils.setPropertyValue(element, builder, "lock-lease", "lockLease");
		ParsingUtils.setPropertyValue(element, builder, "message-sync-interval", "messageSyncInterval");
		ParsingUtils.setPropertyValue(element, builder, "search-timeout", "searchTimeout");
		List<Element> txListeners = DomUtils.getChildElementsByTagName(element, "transaction-listener");
		if (!CollectionUtils.isEmpty(txListeners)) {
			ManagedList<Object> transactionListeners = new ManagedList<Object>();
			for (Element txListener : txListeners) {
				transactionListeners.add(ParsingUtils.parseRefOrNestedBeanDeclaration(parserContext, txListener,
						builder));
			}
			builder.addPropertyValue("transactionListeners", transactionListeners);
		}
		Element txWriter = DomUtils.getChildElementByTagName(element, "transaction-writer");
		if (txWriter != null) {
			builder.addPropertyValue("transactionWriter",
					ParsingUtils.parseRefOrNestedBeanDeclaration(parserContext, txWriter, builder));
		}

		Element initializer = DomUtils.getChildElementByTagName(element, "initializer");
		if (initializer != null) {
			builder.addPropertyValue("initializer",
					ParsingUtils.parseRefOrNestedBeanDeclaration(parserContext, initializer, builder));
		}
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String name = super.resolveId(element, definition, parserContext);
		if (!StringUtils.hasText(name)) {
			name = "gemfire-cache";
		}
		return name;
	}

}