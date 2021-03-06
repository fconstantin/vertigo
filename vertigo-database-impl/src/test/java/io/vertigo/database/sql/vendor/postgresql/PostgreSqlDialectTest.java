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
package io.vertigo.database.sql.vendor.postgresql;

import java.util.Optional;

import io.vertigo.database.impl.sql.vendor.postgresql.PostgreSqlDataBase;
import io.vertigo.database.sql.AbstractSqlDialectTest;
import io.vertigo.database.sql.vendor.SqlDialect;

/**
 *
 * @author mlaroche
 */
public final class PostgreSqlDialectTest extends AbstractSqlDialectTest {

	@Override
	public SqlDialect getDialect() {
		return new PostgreSqlDataBase().getSqlDialect();

	}

	@Override
	public String getExpectedInsertQuery() {
		return "insert into MOVIE ( TITLE) values (  #DTO.TITLE#) ";
	}

	@Override
	public String getExpectedSelectForUpdateWildCardQuery() {
		return " select * from MOVIE where ID = #ID# for update ";
	}

	@Override
	public String getExpectedSelectForUpdateFieldsQuery() {
		return " select ID, TITLE from MOVIE where ID = #ID# for update ";
	}

	@Override
	public Optional<String> getExpectedCreatePrimaryKeyQuery() {
		return Optional.empty();
	}

	@Override
	public String getExpectedAppendMaxRowsQuery() {
		return "select * from MOVIE limit 100";
	}
}
