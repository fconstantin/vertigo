package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.WORD;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XPropertyEntry;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

final class InnerDefinitionRule extends AbstractRule<XDefinitionEntry, List<?>> {
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final String entityName;
	private final Entity entity;

	InnerDefinitionRule(final DynamicDefinitionRepository dynamicModelRepository, final String entityName, final Entity entity) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkArgNotEmpty(entityName);
		Assertion.checkNotNull(entity);
		//-----------------------------------------------------------------
		this.dynamicModelRepository = dynamicModelRepository;
		this.entityName = entityName;
		this.entity = entity;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicModelRepository, entity);
		return new SequenceRule(//"InnerDefinition"
				new TermRule(entityName), //
				SPACES,//
				WORD,//2
				SPACES,//
				definitionBodyRule,//4
				SPACES,//
				new OptionRule<>(SEPARATOR)//
		);
	}

	@Override
	protected XDefinitionEntry handle(final List<?> parsing) {
		//Dans le cas des sous définition :: field [PRD_XXX]

		final String definitionName = (String) parsing.get(2);
		final XDefinitionBody definitionBody = (XDefinitionBody) parsing.get(4);

		final DynamicDefinitionBuilder dynamicDefinitionBuilder = dynamicModelRepository.createDynamicDefinition(definitionName, entity, null);
		populateDefinition(definitionBody, dynamicDefinitionBuilder);

		//---
		return new XDefinitionEntry(entityName, dynamicDefinitionBuilder.build());
	}

	/**
	 * Peuple la définition à partir des éléments trouvés.
	 */
	private static void populateDefinition(final XDefinitionBody definitionBody, final DynamicDefinitionBuilder dynamicDefinitionBuilder) {
		for (final XDefinitionEntry fieldDefinitionEntry : definitionBody.getDefinitionEntries()) {
			// ------------------------------------------------------------------
			// 1.On vérifie que le champ existe pour la metaDefinition
			// et qu'elle n'est pas déjà enregistrée sur l'objet.
			// ------------------------------------------------------------------
			if (fieldDefinitionEntry.containsDefinition()) {
				// On ajoute la définition par sa valeur.
				dynamicDefinitionBuilder.addChildDefinition(fieldDefinitionEntry.getFieldName(), fieldDefinitionEntry.getDefinition());
			} else {
				// On ajoute les définitions par leur clé.
				dynamicDefinitionBuilder.addDefinitionList(fieldDefinitionEntry.getFieldName(), toDefinitionKeyList(fieldDefinitionEntry.getDefinitionKeys()));
			}
		}
		for (final XPropertyEntry fieldPropertyEntry : definitionBody.getPropertyEntries()) {
			//			// On vérifie que la propriété est enregistrée sur la metaDefinition
			//			Assertion.precondition(definition.getEntity().getPropertySet().contains(fieldPropertyEntry.getProperty()), "Propriété {0} non enregistré sur {1}",
			//					fieldPropertyEntry.getProperty(), definition.getEntity().getName());
			//			// ------------------------------------------------------------------
			final Object value = readProperty(fieldPropertyEntry.getProperty(), fieldPropertyEntry.getPropertyValueAsString());
			dynamicDefinitionBuilder.putPropertyValue(fieldPropertyEntry.getProperty(), value);
		}
	}

	//=========================================================================
	//=================================STATIC==================================
	//=========================================================================

	/**
	 * Retourne la valeur typée en fonction de son expression sous forme de String
	 * L'expression est celle utilisée dans le fichier xml/ksp.
	 * Cette méthode n'a pas besoin d'être optimisée elle est appelée au démarrage uniquement.
	 * @param property Propriété à lire.
	 * @param stringValue Valeur de la propriété sous forme String
	 * @return J Valeur typée de la propriété
	 */
	private static Object readProperty(final EntityProperty property, final String stringValue) {
		Assertion.checkNotNull(property);
		//---------------------------------------------------------------------
		final Class<?> propertyClass = property.getDataType().getJavaClass();
		final Object result = cast(propertyClass, stringValue);
		property.getDataType().checkValue(result);
		return result;
	}

	private static List<DynamicDefinitionKey> toDefinitionKeyList(final List<String> list) {
		final List<DynamicDefinitionKey> definitionKeyList = new ArrayList<>();
		for (final String item : list) {
			definitionKeyList.add(new DynamicDefinitionKey(item));
		}
		return definitionKeyList;
	}

	private static Object cast(final Class<?> propertyClass, final String stringValue) {
		final String sValue = stringValue == null ? null : stringValue.trim();
		if (sValue == null || sValue.length() == 0) {
			return null;
		}
		final Object result;
		if (propertyClass.equals(Integer.class)) {
			result = Integer.valueOf(sValue);
		} else if (propertyClass.equals(Long.class)) {
			result = Long.valueOf(sValue);
		} else if (propertyClass.equals(String.class)) {
			result = String.valueOf(sValue);
		} else if (propertyClass.equals(Boolean.class)) {
			result = Boolean.valueOf(sValue);
		} else {
			throw new IllegalArgumentException("cast de la propriété '" + propertyClass + "' non implémenté");
		}
		return result;
	}
}