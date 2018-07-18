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
package io.vertigo.core.component.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.core.component.proxy.data.Aggregate;
import io.vertigo.util.AbstractTestCaseJU4;

@RunWith(JUnitPlatform.class)
public final class ProxyTest extends AbstractTestCaseJU4 {
	@Inject
	private Aggregate aggregatea;

	@Override
	protected Map<String, Function<Class, Lookup>> getPrivateLookups() {
		return Map.of("proxies", AbstractTestCaseJU4.getCoreLookup(),
				"components", AbstractTestCaseJU4.getCoreLookup());
	}

	@Test
	public final void testMin() {
		assertEquals(10, aggregatea.min(12, 10, 55));
		assertEquals(10, aggregatea.min(10, 55));
		assertEquals(10, aggregatea.min(10));
	}

	@Test
	public final void testMax() {
		assertEquals(55, aggregatea.max(12, 10, 55));
		assertEquals(55, aggregatea.max(10, 55));
		assertEquals(55, aggregatea.max(55));
	}

	@Test
	public final void testCount() {
		assertEquals(3, aggregatea.count(12, 10, 55));
		assertEquals(2, aggregatea.count(10, 55));
		assertEquals(1, aggregatea.count(55));
	}
}
