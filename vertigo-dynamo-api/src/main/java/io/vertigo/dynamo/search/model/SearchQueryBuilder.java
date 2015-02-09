package io.vertigo.dynamo.search.model;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.Arrays;

/**
 * @author pchretien
 */
public final class SearchQueryBuilder implements Builder<SearchQuery> {
	private final ListFilter myListFilter;
	//-----
	private DtField mySortField;
	private boolean mySortAsc;
	//-----
	private DtField myBoostedDocumentDateField;
	private Integer myNumDaysOfBoostRefDocument;
	private Integer myMostRecentBoost;
	private FacetedQuery myFacetedQuery;

	/**
	 * Constructor.
	 * @param query Query
	 */
	public SearchQueryBuilder(final String query) {
		Assertion.checkNotNull(query);
		//-----
		myListFilter = new ListFilter(query);
	}

	/**
	 * @param sortField Champ utilisé pour le tri
	 * @param sortAsc  Ordre de tri (true pour ascendant)
	*/
	public SearchQueryBuilder withSortStrategy(final DtField sortField, final boolean sortAsc) {
		Assertion.checkNotNull(sortField);
		//-----
		mySortField = sortField;
		mySortAsc = sortAsc;
		return this;
	}

	/**
	 * Defines Boost strategy  including most recents docs.
	 * @param boostedDocumentDateField Nom du champ portant la date du document (null si non utilisé)
	 * @param numDaysOfBoostRefDocument Age des documents servant de référence pour le boost des plus récents par rapport à eux (null si non utilisé)
	 * @param mostRecentBoost Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1) (null si non utilisé)
	 * @return SearchQuery.
	 */
	public SearchQueryBuilder withBoostStrategy(final DtField boostedDocumentDateField, final int numDaysOfBoostRefDocument, final int mostRecentBoost) {
		Assertion.checkNotNull(boostedDocumentDateField);
		Assertion.checkArgument(numDaysOfBoostRefDocument > 1 && mostRecentBoost > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieurs à 1.");
		//-----
		myBoostedDocumentDateField = boostedDocumentDateField;
		myNumDaysOfBoostRefDocument = numDaysOfBoostRefDocument;
		myMostRecentBoost = mostRecentBoost;
		return this;
	}

	public SearchQueryBuilder withFacetStrategy(final FacetedQueryDefinition facetedQueryDefinition, final ListFilter... listFilters) {
		return this.withFacetStrategy(new FacetedQuery(facetedQueryDefinition, Arrays.asList(listFilters)));
	}

	public SearchQueryBuilder withFacetStrategy(final FacetedQuery facetedQuery) {
		Assertion.checkNotNull(facetedQuery);
		//-----
		myFacetedQuery = facetedQuery;
		return this;
	}

	@Override
	public SearchQuery build() {
		return new SearchQuery(myFacetedQuery, myListFilter, mySortField, mySortAsc, myBoostedDocumentDateField, myNumDaysOfBoostRefDocument, myMostRecentBoost);
	}

}
