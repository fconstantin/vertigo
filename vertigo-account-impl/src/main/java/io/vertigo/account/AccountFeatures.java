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
package io.vertigo.account;

import io.vertigo.account.account.AccountManager;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.impl.account.AccountCachePlugin;
import io.vertigo.account.impl.account.AccountDefinitionProvider;
import io.vertigo.account.impl.account.AccountStorePlugin;
import io.vertigo.account.impl.account.AccountManagerImpl;
import io.vertigo.account.impl.authentication.AuthenticationManagerImpl;
import io.vertigo.account.impl.authentication.AuthenticationPlugin;
import io.vertigo.account.impl.security.VSecurityManagerImpl;
import io.vertigo.account.plugins.account.cache.redis.RedisAccountCachePlugin;
import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Defines the 'account' extension
 * @author pchretien
 */
public final class AccountFeatures extends Features {

	/**
	 * Constructor.
	 */
	public AccountFeatures() {
		super("x-account");
	}

	/**
	 * Activates user session.
	 * @param userSessionClass the user session class
	 * @return these features
	 */
	public AccountFeatures withUserSession(final Class<? extends UserSession> userSessionClass) {
		getModuleConfigBuilder()
				.addComponent(VSecurityManager.class, VSecurityManagerImpl.class,
						Param.of("userSessionClassName", userSessionClass.getName()));
		return this;
	}

	/**
	 * Defines REDIS as the database to store the accounts
	 * @return the features
	 */
	public AccountFeatures withRedisAccountCachePlugin() {
		return withAccountCachePlugin(RedisAccountCachePlugin.class);
	}

	/**
	 * Defines a Authenticating realm.
	 * @param authenticatingRealmPluginClass
	 * @param params
	 * @return the features
	 */
	public AccountFeatures withAuthentication(final Class<? extends AuthenticationPlugin> authenticatingRealmPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(authenticatingRealmPluginClass, params);
		return this;
	}

	/**
	 * @param accountCachePluginClass
	 * @param params
	 * @return the features
	 */
	public AccountFeatures withAccountCachePlugin(final Class<? extends AccountCachePlugin> accountCachePluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(accountCachePluginClass, params);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addDefinitionProvider(AccountDefinitionProvider.class);
	}

	/**
	 * @param accountStorePluginClass
	 * @param params
	 * @return the features
	 */
	public AccountFeatures withAccountStorePlugin(final Class<? extends AccountStorePlugin> accountStorePluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addComponent(AuthenticationManager.class, AuthenticationManagerImpl.class)
				.addComponent(AccountManager.class, AccountManagerImpl.class)
				.addPlugin(accountStorePluginClass, params);
		return this;
	}

}
