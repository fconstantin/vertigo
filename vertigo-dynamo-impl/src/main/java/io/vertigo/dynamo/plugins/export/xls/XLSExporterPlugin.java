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
package io.vertigo.dynamo.plugins.export.xls;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.impl.export.ExporterPlugin;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.persistence.PersistenceManager;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

/**
 * Plugin d'export Excel.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExcelExporterPlugin.java,v 1.2 2014/01/28 18:49:44 pchretien Exp $
 */
public final class XLSExporterPlugin implements ExporterPlugin {
	private final ExportHelper exportHelper;

	@Inject
	public XLSExporterPlugin(final PersistenceManager persistenceManager) {
		exportHelper = new ExportHelper(persistenceManager);
	}

	/** {@inheritDoc}*/
	public void exportData(final Export export, final OutputStream out) throws IOException {
		new XLSExporter(exportHelper).exportData(export, out);
	}

	/** {@inheritDoc}*/
	public boolean accept(final ExportFormat exportFormat) {
		return ExportFormat.XLS.equals(exportFormat);
	}

}