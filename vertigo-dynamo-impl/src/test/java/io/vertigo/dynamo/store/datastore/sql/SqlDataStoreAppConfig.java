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
package io.vertigo.dynamo.store.datastore.sql;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.plugins.sql.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;
import io.vertigo.dynamo.plugins.store.filestore.db.DbFileStorePlugin;
import io.vertigo.dynamo.store.StoreManagerInitializer;

/**
 * AppConfig builder for SqlStore tests. (Params for db specificities)
 * @author mlaroche
 *
 */
public class SqlDataStoreAppConfig {

	public static AppConfig build(final String dataBaseClass, final String jdbcDriver, final String jdbcUrl) {
		return AppConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withScript()
						.withCache(MemoryCachePlugin.class)
						.build())
				.addModule(new DatabaseFeatures()
						.withSqlDataBase()
						.addSqlConnectionProviderPlugin(C3p0ConnectionProviderPlugin.class,
								Param.of("dataBaseClass", dataBaseClass),
								Param.of("jdbcDriver", jdbcDriver),
								Param.of("jdbcUrl", jdbcUrl))
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.addDataStorePlugin(SqlDataStorePlugin.class)
						.addFileStorePlugin(DbFileStorePlugin.class,
								Param.of("storeDtName", "DT_VX_FILE_INFO"))
						.build())
				.addModule(ModuleConfig.builder("definition")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addDefinitionResource("kpr", "io/vertigo/dynamo/store/data/executionWfileinfo.kpr")
								.addDefinitionResource("classes", "io.vertigo.dynamo.store.data.DtDefinitions")
								.build())
						.build())
				.addInitializer(StoreManagerInitializer.class)
				.build();

	}

}
