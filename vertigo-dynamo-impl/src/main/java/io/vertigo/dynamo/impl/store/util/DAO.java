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
package io.vertigo.dynamo.impl.store.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.Criterions;
import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.Fragment;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

/**
 * Classe utilitaire pour accéder au Broker.
 *
 * @author cgodard
 * @param <E> the type of entity
 * @param <P> Type de la clef primaire.
 */
public class DAO<E extends Entity, P> implements BrokerNN {

	/** DT de l'objet dont on gére le CRUD. */
	private final Class<? extends Entity> entityClass;
	protected final DataStore dataStore;
	private final BrokerNN brokerNN;
	private final BrokerBatch<E, P> brokerBatch;
	private final TaskManager taskManager;

	/**
	 * Contructeur.
	 *
	 * @param entityClass Définition du DtObject associé à ce DAO
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAO(final Class<? extends Entity> entityClass, final StoreManager storeManager, final TaskManager taskManager) {
		Assertion.checkNotNull(entityClass);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(taskManager);
		//-----
		this.entityClass = entityClass;
		dataStore = storeManager.getDataStore();
		this.taskManager = taskManager;
		brokerNN = new BrokerNNImpl(taskManager);
		brokerBatch = new BrokerBatchImpl<>(taskManager);
	}

	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	public final BrokerBatch<E, P> getBatch() {
		return brokerBatch;
	}

	/**
	 * Saves an object and returns the saved object
	 *
	 * @param entity Object to save
	 * @return the saved entity
	 */
	public final E save(final E entity) {
		if (DtObjectUtil.getId(entity) == null) {
			return dataStore.create(entity);
		}

		dataStore.update(entity);
		return entity;
	}

	/**
	 * Creates an object.
	 *
	 * @param entity Object to create
	 * @return the created entity
	 */
	public final E create(final E entity) {
		return dataStore.create(entity);
	}

	/**
	 * Update an object.
	 *
	 * @param entity Object to update
	 */
	public final void update(final E entity) {
		dataStore.update(entity);
	}

	/**
	 * Reloads entity from fragment, and keep fragment modifications.
	 *
	 * @param fragment  merged from datastore and input
	 * @return merged root entity merged with the fragment
	 */
	public final E reloadAndMerge(final Fragment<E> fragment) {
		final DtDefinition fragmentDefinition = DtObjectUtil.findDtDefinition(fragment);
		final DtDefinition entityDefinition = fragmentDefinition.getFragment().get();
		final Map<String, DtField> entityFields = indexFields(entityDefinition.getFields());
		final DtField idField = entityDefinition.getIdField().get();
		final P entityId = (P) idField.getDataAccessor().getValue(fragment);//etrange on utilise l'accessor de l'entity sur le fragment
		final E dto = get(entityId);
		for (final DtField fragmentField : fragmentDefinition.getFields()) {
			//On vérifie la présence du champ dans l'Entity (il peut s'agir d'un champ non persistent d'UI
			if (entityFields.containsKey(fragmentField.getName())) {
				final DataAccessor fragmentDataAccessor = fragmentField.getDataAccessor();
				final DataAccessor entityDataAccessor = entityFields.get(fragmentField.getName()).getDataAccessor();
				entityDataAccessor.setValue(dto, fragmentDataAccessor.getValue(fragment));
			}
		}
		return dto;
	}

	private static Map<String, DtField> indexFields(final List<DtField> fields) {
		return fields
				.stream()
				.collect(Collectors.toMap(DtField::getName, Function.identity()));
	}

