/*
 * Copyright 2010-2013 the original author or authors.
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
package org.springframework.data.gemfire;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.Scope;

/**
 * @author David Turanski
 * @author John Blum
 */
public class LocalRegionFactoryBean<K, V> extends RegionFactoryBean<K, V> {

	@Override
	public void setScope(Scope scope) {
		throw new UnsupportedOperationException("Setting the Scope on Local Regions is not allowed.");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.setScope(Scope.LOCAL);
		super.afterPropertiesSet();
	}

	/**
	 * Resolves the Data Policy used by this "local" GemFire Region (i.e. locally Scoped; Scope.LOCAL) based on the
	 * enumerated value from com.gemstone.gemfire.cache.RegionShortcuts (LOCAL, LOCAL_PERSISTENT, LOCAL_HEAP_LRU,
	 * LOCAL_OVERFLOW, and LOCAL_PERSISTENT_OVERFLOW), but without consideration of the Eviction settings.
	 * <p/>
	 * @param regionFactory the GemFire RegionFactory used to created the Local Region.
	 * @param persistent a boolean value indicating whether the Local Region should persist it's data.
	 * @param dataPolicy requested Data Policy as set by the user in the Spring GemFire configuration meta-data.
	 * @see com.gemstone.gemfire.cache.DataPolicy
	 * @see com.gemstone.gemfire.cache.RegionFactory
	 * @see com.gemstone.gemfire.cache.RegionShortcut
	 */
	@Override
	protected void resolveDataPolicy(RegionFactory<K, V> regionFactory, Boolean persistent, String dataPolicy) {
		DataPolicy resolvedDataPolicy = new DataPolicyConverter().convert(dataPolicy);

		Assert.isTrue(resolvedDataPolicy != null || !StringUtils.hasText(dataPolicy),
			String.format("Data Policy '%1$s' is invalid.", dataPolicy));

		if (resolvedDataPolicy == null || DataPolicy.NORMAL.equals(resolvedDataPolicy)) {
			// NOTE this is safe since a LOCAL Scoped NORMAL Region requiring persistence can be satisfied with
			// PERSISTENT_REPLICATE, per the RegionShortcut.LOCAL_PERSISTENT
			regionFactory.setDataPolicy(isPersistent() ? DataPolicy.PERSISTENT_REPLICATE : DataPolicy.NORMAL);
		}
		else if (DataPolicy.PRELOADED.equals(resolvedDataPolicy)) {
			// NOTE this is safe since a LOCAL Scoped PRELOADED Region requiring persistence can be satisfied with
			// PERSISTENT_REPLICATE, per the RegionShortcut.LOCAL_PERSISTENT
			regionFactory.setDataPolicy(isPersistent() ? DataPolicy.PERSISTENT_REPLICATE : DataPolicy.PRELOADED);
		}
		else {
			throw new IllegalArgumentException(String.format("Data Policy '%1$s' is not supported in Local Regions.",
				dataPolicy));
		}
	}

}
