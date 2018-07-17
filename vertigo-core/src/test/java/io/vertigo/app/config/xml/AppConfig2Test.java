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
package io.vertigo.app.config.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.core.component.BioManager;
import io.vertigo.util.AbstractTestCaseJU4;

@RunWith(JUnitPlatform.class)
public final class AppConfig2Test {
	@Test
	public void HomeTest() {

		final AppConfig appConfig = new XMLAppConfigBuilder()
				.withModules(getClass(), new Properties(), Map.of("bio", AbstractTestCaseJU4.getCoreLookup()), "bio.xml")
				.build();

		testBioManager(appConfig);
	}

	@Test
	public void FeatureTest() {

		final AppConfig appConfig = new XMLAppConfigBuilder()
				.withModules(getClass(), new Properties(), Map.of("bio", AbstractTestCaseJU4.getCoreLookup()),  "bio-features.xml")
				.build();

		testBioManager(appConfig);

	}

	@Test
	public void nodeTest() {

		final AppConfig appConfig = new XMLAppConfigBuilder()
				.withModules(getClass(), new Properties(), Map.of("bio", AbstractTestCaseJU4.getCoreLookup()), "bio-node.xml")
				.build();

		testBioManager(appConfig);
	}

	private void testBioManager(final AppConfig appConfig) {
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			assertEquals(app, app);
			assertTrue(app.getComponentSpace().contains("bioManager"));
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}
}
