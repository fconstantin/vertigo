/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.app.config.discovery;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Function;

import io.vertigo.lang.WrappedException;

public class TestModuleDiscoveryFeatures extends ModuleDiscoveryFeatures {

	public TestModuleDiscoveryFeatures() {
		super("test");
	}

	@Override
	protected String getPackageRoot() {
		return this.getClass().getPackage().getName();
	}
	
	@Override
	public Function<Class, Lookup> getLookupProvider() {
		final Function<Class, Lookup> featuresLookup = new Function<>() {
			@Override
			public Lookup apply(Class t) {
				try {
					return MethodHandles.privateLookupIn(t, MethodHandles.lookup());
				} catch (IllegalAccessException e) {
					throw WrappedException.wrap(e);
				}
			}
			
		};
		return featuresLookup;
	}

}
