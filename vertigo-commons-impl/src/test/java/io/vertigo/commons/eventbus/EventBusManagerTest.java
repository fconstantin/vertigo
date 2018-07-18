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
package io.vertigo.commons.eventbus;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.commons.AbstractTestCaseJU4;

/**
 * @author pchretien
 */
public final class EventBusManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private EventBusManager eventBusManager;

	@Inject
	private MySubscriber mySubscriber;
	private int deadEvents = 0;

	@Override
	protected Map<String, Function<Class, Lookup>> getPrivateLookups() {
		return Map.of("vertigo-commons", getCommonsLookup(),
				"aspects", getCommonsLookup(),
				"myApp", getCommonsLookup());
	}

	@Override
	protected void doSetUp() {
		eventBusManager.registerDead(event -> deadEvents++);
	}

	@Test
	public void testSimple() {
		assertEquals(0, mySubscriber.getBlueCount());
		assertEquals(0, mySubscriber.getRedCount());
		assertEquals(0, mySubscriber.getCount());

		eventBusManager.post(new BlueColorEvent());
		eventBusManager.post(new WhiteColorEvent());
		eventBusManager.post(new RedColorEvent());
		eventBusManager.post(new RedColorEvent());

		assertEquals(1, mySubscriber.getBlueCount());
		assertEquals(2, mySubscriber.getRedCount());
		assertEquals(4, mySubscriber.getCount());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testWithAspects() {
		/*
		 * We want to check that aspects are used.
		 */
		assertTrue(FlipAspect.isOff());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		assertTrue(FlipAspect.isOn());

		eventBusManager.post(new RedColorEvent()); //there is no aspect
		assertTrue(FlipAspect.isOn());

		eventBusManager.post(new BlueColorEvent()); //<< Flip here
		assertTrue(FlipAspect.isOff());

		assertEquals(0, deadEvents);
	}

	@Test
	public void testDeadEvent() {
		assertEquals(0, deadEvents);
		eventBusManager.post(new DummyEvent());
		assertEquals(1, deadEvents);
	}
}
