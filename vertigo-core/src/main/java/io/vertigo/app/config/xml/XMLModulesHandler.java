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
package io.vertigo.app.config.xml;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ComponentConfigBuilder;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.DefinitionProviderConfigBuilder;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.ModuleConfigBuilder;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.PluginConfig;
import io.vertigo.app.config.PluginConfigBuilder;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * @author npiedeloup, pchretien
 */
final class XMLModulesHandler extends DefaultHandler {
	private final AppConfigBuilder appConfigBuilder;
	//Global Params
	private final XMLModulesParams params;
	private final Map<String, Function<Class, Lookup>> privateLookupByModule = new HashMap<>();
	private String currentModuleName;

	private BootConfigBuilder bootConfigBuilder;
	private ModuleConfigBuilder moduleConfigBuilder;
	private ComponentConfigBuilder componentConfigBuilder;
	private PluginConfigBuilder pluginConfigBuilder;
	private DefinitionProviderConfigBuilder definitionProviderConfigBuilder;
	private TagName current;

	XMLModulesHandler(final AppConfigBuilder appConfigBuilder, final XMLModulesParams params, final Map<String, Function<Class, Lookup>> privateLookupByModule) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkNotNull(params);
		//for now we have a singlePrivatelookup...
		Assertion.checkNotNull(privateLookupByModule);
		//-----
		this.appConfigBuilder = appConfigBuilder;
		this.params = params;
		this.privateLookupByModule.putAll(privateLookupByModule);
	}

	enum TagName {
		app, config, boot, module, init,
		//---
		definitions, resource, provider,
		//---
		component, plugin, param, aspect, proxyMethod, proxy,
		//-----
		initializer,
		//----
		feature
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		switch (TagName.valueOf(qName)) {
			case app:
				break;
			case boot:
				bootConfigBuilder.endBoot();
				bootConfigBuilder = null;
				break;
			case module:
				appConfigBuilder.addModule(moduleConfigBuilder.build());
				moduleConfigBuilder = null;
				currentModuleName = null;
				break;
			case component:
				moduleConfigBuilder.addComponent(componentConfigBuilder.build());
				componentConfigBuilder = null;
				break;
			case plugin:
				if (bootConfigBuilder != null) {
					bootConfigBuilder.addPlugin(pluginConfigBuilder.build());
				} else {
					moduleConfigBuilder.addPlugin(pluginConfigBuilder.build());
				}
				pluginConfigBuilder = null;
				break;
			case provider:
				moduleConfigBuilder.addDefinitionProvider(definitionProviderConfigBuilder.build());
				definitionProviderConfigBuilder = null;
				break;
			case aspect:
			case proxyMethod:
			case proxy:
			case param:
			case definitions:
			case resource:
			case config:
			case init:
			case initializer:
			case feature:
				//nothing to do
			default:
		}
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		switch (TagName.valueOf(qName)) {
			case app:
				final String appName = attrs.getValue("name");
				final String nodeId = attrs.getValue("nodeId");
				final String endPoint = attrs.getValue("endPoint");
				appConfigBuilder.withNodeConfig(NodeConfig.builder()
						.withAppName(appName)
						.withNodeId(nodeId)
						.withEndPoint(endPoint)
						.build());
				break;
			case boot:
				current = TagName.boot;
				final String locales = attrs.getValue("locales");
				final String defaultZoneId = attrs.getValue("defaultZoneId");
				if (defaultZoneId == null) {
					bootConfigBuilder = appConfigBuilder
							.beginBoot()
							.withLocales(locales);
				} else {
					bootConfigBuilder = appConfigBuilder
							.beginBoot()
							.withLocalesAndDefaultZoneId(locales, defaultZoneId);
				}

				break;
			case module:
				current = TagName.module;
				final String moduleName = attrs.getValue("name");
				currentModuleName = moduleName;
				moduleConfigBuilder = ModuleConfig.builder(moduleName, privateLookupByModule.get(moduleName));
				break;
			case component:
				current = TagName.component;
				final String componentApi = attrs.getValue("api");
				final Class<? extends Component> componentImplClass = ClassUtil.classForName(attrs.getValue("class"), Component.class);
				componentConfigBuilder = ComponentConfig
						.builder()
						.withImpl(componentImplClass, privateLookupByModule.get(currentModuleName));
				if (componentApi != null) {
					final Class<?> componentApiClass = resolveInterface(componentApi, componentImplClass);
					componentConfigBuilder.withApi((Class<? extends Component>) componentApiClass);
				}
				break;
			case initializer:
				final String initClass = attrs.getValue("class");
				if (initClass != null) {
					final Class componentInitialierClass = ClassUtil.classForName(initClass);
					appConfigBuilder.addInitializer(componentInitialierClass);
				}
				break;
			case plugin:
				current = TagName.plugin;
				final Class<? extends Plugin> pluginImplClass = ClassUtil.classForName(attrs.getValue("class"), Plugin.class);
				pluginConfigBuilder = PluginConfig.builder(pluginImplClass, privateLookupByModule.get(currentModuleName));
				break;
			case proxy:
				final Class<? extends Component> proxyApiClass = ClassUtil.classForName(attrs.getValue("api"), Component.class);
				moduleConfigBuilder.addProxy(proxyApiClass);
				break;
			case provider:
				current = TagName.provider;
				final String definitionProviderClassName = attrs.getValue("class");
				final Class<? extends DefinitionProvider> definitionProviderClass = ClassUtil.classForName(definitionProviderClassName, DefinitionProvider.class);
				definitionProviderConfigBuilder = DefinitionProviderConfig.builder(definitionProviderClass, privateLookupByModule.get(currentModuleName));
				break;
			case resource:
				final String resourceType = attrs.getValue("type");
				final String resourcePath = attrs.getValue("path");
				definitionProviderConfigBuilder.addDefinitionResource(resourceType, evalParamValue(resourcePath));
				break;
			case param:
				final String paramName = attrs.getValue("name");
				final String paramValue = evalParamValue(attrs.getValue("value"));
				final Param param = Param.of(paramName, paramValue);
				if (current == TagName.plugin) {
					pluginConfigBuilder.addParam(param);
				} else if (current == TagName.component) {
					componentConfigBuilder.addParam(param);
				} else if (current == TagName.provider) {
					definitionProviderConfigBuilder.addParam(param);
				}
				break;
			case aspect:
				final String aspectImplClassStr = attrs.getValue("class");
				final Class<? extends Aspect> aspectImplClass = ClassUtil.classForName(aspectImplClassStr, Aspect.class);
				moduleConfigBuilder.addAspect(aspectImplClass);
				break;
			case proxyMethod:
				final String proxyMethodImplClassStr = attrs.getValue("class");
				final Class<? extends ProxyMethod> proxyMethodClass = ClassUtil.classForName(proxyMethodImplClassStr, ProxyMethod.class);
				moduleConfigBuilder.addProxyMethod(proxyMethodClass);
				break;
			case feature:
				final String featureClassStr = attrs.getValue("class");
				final ModuleConfig moduleConfigByFeatures = ClassUtil.newInstance(featureClassStr, Features.class).build();
				appConfigBuilder.addModule(moduleConfigByFeatures);
				break;
			case definitions:
			case config:
			case init:
				//non géré
			default:
		}
	}

	//On recherche l'interface ayant le nom 'simpleName' dans l'arbre de la classe 'clazz'
	//Cette interface doit exister et être unique.
	private static Class<?> resolveInterface(final String simpleName, final Class<? extends Component> clazz) {
		final List<Class> interfaces = ClassUtil.getAllInterfaces(clazz).stream()
				.filter(interfaceClazz -> simpleName.equals(interfaceClazz.getSimpleName()))
				.collect(Collectors.toList());

		Assertion.checkState(interfaces.size() <= 1, "Many interfaces of class '{0}' have the same simpleName {1}", clazz, simpleName);
		Assertion.checkState(!interfaces.isEmpty(), "No interface of class '{0}' have the simpleName '{1}", clazz, simpleName);
		//there is exactly one interface.
		return interfaces.get(0);
	}

	private String evalParamValue(final String paramValue) {
		if (paramValue.startsWith("${boot.") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return params.getParam(property);
		}
		return paramValue;
	}
}
