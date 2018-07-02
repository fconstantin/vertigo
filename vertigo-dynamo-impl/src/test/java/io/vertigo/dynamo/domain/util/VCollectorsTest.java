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
package io.vertigo.dynamo.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.dynamo.domain.data.domain.Artist;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 *
 * @author xdurand
 *
 */
public class VCollectorsTest extends AbstractTestCaseJU4 {

	/**
	 * Test du VCollectors.toDtList sur une liste vide
	 */
	@Test
	public void testCollectDtListEmpty() {
		final DtList<Artist> emptyDtList = new DtList<>(Artist.class);
		final DtList<Artist> listCollected = emptyDtList.stream().collect(VCollectors.toDtList(Artist.class));

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty());
		assertEquals(0, listCollected.size());
	}

	private static Artist createArtist(final long id, final String name) {
		final Artist m = new Artist();
		m.setId(id);
		m.setName(name);
		return m;
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide sans filtrage
	 */
	@Test
	public void testCollectDtList() {
		final Artist m1 = createArtist(1, "David Bowie");
		final Artist m2 = createArtist(2, "Joe Strummer");

		final DtList<Artist> dtList = DtList.of(m1, m2);
		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.sorted( (art1, art2) -> art1.getId().compareTo(art2.getId()))
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(2, listCollected.size());
		assertEquals(listCollected.get(0), m1);
		assertEquals(listCollected.get(1), m2);
		assertEquals(2, dtList.size());
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide avec filtrage
	 */
	@Test
	public void testFilterCollectDtList() {
		final Artist m1 = createArtist(1, "Louis Armstrong");
		final Artist m2 = createArtist(2, "Duke Ellington");
		final Artist m3 = createArtist(3, "Jimmy Hendricks");

		final DtList<Artist> dtList = DtList.of(m1, m2, m3);

		// @formatter:off
		final DtList<Artist> listCollected = dtList.stream()
											.filter( m -> m.getId() % 2 == 0)
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on
		assertNotNull(listCollected);
		Assert.assertFalse(listCollected.isEmpty());
		assertEquals(1, listCollected.size());
		assertEquals(listCollected.get(0), m2);
		assertEquals(3, dtList.size());
	}

}
