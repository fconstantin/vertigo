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
package io.vertigo.account.account;

import java.util.Optional;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.account.model.DtDefinitions;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.plugins.account.cache.memory.MemoryAccountCachePlugin;
import io.vertigo.account.plugins.account.store.datastore.StoreAccountStorePlugin;
import io.vertigo.account.plugins.account.store.text.TextAccountStorePlugin;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;
import io.vertigo.database.impl.sql.vendor.h2.H2DataBase;
import io.vertigo.database.plugins.sql.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;

public final class MyAppConfig {
	private static final String REDIS_HOST = "redis-pic.part.klee.lan.net";
	private static final int REDIS_PORT = 6379;
	private static final int REDIS_DATABASE = 15;

	public static AppConfig config(final boolean redis, final boolean database) {
		final CommonsFeatures commonsFeatures = new CommonsFeatures()
				.withScript()
				.withCache(MemoryCachePlugin.class);
		final DatabaseFeatures databaseFeatures = new DatabaseFeatures();
		final DynamoFeatures dynamoFeatures = new DynamoFeatures();
		final AccountFeatures accountFeatures = new AccountFeatures()
				.withUserSession(TestUserSession.class);

		if (database) {
			databaseFeatures
					.withSqlDataBase()
					.addSqlConnectionProviderPlugin(C3p0ConnectionProviderPlugin.class,
							Param.of("dataBaseClass", H2DataBase.class.getName()),
							Param.of("jdbcDriver", "org.h2.Driver"),
							Param.of("jdbcUrl", "jdbc:h2:mem:database"));

			dynamoFeatures
					.withStore()
					.addDataStorePlugin(SqlDataStorePlugin.class);

			accountFeatures.withAccountStorePlugin(StoreAccountStorePlugin.class,
					Param.of("userIdentityEntity", "DT_USER"),
					Param.of("groupIdentityEntity", "DT_USER_GROUP"),
					Param.of("userAuthField", "EMAIL"),
					Param.of("userToAccountMapping", "id:USR_ID, displayName:FULL_NAME, email:EMAIL, authToken:EMAIL"),
					Param.of("groupToGroupAccountMapping", "id:GRP_ID, displayName:NAME"));
		} else {
			accountFeatures.withAccountStorePlugin(TextAccountStorePlugin.class,
					Param.of("accountFilePath", "io/vertigo/account/data/identities.txt"),
					Param.of("accountFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<email>(?<authToken>[^;@]+)@[^;]+);(?<photoUrl>.*)$"),
					Param.of("groupFilePath", "io/vertigo/account/data/groups.txt"),
					Param.of("groupFilePattern", "^(?<id>[^;]+);(?<displayName>[^;]+);(?<accountIds>.*)$"));
		}

		if (redis) {
			commonsFeatures.withRedisConnector(REDIS_HOST, REDIS_PORT, REDIS_DATABASE, Optional.empty());
			accountFeatures.withRedisAccountCachePlugin();
		} else {
			//else we use memory
			accountFeatures.withAccountCachePlugin(MemoryAccountCachePlugin.class);

		}
		return AppConfig.builder()
				.beginBoot()
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(commonsFeatures.build())
				.addModule(databaseFeatures.build())
				.addModule(dynamoFeatures.build())
				.addModule(accountFeatures.build())
				.addModule(ModuleConfig.builder("app")
						.addDefinitionProvider(
								DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
										.addDefinitionResource("classes", DtDefinitions.class.getName())
										.addDefinitionResource("kpr", "account/domains.kpr")
										.build())
						.build())
				.build();
	}

}
