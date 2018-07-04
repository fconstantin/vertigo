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
package io.vertigo.dynamo.search.multiindex;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Item;
import io.vertigo.dynamo.search.data.domain.ItemDataBase;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;

/**
 * @author  npiedeloup
 */
public class SearchManagerMultiIndexTest extends AbstractTestCaseJU4 {
	//Index
	private static final String IDX_DYNA_ITEM = "IDX_DYNA_ITEM";
	private static final String IDX_ITEM = "IDX_ITEM";

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;

	private ItemDataBase itemDataBase;

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		itemDataBase = new ItemDataBase();
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction mais sur deux indexes.
	 * Vérifie la capacité du système à gérer plusieurs indexes.
	 */
	@Test
	public void testIndex() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final SearchIndexDefinition itemIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);
		final SearchIndexDefinition itemDynIndexDefinition = definitionSpace.resolve(IDX_DYNA_ITEM, SearchIndexDefinition.class);

		for (final Item item : itemDataBase.getAllItems()) {
			final SearchIndex<Item, Item> index = SearchIndex.createIndex(itemIndexDefinition, item.getURI(), item);
			searchManager.put(itemIndexDefinition, index);

			final SearchIndex<Item, Item> index2 = SearchIndex.createIndex(itemDynIndexDefinition, item.getURI(), item);
			searchManager.put(itemDynIndexDefinition, index2);
		}
		waitIndexation();

		final long size = query("*:*", itemIndexDefinition);
		Assert.assertEquals(itemDataBase.size(), size);

		final long sizeDyn = query("*:*", itemDynIndexDefinition);
		Assert.assertEquals(itemDataBase.size(), sizeDyn);
	}

	/**
	 * Test de création nettoyage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testClean() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final SearchIndexDefinition itemIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);
		final SearchIndexDefinition itemDynIndexDefinition = definitionSpace.resolve(IDX_DYNA_ITEM, SearchIndexDefinition.class);
		final ListFilter removeQuery = ListFilter.of("*:*");
		searchManager.removeAll(itemIndexDefinition, removeQuery);
		searchManager.removeAll(itemDynIndexDefinition, removeQuery);
		waitIndexation();

		final long size = query("*:*", itemIndexDefinition);
		Assert.assertEquals(0, size);

		final long sizeDyn = query("*:*", itemDynIndexDefinition);
		Assert.assertEquals(0, sizeDyn);
	}

	private long query(final String query, final SearchIndexDefinition indexDefinition) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(ListFilter.of(query))
				.build();
		final FacetedQueryResult<DtObject, SearchQuery> result = searchManager.loadList(indexDefinition, searchQuery, null);
		return result.getCount();
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(2000); //wait index was done
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}
}
