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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.plugins.environment.loaders.xml.AbstractXmlLoader;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlClass;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.util.StringUtil;

/**
 * Loader de fichier XMI version Enterprise Architect.
 * @author pforhan
 */
public final class EAXmiLoader extends AbstractXmlLoader {
	private final Map<XmlId, EAXmiObject> map = new LinkedHashMap<>();

	private static final Logger LOG = LogManager.getLogger(EAXmiLoader.class);

	/**
	 * Constructor.
	 * @param resourceManager the vertigo resourceManager
	 */
	public EAXmiLoader(final ResourceManager resourceManager) {
		super(resourceManager);
	}

	@Override
	protected DefaultHandler getHandler() {
		return new EAXmiHandler(map);
	}

	/**
	 * Récupération des classes déclarées dans le XMI.
	 * @return Liste des classes
	 */
	@Override
	public List<XmlClass> getClasses() {
		return map.values()
				.stream()
				.peek(obj -> LOG.debug("class : " + obj))
				//On ne conserve que les classes et les domaines
				.filter(obj -> obj.getType() == EAXmiType.Class)
				.map(EAXmiLoader::createClass)
				.collect(Collectors.toList());
	}

	/**
	 * Récupération des associations déclarées dans le XMI.
	 * @return Liste des associations
	 */
	@Override
	public List<XmlAssociation> getAssociations() {
		return map.values()
				.stream()
				.filter(obj -> obj.getType() == EAXmiType.Association)
				.map(this::createAssociation)
				.collect(Collectors.toList());
	}

	private static XmlClass createClass(final EAXmiObject obj) {
		LOG.debug("Creation de classe : " + obj.getName());
		//On recherche les attributs (>DtField) de cette classe(>Dt_DEFINITION)
		final String code = obj.getName().toUpperCase(Locale.ENGLISH);
		final String packageName = obj.getParent().getPackageName();
		final String stereotype = obj.getStereotype();

		final List<XmlAttribute> keyAttributes = new ArrayList<>();
		final List<XmlAttribute> fieldAttributes = new ArrayList<>();
		for (final EAXmiObject child : obj.getChildren()) {
			if (child.getType() == EAXmiType.Attribute) {
				LOG.debug("Attribut = " + child.getName() + " isId = " + Boolean.toString(child.getIsId()));
				if (child.getIsId()) {
					final XmlAttribute attributeXmi = createAttribute(child, true);
					keyAttributes.add(attributeXmi);
				} else {
					fieldAttributes.add(createAttribute(child, false));
				}
			}
		}
		return new XmlClass(code, packageName, stereotype, keyAttributes, fieldAttributes);
	}

	private static XmlAttribute createAttribute(final EAXmiObject obj, final boolean isPK) {
		final String code = obj.getName().toUpperCase(Locale.ENGLISH);
		final String label = obj.getLabel();
		final boolean persistent = true;

		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		// L'information de persistence ne peut pas être déduite du Xmi, tous les champs sont déclarés persistent de facto
		return new XmlAttribute(code, label, persistent, notNull, obj.getDomain());
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association
	 */
	private XmlAssociation createAssociation(final EAXmiObject obj) {
		LOG.debug("Créer association :" + obj.getName());
		//On recherche les objets référencés par l'association.
		final EAXmiObject objectB = map.get(obj.getClassB());
		final EAXmiObject objectA = map.get(obj.getClassA());

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}

		final String code = obj.getName().toUpperCase(Locale.ENGLISH);
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToUpperCamelCase(objectA.getName());
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToUpperCamelCase(objectB.getName());
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = objectA.getName().toUpperCase(Locale.ENGLISH);
		final String codeB = objectB.getName().toUpperCase(Locale.ENGLISH);

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
		final boolean navigabilityA = obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability();
		return new XmlAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}

	@Override
	public String getType() {
		return "xmi";
	}

}
