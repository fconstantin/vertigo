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
package io.vertigo.studio.plugins.mda.domain.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.masterdata.MasterDataManager;
import io.vertigo.studio.masterdata.MasterDataValues;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.domain.sql.model.SqlDtDefinitionModel;
import io.vertigo.studio.plugins.mda.domain.sql.model.SqlMasterDataDefinitionModel;
import io.vertigo.studio.plugins.mda.domain.sql.model.SqlMethodModel;
import io.vertigo.studio.plugins.mda.util.DomainUtil;
import io.vertigo.util.MapBuilder;
import io.vertigo.util.StringUtil;

/**
 * Generate crebas.sql.
 *
 * @author pchretien, gpierre-nicolas
 */
public final class SqlGeneratorPlugin implements GeneratorPlugin {

	private static final String DEFAULT_DATA_SPACE = "main";

	private final String targetSubDir;
	private final boolean generateDrop;
	private final String baseCible;
	private final Optional<String> tableSpaceDataOpt;
	private final Optional<String> tableSpaceIndexOpt;
	private final boolean generateMasterData;

	private final Optional<MasterDataManager> masterDataManagerOpt;

	/**
	 * Constructeur.
	 *
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param generateDrop Si on génère les Drop table dans le fichier SQL
	 * @param baseCible Type de base de données ciblé.
	 * @param tableSpaceData Nom du tableSpace des données
	 * @param tableSpaceIndex Nom du tableSpace des indexes
	 */
	@Inject
	public SqlGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("generateDrop") final boolean generateDrop,
			@Named("baseCible") final String baseCible,
			@Named("generateMasterData") final Optional<Boolean> generateMasterDataOpt,
			@Named("tableSpaceData") final Optional<String> tableSpaceData,
			@Named("tableSpaceIndex") final Optional<String> tableSpaceIndex,
			final Optional<MasterDataManager> masterDataManagerOpt) {
		//-----
		this.targetSubDir = targetSubDir;
		this.generateDrop = generateDrop;
		this.baseCible = baseCible;
		tableSpaceDataOpt = tableSpaceData;
		tableSpaceIndexOpt = tableSpaceIndex;
		generateMasterData = generateMasterDataOpt.orElse(false);
		this.masterDataManagerOpt = masterDataManagerOpt;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generateSql(fileGeneratorConfig, mdaResultBuilder);

		if (generateMasterData) {
			generateMasterDataInserts(fileGeneratorConfig, mdaResultBuilder);
		}
	}

	private void generateMasterDataInserts(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final MasterDataValues masterDataValues = masterDataManagerOpt.isPresent() ? masterDataManagerOpt.get().getValues() : new MasterDataValues();

		final List<SqlMasterDataDefinitionModel> sqlMasterDataDefinitionModels = Home.getApp().getDefinitionSpace().getAll(DtDefinition.class)
				.stream()
				.filter(dtDefinition -> dtDefinition.getStereotype() == DtStereotype.StaticMasterData)
				.map(dtDefinition -> new SqlMasterDataDefinitionModel(dtDefinition, masterDataValues.getOrDefault(dtDefinition.getClassCanonicalName(), Collections.emptyMap())))
				.collect(Collectors.toList());

		final Map<String, Object> model = new MapBuilder<String, Object>()
				.put("masterdatas", sqlMasterDataDefinitionModels)
				.build();

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName("init_masterdata.sql")
				.withGenSubDir(targetSubDir)
				.withPackageName("")
				.withTemplateName("domain/sql/template/init_masterdata.ftl")
				.build()
				.generateFile(mdaResultBuilder);

	}

	private void generateSql(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {

		final Map<String, List<SqlDtDefinitionModel>> mapListDtDef = new HashMap<>();
		for (final DtDefinition dtDefinition : DomainUtil.sortDefinitionCollection(DomainUtil.getDtDefinitions())) {
			if (dtDefinition.isPersistent()) {
				final SqlDtDefinitionModel templateDef = new SqlDtDefinitionModel(dtDefinition);
				final String dataSpace = dtDefinition.getDataSpace();
				final List<SqlDtDefinitionModel> listDtDef = obtainListDtDefinitionPerDataSpace(mapListDtDef, dataSpace);
				listDtDef.add(templateDef);
			}
		}
		//
		final Collection<AssociationSimpleDefinition> collectionSimpleAll = DomainUtil.getSimpleAssociations();
		final Collection<AssociationNNDefinition> collectionNNAll = DomainUtil.getNNAssociations();
		//
		for (final Entry<String, List<SqlDtDefinitionModel>> entry : mapListDtDef.entrySet()) {
			final String dataSpace = entry.getKey();
			final Collection<AssociationSimpleDefinition> associationSimpleDefinitions = filterAssociationSimple(collectionSimpleAll, dataSpace);
			final Collection<AssociationNNDefinition> associationNNDefinitions = filterAssociationNN(collectionNNAll, dataSpace);

			generateSqlByDataSpace(
					fileGeneratorConfig,
					mdaResultBuilder,
					associationSimpleDefinitions,
					associationNNDefinitions,
					dataSpace,
					entry.getValue());
		}
	}

	private void generateSqlByDataSpace(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final Collection<AssociationSimpleDefinition> asssociationSimpleDefinitions,
			final Collection<AssociationNNDefinition> associationNNDefinitions,
			final String dataSpace,
			final List<SqlDtDefinitionModel> dtDefinitions) {
		final StringBuilder filename = new StringBuilder()
				.append("crebas");
		if (!StringUtil.isEmpty(dataSpace) && !DEFAULT_DATA_SPACE.equals(dataSpace)) {
			filename.append('_').append(dataSpace);
		}
		filename.append(".sql");
		generateFile(
				fileGeneratorConfig,
				mdaResultBuilder,
				dtDefinitions,
				asssociationSimpleDefinitions,
				associationNNDefinitions,
				filename.toString());
	}

	private static List<SqlDtDefinitionModel> obtainListDtDefinitionPerDataSpace(final Map<String, List<SqlDtDefinitionModel>> mapListDtDef, final String dataSpace) {
		return mapListDtDef.computeIfAbsent(dataSpace, k -> new ArrayList<>());
	}

	private static Collection<AssociationSimpleDefinition> filterAssociationSimple(
			final Collection<AssociationSimpleDefinition> collectionSimpleAll,
			final String dataSpace) {
		return collectionSimpleAll.stream()
				.filter(a -> dataSpace.equals(a.getAssociationNodeA().getDtDefinition().getDataSpace()))
				.collect(Collectors.toList());
	}

	private static Collection<AssociationNNDefinition> filterAssociationNN(
			final Collection<AssociationNNDefinition> collectionNNAll,
			final String dataSpace) {
		return collectionNNAll.stream()
				.filter(a -> dataSpace.equals(a.getAssociationNodeA().getDtDefinition().getDataSpace()))
				.collect(Collectors.toList());
	}

	private void generateFile(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final List<SqlDtDefinitionModel> dtDefinitionModels,
			final Collection<AssociationSimpleDefinition> associationSimpleDefinitions,
			final Collection<AssociationNNDefinition> collectionNN,
			final String fileName) {
		final MapBuilder<String, Object> modelBuilder = new MapBuilder<String, Object>()
				.put("sql", new SqlMethodModel())
				.put("dtDefinitions", dtDefinitionModels)
				.put("simpleAssociations", associationSimpleDefinitions)
				.put("nnAssociations", collectionNN)
				.put("drop", generateDrop)
				// Ne sert actuellement à rien, le sql généré étant le même. Prévu pour le futur
				.put("basecible", baseCible)
				// Oracle limite le nom des entités (index) à 30 charactères. Il faut alors tronquer les noms composés.
				.put("truncateNames", "Oracle".equals(baseCible));

		tableSpaceDataOpt.ifPresent(
				tableSpaceData -> modelBuilder.put("tableSpaceData", tableSpaceData));
		tableSpaceIndexOpt.ifPresent(
				tableSpaceIndex -> modelBuilder.put("tableSpaceIndex", tableSpaceIndex));

		final Map<String, Object> model = modelBuilder.build();
		final String templatName = isSqlServer() ? "domain/sql/template/sqlserver.ftl" : "domain/sql/template/sql.ftl";

		FileGenerator.builder(fileGeneratorConfig)
				.withModel(model)
				.withFileName(fileName)
				.withGenSubDir(targetSubDir)
				.withPackageName("")
				.withTemplateName(templatName)
				.build()
				.generateFile(mdaResultBuilder);
	}

	private boolean isSqlServer() {
		return "sqlserver".equalsIgnoreCase(baseCible) || "sql server".equalsIgnoreCase(baseCible);
	}

}
