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
package io.vertigo.dynamo.environment.oom;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 * Test de lecture d'un OOM.
 *
 * @author npiedeloup
 */
public final class OOMParserIdentifiersTest extends AbstractTestCaseJU4 {
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "managers-test.xml", "resources-test.xml" };
	}

	private DtDefinition getDtDefinition(final String urn) {
		return getApp().getDefinitionSpace().resolve(urn, DtDefinition.class);
	}

	@Test
	public void testIdentifiersVsPrimaryKey() {
		final DtDefinition loginDefinition = getDtDefinition("DT_LOGIN");
		Assert.assertTrue(loginDefinition.getIdField().isPresent());
	}
}
