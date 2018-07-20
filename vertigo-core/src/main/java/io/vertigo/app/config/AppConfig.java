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
package io.vertigo.app.config;

import java.io.PrintStream;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * The AppConfig class defines the config.
 * The app is built from this config.
 *
 * AppConfig must be created using the AppConfigBuilder.
 * @author pchretien
 */
public final class AppConfig {
	private final BootConfig bootConfig;
	private final List<ModuleConfig> modules;
	private final List<ComponentInitializerConfig> initializers;
	private final NodeConfig nodeConfig;
	private final Map<String, Function<Class, Lookup>> lookupByModule = new HashMap<>();
	private final Map<String, Function<Class, Lookup>> lookupByComponent = new HashMap<>();

	AppConfig(
			final BootConfig bootConfig,
			final List<ModuleConfig> moduleConfigs,
			final List<ComponentInitializerConfig> componentInitializerConfigs,
			final NodeConfig nodeConfig) {
		Assertion.checkNotNull(bootConfig);
		Assertion.checkNotNull(moduleConfigs);
		Assertion.checkNotNull(componentInitializerConfigs);
		Assertion.checkNotNull(nodeConfig);
		//---
		this.bootConfig = bootConfig;
		modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
		initializers = Collections.unmodifiableList(new ArrayList<>(componentInitializerConfigs));
		this.nodeConfig = nodeConfig;

		moduleConfigs.stream()
				.forEach(moduleConfig -> lookupByModule.put(moduleConfig.getName(), moduleConfig.getPrivateLookup()));

		moduleConfigs.stream()
				.flatMap(moduleConfig -> moduleConfig.getComponentConfigs().stream())
				.forEach(componentConfig -> lookupByComponent.put(componentConfig.getImplClass().getName(), componentConfig.getPrivateLookupProvider()));
	}

	/**
	 * Static method factory for AppConfigBuilder
	 * @return AppConfigBuilder
	 */
	public static AppConfigBuilder builder() {
		return new AppConfigBuilder();
	}

	/**
	 *
	 * @return the config of the boot
	 */
	public BootConfig getBootConfig() {
		return bootConfig;
	}

	/**
	 * @return list of the configs of the modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	/**
	 *
	 * @return List of the config of the initializers
	 */
	public List<ComponentInitializerConfig> getComponentInitializerConfigs() {
		return initializers;
	}

	/**
	 *
	 * @return the config of the node
	 */
	public NodeConfig getNodeConfig() {
		return nodeConfig;
	}

	public Function<Class, Lookup> getLookupProviderForClass(final Class clazz) {
		if (lookupByComponent.containsKey(clazz.getName())) {
			return lookupByComponent.get(clazz.getName());
		}
		final String module = lookupByModule.keySet()
				.stream()
				.filter(moduleName -> clazz.getName().startsWith(moduleName))
				.findFirst()// there should be only one according to the java module specification
				.orElseThrow(() -> new VSystemException("Unable to find the module corresponding to the class : " + clazz.getName()));

		return lookupByModule.get(module);

	}

	//=========================================================================
	//======================Gestion des affichages=============================
	//=========================================================================
	/**
	 * Allows to print a short description of the config.
	 * @param out Out
	 */
	public void print(final PrintStream out) {
		Assertion.checkNotNull(out);
		doPrint(out);
	}

	private static void doPrintLine(final PrintStream out) {
		out.println("+-------------------------+------------------------+----------------------------------------------+");
	}

	/**
	 * Affiche dans la console le logo.
	 * @param out Flux de sortie des informations
	 */
	private void doPrint(final PrintStream out) {
		doPrintLine(out);
		printComponent(out, "modules", "components", "plugins");
		doPrintLine(out);
		printComponents(out, "boot", bootConfig.getComponentConfigs());
		for (final ModuleConfig moduleConfig : modules) {
			doPrintLine(out);
			printModule(out, moduleConfig);
		}
		doPrintLine(out);
	}

	private static void printModule(final PrintStream out, final ModuleConfig moduleConfig) {
		printComponents(out, moduleConfig.getName(), moduleConfig.getComponentConfigs());
	}

	private static void printComponents(final PrintStream out, final String moduleName, final List<ComponentConfig> componentConfigs) {
		boolean first = true;
		for (final ComponentConfig componentConfig : componentConfigs) {
			final String componentClassName = componentConfig.getApiClass()
					.orElse(componentConfig.getImplClass())
					.getSimpleName();
			printComponent(out, first ? moduleName : null, componentClassName, null);
			first = false;
		}
	}

	private static void printComponent(final PrintStream out, final String column1, final String column2, final String column3) {
		out.println("|" + truncate(column1, 24) + " | " + truncate(column2, 22) + " | " + truncate(column3, 44) + " |");
	}

	private static String truncate(final String value, final int size) {
		final String result = (value != null ? value : "") + "                                                                  ";
		return result.substring(0, size);
	}

}