	/**
	 * Suppression d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	public final void delete(final URI<E> uri) {
		dataStore.delete(uri);
	}

	/**
	 * Suppression d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant à supprimer
	 */
	public final void delete(final P id) {
		delete(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un objet persistant par son URI. L'objet doit exister.
	 *
	 * @param uri URI de l'objet à récupérer
	 * @return D Object recherché
	 */
	public final E get(final URI<E> uri) {
		return dataStore.readOne(uri);
	}

	/**
	 * Récupération d'un fragment persistant par son URI. L'objet doit exister.
	 *
	 * @param uri URI de l'objet à récupérer
	 * @param fragmentClass Fragment class
	 * @return F Fragment recherché
	 */
	public final <F extends Fragment<E>> F getFragment(final URI<E> uri, final Class<F> fragmentClass) {
		final E dto = dataStore.readOne(uri);
		final DtDefinition fragmentDefinition = DtObjectUtil.findDtDefinition(fragmentClass);
		final F fragment = fragmentClass.cast(DtObjectUtil.createDtObject(fragmentDefinition));
		for (final DtField dtField : fragmentDefinition.getFields()) {
			final DataAccessor dataAccessor = dtField.getDataAccessor();
			dataAccessor.setValue(fragment, dataAccessor.getValue(dto));
			//etrange on utilise l'accessor du fragment sur l'entity
		}
		return fragment;
	}

	/**
	 * Récupération d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return D Object objet recherché
	 */
	public final E get(final P id) {
		return get(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un fragment persistant par son identifiant.<br>
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @param fragmentClass Fragment class
	 * @return D Fragment recherché
	 */
	public final <F extends Fragment<E>> F get(final P id, final Class<F> fragmentClass) {
		return getFragment(new URI<E>(DtObjectUtil.findDtDefinition(fragmentClass).getFragment().get(), id), fragmentClass);
	}

	/**
	 * Retourne l'URI de DtObject correspondant à une URN de définition et une valeur d'URI donnés.
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return URI recherchée
	 */
	protected final URI<E> createDtObjectURI(final P id) {
		return new URI<>(getDtDefinition(), id);
	}

	/**
	 * @param dtFieldName de l'object à récupérer NOT NULL
	 * @param value de l'object à récupérer NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<E> getListByDtFieldName(final DtFieldName dtFieldName, final Serializable value, final int maxRows) {
		final Criteria<E> criteria = Criterions.isEqualTo(dtFieldName, value);
		// Verification de la valeur est du type du champ
		final DtDefinition dtDefinition = getDtDefinition();
		dtDefinition.getField(dtFieldName.name()).getDomain().checkValue(value);
		return dataStore.findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/**
	 * Find one and only one object matching the criteria.
	 * If there are many results or no result an exception is thrown
	 * @param criteria the filter criteria
	 * @return  the result
	 */
	public final E find(final Criteria<E> criteria) {
		return findOptional(criteria)
				.orElseThrow(() -> new NullPointerException("No data found"));
	}

	/**
	 * Find one or zero object matching the criteria.
	 * If there are many results an exception is thrown
	 * @param criteria the filter criteria
	 * @return  the optional result
	 */
	public final Optional<E> findOptional(final Criteria<E> criteria) {
		final DtList<E> list = dataStore.findAll(new DtListURIForCriteria<>(getDtDefinition(), criteria, 2));
		Assertion.checkState(list.size() <= 1, "Too many results");
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	/**
	 * @param criteria Thr criteria
	 * @param maxRows Max rows
	 * @return DtList<D> result NOT NULL
	 */
	public final DtList<E> findAll(final Criteria<E> criteria, final int maxRows) {
		return dataStore.findAll(new DtListURIForCriteria<>(getDtDefinition(), criteria, maxRows));
	}

	/** {@inheritDoc} */
	@Override
	public final void removeAllNN(final DtListURIForNNAssociation dtListURI) {
		brokerNN.removeAllNN(dtListURI);
	}

	/** {@inheritDoc} */
	@Override
	public final void removeNN(final DtListURIForNNAssociation dtListURI, final URI uriToDelete) {
		brokerNN.removeNN(dtListURI, uriToDelete);
	}

	/**
	 * Mise à jour des associations n-n.
	 *
	 * @param <FK> <FK extends DtObject>
	 * @param dtListURI DtList de référence
	 * @param newDtc DtList modifiée
	 */
	public final <FK extends Entity> void updateNN(final DtListURIForNNAssociation dtListURI, final DtList<FK> newDtc) {
		Assertion.checkNotNull(newDtc);
		//-----
		final List<URI> objectURIs = newDtc
				.stream()
				.map(DtObjectUtil::createURI)
				.collect(Collectors.toList());
		updateNN(dtListURI, objectURIs);
	}

	/** {@inheritDoc} */
	@Override
	public final void updateNN(final DtListURIForNNAssociation dtListURI, final List<URI> newUriList) {
		brokerNN.updateNN(dtListURI, newUriList);
	}

	/** {@inheritDoc} */
	@Override
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final URI uriToAppend) {
		brokerNN.appendNN(dtListURI, uriToAppend);
	}

	/**
	 * Ajout un objet à la collection existante.
	 *
	 * @param dtListURI DtList de référence
	 * @param entity the entity to append
	 */
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final Entity entity) {
		brokerNN.appendNN(dtListURI, entity.getURI());
	}

	private DtDefinition getDtDefinition() {
		return DtObjectUtil.findDtDefinition(entityClass);
	}
}
