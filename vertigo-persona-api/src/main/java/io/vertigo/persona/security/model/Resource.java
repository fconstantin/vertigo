/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Repr�sente une ressource. Une ressource peut �tre un �l�ment de l'interface graphique,
 * une page, un service ou encore une donn�e. 
 */

/**
 * Le sch�ma d'identification des ressources est sp�cifique � chaque projet.
 * 
 * Note : L'attribut 'filter' est une expression r�guli�re permettant de 'collecter' une liste de
 * ressources.
 * 
 * @author prahmoune
 * @version $Id: Resource.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
 */
@Prefix("RSR_")
public final class Resource implements Definition {
	private final String name;
	private final String filter;
	private final String description;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de la ressource
	 * @param filter Filtre associ�
	 * @param description Description de la ressource
	 */
	public Resource(final String name, final String filter, final String description) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(filter);
		Assertion.checkArgNotEmpty(description);
		// ---------------------------------------------------------------------
		this.name = name;
		this.filter = filter;
		this.description = description;
	}

	/**
	 * @return Filtre
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Nom de la ressouce
	 */
	public String getName() {
		return name;
	}

}