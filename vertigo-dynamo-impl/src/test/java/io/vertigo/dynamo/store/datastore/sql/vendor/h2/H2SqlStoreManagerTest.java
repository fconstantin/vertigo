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
package io.vertigo.dynamo.store.datastore.sql.vendor.h2;

import org.h2.Driver;

import io.vertigo.app.config.AppConfig;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.dynamo.store.datastore.sql.AbstractSqlStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.SqlDataStoreAppConfig;

/**
 * Test of sql storage in H2 DB.
 * @author mlaroche
 *
 */
public final class H2SqlStoreManagerTest extends AbstractSqlStoreManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return SqlDataStoreAppConfig.build(
				H2DataBase.class.getCanonicalName(),
				Driver.class.getCanonicalName(),
				"jdbc:h2:mem:database");
	}

}
