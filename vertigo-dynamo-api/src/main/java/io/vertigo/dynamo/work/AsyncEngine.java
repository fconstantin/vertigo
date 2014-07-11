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
package io.vertigo.dynamo.work;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.Callable;

/**
 * Gestion des taches asynchrones définies par un Callable 
 * @author pchretien, npiedeloup
 */
final class AsyncEngine<WR, W> implements WorkEngine<WR, W> {
	private final Callable<WR> callable;

	AsyncEngine(final Callable<WR> callable) {
		Assertion.checkNotNull(callable);
		//-----------------------------------------------------------------
		this.callable = callable;
	}

	public WR process(final W dummy) {
		try {
			return callable.call();
		} catch (final Exception e) {
			throw new VRuntimeException(e);
		}
	}
}