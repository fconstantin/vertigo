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
package io.vertigo.core.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
public final class ResourceManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ResourceManager resourceManager;
	final String locales = "fr_FR";

	@Override
	protected AppConfig buildAppConfig() {
		//@formatter:off
		return AppConfig.builder()
			.beginBoot()
				.withLocales(locales)
				.addPlugin(ClassPathResourceResolverPlugin.class)
			.endBoot()
			.build();
		// @formatter:on
	}

	@Test
	public void testNullResource() {
		Assertions.assertThrows(NullPointerException.class,
				() -> resourceManager.resolve(null));

	}

	@Test
	public void testEmptyResource() {
		Assertions.assertThrows(RuntimeException.class,
				() -> resourceManager.resolve("nothing"));
	}

	@Test
	public void testResourceSelector() {
		final String expected = "io/vertigo/core/resource/hello.properties";
		final URL url = resourceManager.resolve(expected);
		assertTrue(url.getPath().contains(expected));
	}
}
