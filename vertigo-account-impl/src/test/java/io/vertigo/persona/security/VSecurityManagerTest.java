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
package io.vertigo.persona.security;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.persona.impl.security.BeanResourceNameFactory;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 * @author pchretien
 */
public final class VSecurityManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private VSecurityManager securityManager;

	@Test
	public void testCreateUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertEquals(Locale.FRANCE, userSession.getLocale());
		Assert.assertEquals(TestUserSession.class, userSession.getClass());
	}

	@Test
	public void testInitCurrentUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			Assert.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertFalse(userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assert.assertFalse(userSession.isPresent());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			//
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assert.assertFalse(securityManager.getCurrentUserSession().isPresent());
	}

	@Test
	public void testRole() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("R_ADMIN", Role.class);
		Assert.assertTrue("R_ADMIN".equals(admin.getName()));
		final Role secretary = definitionSpace.resolve("R_SECRETARY", Role.class);
		Assert.assertTrue("R_SECRETARY".equals(secretary.getName()));
	}

	@Test
	public void testAccess() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("R_ADMIN", Role.class);
		final Role manager = definitionSpace.resolve("R_MANAGER", Role.class);
		final Role secretary = definitionSpace.resolve("R_SECRETARY", Role.class);

		final UserSession userSession = securityManager.<PersonaUserSession> createUserSession()
				.addRole(admin)
				.addRole(manager);
		try {
			securityManager.startCurrentUserSession(userSession);

			final Set<Role> roles = new HashSet<>();
			roles.add(admin);
			roles.add(secretary);
			Assert.assertTrue(securityManager.hasRole(roles));

			roles.clear();
			roles.add(secretary);
			Assert.assertFalse(securityManager.hasRole(roles));

			roles.clear(); //Si aucun droit necessaire alors c'est bon
			Assert.assertTrue(securityManager.hasRole(roles));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNotAuthorized() {
		final Role reader = getRole("R_READER");
		final Role writer = getRole("R_WRITER");

		final UserSession userSession = securityManager.<PersonaUserSession> createUserSession()
				.addRole(reader)
				.addRole(writer);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean authorized = securityManager.isAuthorized("not", "authorized");
			Assert.assertFalse(authorized);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorized() {
		final Role reader = getRole("R_READER");
		final Role writer = getRole("R_WRITER");

		final UserSession userSession = securityManager.<PersonaUserSession> createUserSession()
				.addRole(reader)
				.addRole(writer);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canread = securityManager.isAuthorized("/products/12", "READ");
			Assert.assertTrue(canread);
			final boolean canwrite = securityManager.isAuthorized("/products/12", "WRITE");
			Assert.assertTrue(canwrite);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		final Role reader = getRole("R_READER");

		final UserSession userSession = securityManager.<PersonaUserSession> createUserSession()
				.addRole(reader);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canread = securityManager.isAuthorized("/products/12", "READ");
			Assert.assertTrue(canread);
			final boolean cannotwrite = securityManager.isAuthorized("/products/12", "WRITE");
			Assert.assertFalse(cannotwrite);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedAllWithResourceNameFactory() {
		securityManager.registerResourceNameFactory(Famille.class.getSimpleName(), new BeanResourceNameFactory("/famille/${famId}"));
		final Famille famille12 = new Famille();
		famille12.setFamId(12L);

		final Famille famille13 = new Famille();
		famille13.setFamId(13L);

		//Test toutes familles
		final Role readAllFamillies = getRole("R_ALL_FAMILLES");
		final UserSession userSession = securityManager.<PersonaUserSession> createUserSession()
				.addRole(readAllFamillies);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canRead12 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille12, "READ");
			Assert.assertTrue(canRead12);
			final boolean canRead13 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille13, "READ");
			Assert.assertTrue(canRead13);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedSessionPropertyWithResourceNameFactory() {
		securityManager.registerResourceNameFactory(Famille.class.getSimpleName(), new BeanResourceNameFactory("/famille/${famId}"));
		final Famille famille12 = new Famille();
		famille12.setFamId(12L);

		final Famille famille13 = new Famille();
		famille13.setFamId(13L);

		//Test ma famille
		final Role readMyFamilly = getRole("R_MY_FAMILLE");
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("famId", String.valueOf(famille12.id))
				.addRole(readMyFamilly);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canRead12 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille12, "READ");
			Assert.assertTrue(canRead12);
			final boolean canRead13 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille13, "READ");
			Assert.assertFalse(canRead13);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	private Role getRole(final String name) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(name, Role.class);
	}

	public static final class Famille {
		private long id;

		public void setFamId(final long id) {
			this.id = id;
		}

		public long getFamId() {
			return id;
		}
	}
}
