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
package io.vertigo.core.param.properties;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.core.param.AbstractParamManagerTest;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.param.properties.PropertiesParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;

/**
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
public final class PropertiesParamManagerTest extends AbstractParamManagerTest {
	@Override
	protected AppConfig buildAppConfig() {
		final String locales = "fr_FR";

		// @formatter:off
		return AppConfig.builder()
			.beginBoot()
				.withLocales(locales)
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.addPlugin( PropertiesParamPlugin.class,
						Param.of("url", "io/vertigo/core/param/properties/app-config.properties"))
			.endBoot()
			.build();
		// @formatter:on
	}

}
