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
package io.vertigo.dynamo.plugins.search.elasticsearch_2_4.transport;

import java.net.InetSocketAddress;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.plugins.search.elasticsearch_2_4.AbstractESSearchServicesPlugin;
import io.vertigo.lang.Assertion;

/**
 * Gestion de la connexion au serveur elasticSearch en mode HTTP. Utilisation du client Transport simple, sans
 * intégration au cluster (permet de ne pas avoir de liaison bi-directionelle entre le tomcat et l'ES externe).
 *
 * @author skerdudou
 */
public final class ESTransportSearchServicesPlugin extends AbstractESSearchServicesPlugin {

	/** url du serveur elasticSearch. */
	private final String[] serversNames;
	/** cluster à rejoindre. */
	private final String clusterName;
	/** Nom du noeud. */
	private final String nodeName;
	/** le noeud interne. */
	private TransportClient client;

	/**
	 * Constructor.
	 *
	 * @param serversNamesStr URL du serveur ElasticSearch avec le port de communication de cluster (9300 en général)
	 * @param envIndex Nom de l'index de l'environment
	 * @param envIndexIsPrefix Si Nom de l'index de l'environment est un prefix
	 * @param rowsPerQuery Liste des indexes
	 * @param codecManager Manager des codecs
	 * @param clusterName : nom du cluster à rejoindre
	 * @param nodeNameOpt : nom du node
	 * @param configFile fichier de configuration des index
	 * @param resourceManager Manager d'accès aux ressources
	 */
	@Inject
	public ESTransportSearchServicesPlugin(
			@Named("servers.names") final String serversNamesStr,
			@Named("envIndex") final String envIndex,
			@Named("envIndexIsPrefix") final Optional<Boolean> envIndexIsPrefix,
			@Named("rowsPerQuery") final int rowsPerQuery,
			@Named("cluster.name") final String clusterName,
			@Named("config.file") final Optional<String> configFile,
			@Named("node.name") final Optional<String> nodeNameOpt,
			final CodecManager codecManager,
			final ResourceManager resourceManager) {
		super(envIndex, envIndexIsPrefix.orElse(false), rowsPerQuery, configFile, codecManager, resourceManager);
		Assertion.checkArgNotEmpty(serversNamesStr,
				"Il faut définir les urls des serveurs ElasticSearch (ex : host1:3889,host2:3889). Séparateur : ','");
		Assertion.checkArgument(!serversNamesStr.contains(";"),
				"Il faut définir les urls des serveurs ElasticSearch (ex : host1:3889,host2:3889). Séparateur : ','");
		Assertion.checkArgNotEmpty(clusterName, "Cluster's name must be defined");
		Assertion.checkArgument(!"elasticsearch".equals(clusterName),
				"You have to define a cluster name different from the default one");
		// ---------------------------------------------------------------------
		serversNames = serversNamesStr.split(",");
		this.clusterName = clusterName;
		nodeName = nodeNameOpt.orElseGet(() -> "es-transport-client-" + System.currentTimeMillis());
	}

	/** {@inheritDoc} */
	@Override
	protected Client createClient() {
		client = TransportClient.builder().settings(buildNodeSettings()).build();
		for (final String serverName : serversNames) {
			final String[] serverNameSplit = serverName.split(":");
			Assertion.checkArgument(serverNameSplit.length == 2,
					"La déclaration du serveur doit être au format host:port ({0}", serverName);
			final int port = Integer.parseInt(serverNameSplit[1]);
			client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(serverNameSplit[0], port)));
		}
		return client;
	}

	/** {@inheritDoc} */
	@Override
	protected void closeClient() {
		client.close();
	}

	private Settings buildNodeSettings() {
		// Build settings
		return Settings.settingsBuilder().put("node.name", nodeName)
				// .put("node.data", false)
				// .put("node.master", false)
				// .put("discovery.zen.fd.ping_timeout", "30s")
				// .put("discovery.zen.minimum_master_nodes", 2)
				// .put("discovery.zen.ping.multicast.enabled", false)
				// .putArray("discovery.zen.ping.unicast.hosts", serversNames)
				.put("cluster.name", clusterName)
				// .put("index.store.type", "memory")
				// .put("index.store.fs.memory.enabled", "true")
				// .put("gateway.type", "none")
				.build();
	}

}
