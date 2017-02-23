/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.eventbus;

/**
 * An event is the message that is communicated from the publisher to the suscribers. 
 * The type of events is used to dispatch an event to the right subscribers.
 * 
 * The event must be as simple as possible. 
 * An event explains the WHAT  and not the HOW. 
 * 
 * example :
 * - an event should contain a simple id and a type of transformation.
 *  
 * @author pchretien
 */
public interface Event {
	//
}