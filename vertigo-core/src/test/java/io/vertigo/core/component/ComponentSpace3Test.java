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
package io.vertigo.core.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.param.Param;
import io.vertigo.util.AbstractTestCaseJU4;

@RunWith(JUnitPlatform.class)
public final class ComponentSpace3Test {

	@Test
	public void testInjectPluginsAttribute() {
		final AppConfig appConfig = createHomeWithInjectPluginsAttribute(true);
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final FunctionManager functionManager = app.getComponentSpace().resolve(FunctionManager.class);
			assertEquals(4, functionManager.compute("x+1", 3));
			assertEquals(6, functionManager.compute("2x", 3));
			assertEquals(15, functionManager.compute("4x+3", 3));
			assertEquals(1, functionManager.compute("0x+1", 3));
			assertEquals(-7, functionManager.compute("x-10", 3));
			assertEquals(-9, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsAttributeOrder() {
		final AppConfig appConfig = createHomeWithInjectPluginsAttribute(false);
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final FunctionManager functionManager = app.getComponentSpace().resolve(FunctionManager.class);
			assertEquals(26, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsConstructor() {
		final AppConfig appConfig = createHomeWithInjectPluginsConstructor(true);
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final FunctionManager functionManager = app.getComponentSpace().resolve(FunctionManager.class);
			assertEquals(4, functionManager.compute("x+1", 3));
			assertEquals(6, functionManager.compute("2x", 3));
			assertEquals(15, functionManager.compute("4x+3", 3));
			assertEquals(1, functionManager.compute("0x+1", 3));
			assertEquals(-7, functionManager.compute("x-10", 3));
			assertEquals(-9, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsConstructorOrder() {
		final AppConfig appConfig = createHomeWithInjectPluginsConstructor(false);
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final FunctionManager functionManager = app.getComponentSpace().resolve(FunctionManager.class);
			assertEquals(26, functionManager.computeAll(3));
		}
	}

	private static AppConfig createHomeWithInjectPluginsAttribute(final boolean withNullMult) {
		return startHomeWithFunctionManager(FunctionManager1Impl.class, withNullMult);
	}

	private static AppConfig createHomeWithInjectPluginsConstructor(final boolean withNullMult) {
		return startHomeWithFunctionManager(FunctionManager2Impl.class, withNullMult);
	}

	private static AppConfig startHomeWithFunctionManager(final Class<? extends FunctionManager> implClass, final boolean withNullMult) {

		return AppConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Function", AbstractTestCaseJU4.getCoreLookup())
						.addComponent(FunctionManager.class, implClass)
						.addPlugin(FunctionPlugin.class,
								Param.of("name", "x+1"),
								Param.of("a", "1"),
								Param.of("b", "1"))
						.addPlugin(FunctionPlugin.class,
								Param.of("name", "2x"),
								Param.of("a", "2"),
								Param.of("b", "0"))
						.addPlugin(FunctionPlugin.class,
								Param.of("name", "4x+3"),
								Param.of("a", "4"),
								Param.of("b", "3"))
						.addPlugin(FunctionPlugin.class,
								Param.of("name", (withNullMult ? "0" : "1") + "x+1"),
								Param.of("a", withNullMult ? "0" : "1"),
								Param.of("b", "1"))
						.addPlugin(FunctionPlugin.class,
								Param.of("name", "x-10"),
								Param.of("a", "1"),
								Param.of("b", "-10"))
						.build())
				.build();
	}
}
