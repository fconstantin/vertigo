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
package io.vertigo.dynamo.impl.search;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ClassUtil;

/**
 * Reindex all data taks.
 * @author npiedeloup (27 juil. 2015 14:35:14)
 * @param <S> KeyConcept type
 */
final class ReindexAllTask<S extends KeyConcept> implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger(ReindexAllTask.class);
	private static volatile boolean REINDEXATION_IN_PROGRESS;
	private static volatile long REINDEX_COUNT;
	private final WritableFuture<Long> reindexFuture;
	private final SearchIndexDefinition searchIndexDefinition;
	private final SearchManager searchManager;

	/**
	 * Constructor.
	 * @param searchIndexDefinition Search index definition
	 * @param reindexFuture Future for result
	 * @param searchManager Search manager
	 */
	ReindexAllTask(final SearchIndexDefinition searchIndexDefinition, final WritableFuture<Long> reindexFuture, final SearchManager searchManager) {
		Assertion.checkNotNull(searchIndexDefinition);
		Assertion.checkNotNull(reindexFuture);
		Assertion.checkNotNull(searchManager);
		//-----
		this.searchIndexDefinition = searchIndexDefinition;
		this.reindexFuture = reindexFuture;
		this.searchManager = searchManager;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		if (isReindexInProgress()) {
			final String warnMessage = "Reindexation of " + searchIndexDefinition.getName() + " is already in progess (" + getReindexCount() + " elements done)";
			LOGGER.warn(warnMessage);
			reindexFuture.fail(new VSystemException(warnMessage));
		} else {
			//-----
			startReindex();
			long reindexCount = 0;
			final long startTime = System.currentTimeMillis();
			try {
				final Class<S> keyConceptClass = (Class<S>) ClassUtil.classForName(searchIndexDefinition.getKeyConceptDtDefinition().getClassCanonicalName(), KeyConcept.class);
				final SearchLoader<S, DtObject> searchLoader = Home.getApp().getComponentSpace().resolve(searchIndexDefinition.getSearchLoaderId(), SearchLoader.class);
				String lastUri = null;
				LOGGER.info("Reindexation of " + searchIndexDefinition.getName() + " started");

				for (final SearchChunk<S> searchChunk : searchLoader.chunk(keyConceptClass)) {
					final Collection<SearchIndex<S, DtObject>> searchIndexes = searchLoader.loadData(searchChunk);

					final String maxUri = String.valueOf(searchChunk.getLastURI().toString());
					Assertion.checkState(!maxUri.equals(lastUri), "SearchLoader ({0}) error : return the same uri list", searchIndexDefinition.getSearchLoaderId());
					searchManager.removeAll(searchIndexDefinition, urisRangeToListFilter(lastUri, maxUri));
					if (!searchIndexes.isEmpty()) {
						searchManager.putAll(searchIndexDefinition, searchIndexes);
					}
					lastUri = maxUri;
					reindexCount += searchChunk.getAllURIs().size();
					updateReindexCount(reindexCount);
				}
				//On ne retire pas la fin, il y a un risque de retirer les données ajoutées depuis le démarrage de l'indexation
				reindexFuture.success(reindexCount);
			} catch (final Exception e) {
				LOGGER.error("Reindexation error", e);
				reindexFuture.fail(e);
			} finally {
				stopReindex();
				LOGGER.info("Reindexation of " + searchIndexDefinition.getName() + " finished in " + (System.currentTimeMillis() - startTime) + "ms (" + reindexCount + " elements done)");
			}
		}
	}

	private static boolean isReindexInProgress() {
		return REINDEXATION_IN_PROGRESS;
	}

	private static void startReindex() {
		REINDEXATION_IN_PROGRESS = true;
	}

	private static void stopReindex() {
		REINDEXATION_IN_PROGRESS = false;
	}

	private static void updateReindexCount(final long reindexCount) {
		REINDEX_COUNT = reindexCount;
	}

	private static long getReindexCount() {
		return REINDEX_COUNT;
	}

	private static ListFilter urisRangeToListFilter(final String firstUri, final String lastUri) {
		final String filterValue = new StringBuilder()
				.append("urn").append(":{") //{ for exclude min
				.append(firstUri != null ? ("\"" + firstUri + "\"") : "*")
				.append(" TO ")
				.append(lastUri != null ? ("\"" + lastUri + "\"") : "*")
				.append("]")
				.toString();
		return ListFilter.of(filterValue);
	}
}
