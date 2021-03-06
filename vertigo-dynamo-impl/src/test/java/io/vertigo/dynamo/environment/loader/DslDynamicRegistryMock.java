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
package io.vertigo.dynamo.environment.loader;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinition;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.dsl.dynamic.DynamicRegistry;
import io.vertigo.dynamo.plugins.environment.dsl.entity.DslGrammar;
import io.vertigo.lang.Assertion;

/**
 * Mock pour les tests de regles sur les Definitions.
 * @author npiedeloup
 */
public final class DslDynamicRegistryMock implements DynamicRegistry {

	private DslDynamicRegistryMock() {
		//constructeur private
	}

	/**
	 * @return DynamicDefinitionRepository bouchon pour test
	 */
	public static DslDefinitionRepository createDynamicDefinitionRepository() {
		return new DslDefinitionRepository(new DslDynamicRegistryMock());
	}

	@Override
	public DslGrammar getGrammar() {
		return new PersonGrammar();
	}

	@Override
	public DefinitionSupplier supplyDefinition(final DslDefinition definition) {
		return (definitionSpace) -> new FakeDefinition(definition.getName());
	}

	@DefinitionPrefix("MOCK_")
	public final static class FakeDefinition implements Definition {
		private final String name;

		FakeDefinition(final String name) {
			Assertion.checkArgNotEmpty(name);
			//-----
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
