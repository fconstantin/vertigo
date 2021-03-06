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
package io.vertigo.vega.impl.servlet.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Implémentation de HttpServletResponseWrapper qui fonctionne avec le CompressionServletResponseStream.
 * @author Amy Roh, Dmitri Valdin (Apache Software Foundation)
 */
class CompressionServletResponseWrapper extends AbstractHttpServletResponseWrapper {
	private final int compressionThreshold;

	/**
	 * Constructeur qui crée un adapteur de ServletResponse wrappant la response spécifiée.
	 * @param response javax.servlet.HttpServletResponse
	 * @param compressionThreshold int
	 */
	CompressionServletResponseWrapper(final HttpServletResponse response, final int compressionThreshold) {
		super(response);
		this.compressionThreshold = compressionThreshold;
	}

	/**
	 * Crée et retourne un ServletOutputStream pour �crire le contenu dans la response associée.
	 * @return javax.servlet.ServletOutputStream
	 */
	@Override
	public ServletOutputStream createOutputStream() {
		return new CompressionResponseStream((HttpServletResponse) getResponse(), compressionThreshold);
	}

	/**
	 * Ne fait rien
	 * @param length int
	 */
	@Override
	public void setContentLength(final int length) {
		// ne fait rien
	}
}
