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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
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
public final class ComponentSpaceTest {

	@Test
	public void testHome() {
		final AppConfig appConfig = AppConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio", AbstractTestCaseJU4.getCoreLookup())
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testHome2() {
		final AppConfig appConfig = AppConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio", AbstractTestCaseJU4.getCoreLookup())
						.addComponent(BioManager.class, BioManagerImpl.class)
						//This plugin DummyPlugin is not used By BioManager !!
						.addPlugin(DummyPlugin.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class,
				() -> {
					try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
						//
					}
				});
	}

	@Test
	public void testHome3() {
		final AppConfig appConfig = AppConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio-core", AbstractTestCaseJU4.getCoreLookup())
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.addModule(ModuleConfig.builder("Bio-spe", AbstractTestCaseJU4.getCoreLookup()) //This module depends of Bio-core module
						.addComponent(BioManager.class, BioManagerImpl.class)
						.build())
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			//
		}
	}
}
