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
package io.vertigo.core.lang;

/**
 * Interface assurant le cycle de vie des composants (Optionnelle). 
 * Comportement transverse permettant de démarrer et d'arréter des services.
 * Les annotations JSR #250 sont préférées.
 * 
 * @javax.annotation.PostConstruct et @javax.annotation.PreDestroy. 
 * Ex: public class MyComponent {
 * 	@PostConstruct 
 * 	public void init() { }
 * 	@PreDestroy 
 * 	public void destroy() { } 
 * }
 * @author pchretien, prahmoune
 */
public interface Activeable {
	/**
	 * Called when component is starting.
	 * == postConstruct
	 */
	void start();

	/**
	 * Called when component is stopped.
	 * == preDestroy
	 */
	void stop();
}