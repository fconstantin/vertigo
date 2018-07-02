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
package io.vertigo.core.param;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.util.AbstractTestCaseJU4;

/**
 * @author pchretien
 */
public abstract class AbstractParamManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ParamManager paramManager;

	@Test
	public void test1() {
		final String value = paramManager.getParam("server.host").getValueAsString();
		assertEquals("wiki", value);
	}

	@Test
	public void test2() {
		Assertions.assertThrows(Exception.class,
				() -> paramManager.getParam("server.wrong").getValueAsString());
	}

	@Test
	public void test3() {
		final int value = paramManager.getParam("server.port").getValueAsInt();
		assertEquals(8080, value);
	}

	@Test
	public void test4() {
		Assertions.assertThrows(Exception.class,
				() -> paramManager.getParam("server.active").getValueAsInt());
	}

	@Test
	public void test5() {
		final boolean value = paramManager.getParam("server.active").getValueAsBoolean();
		assertTrue(value);
	}

	@Test
	public void test6() {
		final boolean value = paramManager.getParam("server.verbose").getValueAsBoolean();
		assertFalse(value);
	}

	@Test
	public void test7() {
		Assertions.assertThrows(Exception.class,
				() -> paramManager.getParam("server.port").getValueAsBoolean());
	}
}
