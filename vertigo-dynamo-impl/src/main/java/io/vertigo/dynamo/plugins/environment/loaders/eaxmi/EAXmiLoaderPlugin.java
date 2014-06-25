package io.vertigo.dynamo.plugins.environment.loaders.eaxmi;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiClass;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiLoader;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

public final class EAXmiLoaderPlugin implements LoaderPlugin {
	private static final Logger LOGGER = Logger.getLogger(EAXmiLoaderPlugin.class);

	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private final ResourceManager resourceManager;

	/**
	 * Constructeur.
	 * @param oomFileName Adresse du fichier powerAMC (OOM).
	 */
	@Inject
	public EAXmiLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		this.resourceManager = resourceManager;
	}

	/** {@inheritDoc} */
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(resourcePath);
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		final URL xmiFileURL = resourceManager.resolve(resourcePath);
		final EAXmiLoader loader = new EAXmiLoader(xmiFileURL);

		for (final EAXmiClass classXmi : loader.getClassList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(classXmi, dynamicModelrepository));
		}

		for (final EAXmiAssociation associationXmi : loader.getAssociationList()) {
			dynamicModelrepository.addDefinition(toDynamicDefinition(associationXmi, dynamicModelrepository));
		}
	}

	private DynamicDefinition toDynamicDefinition(final EAXmiClass classXmi, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity dtDefinitionEntity = DomainGrammar.INSTANCE.getDtDefinitionEntity();
		final DynamicDefinitionBuilder dtDefinitionBuilder = dynamicModelrepository.createDynamicDefinitionBuilder(getDtDefinitionName(classXmi.getCode()), dtDefinitionEntity, classXmi.getPackageName())//
				//Par d�faut les DT lues depuis le XMI sont persistantes.
				.withPropertyValue(KspProperty.PERSISTENT, true);

		for (final EAXmiAttribute attributeXmi : classXmi.getKeyAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeXmi, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition(DomainGrammar.PRIMARY_KEY, dtField);
		}
		for (final EAXmiAttribute attributeXmi : classXmi.getFieldAttributes()) {
			final DynamicDefinition dtField = toDynamicDefinition(attributeXmi, dynamicModelrepository);
			dtDefinitionBuilder.withChildDefinition("field", dtField);
		}
		return dtDefinitionBuilder.build();
	}

	private DynamicDefinition toDynamicDefinition(final EAXmiAttribute attributeXmi, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity dtFieldEntity = DomainGrammar.INSTANCE.getDtFieldEntity();
		final DynamicDefinitionKey domainKey = new DynamicDefinitionKey(attributeXmi.getDomain());

		return dynamicModelrepository.createDynamicDefinitionBuilder(attributeXmi.getCode(), dtFieldEntity, null)//
				.withPropertyValue(KspProperty.LABEL, attributeXmi.getLabel())//
				.withPropertyValue(KspProperty.PERSISTENT, attributeXmi.isPersistent())//
				.withPropertyValue(KspProperty.NOT_NULL, attributeXmi.isNotNull())//
				.withDefinition("domain", domainKey)//
				.build();
	}

	private DynamicDefinition toDynamicDefinition(final EAXmiAssociation associationXmi, final DynamicDefinitionRepository dynamicModelrepository) {
		final Entity associationEntity = DomainGrammar.INSTANCE.getAssociationEntity();
		final Entity associationNNEntity = DomainGrammar.INSTANCE.getAssociationNNEntity();

		final String name = associationXmi.getCode().toUpperCase();

		//On regarde si on est dans le cas d'une association simple ou multiple
		final boolean isAssociationNN = AssociationUtil.isMultiple(associationXmi.getMultiplicityA()) && AssociationUtil.isMultiple(associationXmi.getMultiplicityB());
		final Entity dynamicMetaDefinition;
		if (isAssociationNN) {
			dynamicMetaDefinition = associationNNEntity;
		} else {
			dynamicMetaDefinition = associationEntity;
		}

		//On crée l'association
		final DynamicDefinitionBuilder associationDefinitionBuilder = dynamicModelrepository.createDynamicDefinitionBuilder(name, dynamicMetaDefinition, associationXmi.getPackageName())//
				.withPropertyValue(KspProperty.NAVIGABILITY_A, associationXmi.isNavigableA())//
				.withPropertyValue(KspProperty.NAVIGABILITY_B, associationXmi.isNavigableB())//
				//---
				.withPropertyValue(KspProperty.LABEL_A, associationXmi.getRoleLabelA())//
				//On transforme en CODE ce qui est écrit en toutes lettres.
				.withPropertyValue(KspProperty.ROLE_A, french2Java(associationXmi.getRoleLabelA()))//
				.withPropertyValue(KspProperty.LABEL_B, associationXmi.getRoleLabelB())//
				.withPropertyValue(KspProperty.ROLE_B, french2Java(associationXmi.getRoleLabelB()))
				//---
				.withDefinition("dtDefinitionA", getDtDefinitionKey(associationXmi.getCodeA()))//
				.withDefinition("dtDefinitionB", getDtDefinitionKey(associationXmi.getCodeB()));

		if (isAssociationNN) {
			//Dans le cas d'une association NN il faut établir le nom de la table intermédiaire qui porte les relations
			final String tableName = associationXmi.getCode();
			associationDefinitionBuilder.withPropertyValue(KspProperty.TABLE_NAME, tableName);
			LOGGER.trace("isAssociationNN:Code=" + associationXmi.getCode());
		} else {
			LOGGER.trace("!isAssociationNN:Code=" + associationXmi.getCode());
			//Dans le cas d'une NN ses deux propriétés sont redondantes ; 
			//elles ne font donc pas partie de la définition d'une association de type NN
			associationDefinitionBuilder.withPropertyValue(KspProperty.MULTIPLICITY_A, associationXmi.getMultiplicityA())//
					.withPropertyValue(KspProperty.MULTIPLICITY_B, associationXmi.getMultiplicityB())//
					.withPropertyValue(KspProperty.FK_FIELD_NAME, buildFkFieldName(associationXmi, dynamicModelrepository));

		}
		return associationDefinitionBuilder.build();
	}

	private String buildFkFieldName(final EAXmiAssociation associationXmi, final DynamicDefinitionRepository dynamicModelrepository) {
		// Dans le cas d'une association simple, on recherche le nom de la FK
		// recherche de code de contrainte destin� � renommer la fk selon convention du vbsript PowerAMC
		// Cas de la relation 1-n : o� le nom de la FK est red�fini.
		// Exemple : DOS_UTI_LIQUIDATION (relation entre dossier et utilisateur : FK >> UTILISATEUR_ID_LIQUIDATION)
		final DynamicDefinition dtDefinitionA = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationXmi.getCodeA()));
		final DynamicDefinition dtDefinitionB = dynamicModelrepository.getDefinition(getDtDefinitionKey(associationXmi.getCodeB()));

		final DynamicDefinition foreignDefinition = AssociationUtil.isAPrimaryNode(associationXmi.getMultiplicityA(), associationXmi.getMultiplicityB()) ? dtDefinitionA : dtDefinitionB;
		final List<DynamicDefinition> primaryKeyList = foreignDefinition.getChildDefinitions(DomainGrammar.PRIMARY_KEY);
		if (primaryKeyList.isEmpty()) {
			throw new IllegalArgumentException("Pour l'association '" + associationXmi.getCode() + "' aucune cl� primaire sur la d�finition '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (primaryKeyList.size() > 1) {
			throw new IllegalArgumentException("Pour l'association '" + associationXmi.getCode() + "' cl� multiple non g�r� sur '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}
		if (dtDefinitionA.getDefinitionKey().getName().equals(dtDefinitionB.getDefinitionKey().getName()) && associationXmi.getCodeName() == null) {
			throw new IllegalArgumentException("Pour l'association '" + associationXmi.getCode() + "' le nom de la cl� est obligatoire (AutoJointure) '" + foreignDefinition.getDefinitionKey().getName() + "'");
		}

		//On r�cup�re le nom de LA cl� primaire . 
		final String pkFieldName = primaryKeyList.get(0).getDefinitionKey().getName();

		//Par d�faut le nom de la cl� �trang�re est constitu�e de la cl� primaire r�f�renc�e.
		String fkFieldName = pkFieldName;

		//Si l'association poss�de une nom d�fini par l'utilisateur, alors on l'ajoute � la FK avec un s�parateur.
		if (associationXmi.getCodeName() != null) {
			//On construit le nom de la cl� �trang�re. 
			fkFieldName = fkFieldName + '_' + associationXmi.getCodeName();
		}

		//On raccourci le nom de la cl� �trang�re. 
		if (fkFieldName.length() > 30) { // 30 est le max de dynamo (et de Oracle)
			fkFieldName = fkFieldName.substring(0, 30);
			while (fkFieldName.endsWith("_")) {
				fkFieldName = fkFieldName.substring(0, fkFieldName.length() - 1);
			}

		}
		LOGGER.trace(KspProperty.FK_FIELD_NAME + "=" + fkFieldName);
		//-----------------------------------------------------------------
		Assertion.checkNotNull(fkFieldName, "La cl� primaire n''a pas pu �tre d�finie pour l'association '{0}'", associationXmi.getCode());
		return fkFieldName;
	}

	private String getDtDefinitionName(final String code) {
		return DT_DEFINITION_PREFIX + SEPARATOR + code.toUpperCase();
	}

	private DynamicDefinitionKey getDtDefinitionKey(final String code) {
		return new DynamicDefinitionKey(getDtDefinitionName(code));
	}

	// A sa place dans un util mais ramen� ici pour ind�pendance des plugins
	public String french2Java(final String str) {
		Assertion.checkNotNull(str);
		Assertion.checkArgument(str.length() > 0, "La chaine � modifier ne doit pas �tre vide.");
		// ----------------------------------------------------------------------
		final StringBuilder suffix = new StringBuilder();
		int i = 1;
		char c;
		c = replaceAccent(str.charAt(0));
		suffix.append(Character.toUpperCase(c));

		final int length = str.length();
		while (i < length) {
			c = str.charAt(i);
			//On consid�re blanc, et ' comme des s�parateurs de mots.
			if (c == ' ' || c == '\'') {
				if (i + 1 < length) {
					c = replaceAccent(str.charAt(i + 1));
					if (Character.isLetterOrDigit(c)) {
						suffix.append(Character.toUpperCase(c));
					}
					i += 2;
				} else {
					i++; // �vitons boucle infinie
				}
			} else {
				c = replaceAccent(c);
				if (Character.isLetterOrDigit(c)) {
					suffix.append(c);
				}
				i++;
			}
		}
		return suffix.toString();
	}

	/**
	 * Remplacement de caract�res accentu�s par leurs �quivalents non accentu�s
	 * (par ex: accents dans r�les)
	 * @param c caract�re accentu� � traiter
	 * @return caract�re trait� (sans accent)
	 */
	private static char replaceAccent(final char c) {
		char result;
		switch (c) {
			case '\u00e0':
			case '\u00e2':
			case '\u00e4':
				result = 'a';
				break;
			case '\u00e7':
				result = 'c';
				break;
			case '\u00e8':
			case '\u00e9':
			case '\u00ea':
			case '\u00eb':
				result = 'e';
				break;
			case '\u00ee':
			case '\u00ef':
				result = 'i';
				break;
			case '\u00f4':
			case '\u00f6':
				result = 'o';
				break;
			case '\u00f9':
			case '\u00fb':
			case '\u00fc':
				result = 'u';
				break;
			default:
				result = c;
				break;
		}

		return result;
	}

	public String getType() {
		return "xmi";
	}

}