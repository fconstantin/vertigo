package io.vertigo.dynamo.impl.database.vendor.core;

import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.domain.metamodel.KDataType;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Implémentation par défaut du mapping à la BDD.
 * Cette implmentation peut être redéfinie partiellement ou totalement.
 *
 * @author pchretien
 * @version $Id: SQLMappingImpl.java,v 1.2 2014/01/20 17:46:11 pchretien Exp $
 */
public final class SQLMappingImpl implements SQLMapping {
	private static final String TYPE_INCONNU = "Type inconnu : ";

	/** {@inheritDoc} */
	public KDataType getDataType(final int typeSQL) {
		final KDataType dataType;
		switch (typeSQL) {
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.INTEGER:
			case Types.NUMERIC:
				dataType = KDataType.Integer;
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				dataType = KDataType.String;
				break;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				dataType = KDataType.Date;
				break;
			case Types.BIGINT:
				dataType = KDataType.Long;
				break;
			case Types.BOOLEAN:
			case Types.BIT:
				dataType = KDataType.Boolean;
				break;
			case Types.DECIMAL:
				dataType = KDataType.BigDecimal;
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				dataType = KDataType.Double;
				break;
			case Types.BLOB:
				dataType = KDataType.DataStream;
				break;
			default:
				throw new IllegalArgumentException("Type SQL non géré (" + typeSQL + ')');
		}
		return dataType;
	}

	/** {@inheritDoc} */
	public int getTypeSQL(final KDataType dataType) {
		switch (dataType) {
			case Integer:
				return Types.INTEGER;
			case Boolean:
				return Types.BIT;
			case Long:
				return Types.BIGINT;
			case Double:
				return Types.DOUBLE;
			case BigDecimal:
				return Types.DECIMAL;
			case String:
				return Types.VARCHAR;
			case Date:
				return Types.TIMESTAMP;
			case DataStream:
				return Types.BLOB;
			default:
				throw new IllegalArgumentException(TYPE_INCONNU + dataType);
		}
	}

	/** {@inheritDoc} */
	public void setValueOnStatement(final java.sql.PreparedStatement statement, final int index, final KDataType dataType, final Object value) throws SQLException {
		if (value == null) {
			final int typeSQL = getTypeSQL(dataType);
			statement.setNull(index, typeSQL);
		} else {
			switch (dataType) {
				case Integer:
					statement.setInt(index, ((Integer) value).intValue());
					break;
				case Long:
					statement.setLong(index, ((Long) value).longValue());
					break;
				case Boolean:
					final int intValue = Boolean.TRUE.equals(value) ? 1 : 0;
					statement.setInt(index, intValue);
					break;
				case Double:
					statement.setDouble(index, ((Double) value).doubleValue());
					break;
				case BigDecimal:
					statement.setBigDecimal(index, (BigDecimal) value);
					break;
				case String:
					statement.setString(index, (String) value);
					break;
				case Date:
					if (value instanceof java.sql.Timestamp) {
						statement.setTimestamp(index, (java.sql.Timestamp) value);
					} else {
						final java.sql.Timestamp ts = new java.sql.Timestamp(((java.util.Date) value).getTime());
						statement.setTimestamp(index, ts);
					}
					break;
				case DataStream:
					try {
						final DataStream dataStream = (DataStream) value;
						statement.setBinaryStream(index, dataStream.createInputStream(), (int) dataStream.getLength()); //attention le setBinaryStream avec une longueur de fichier en long N'EST PAS implémentée dans de nombreux drivers !!
					} catch (final IOException e) {
						final SQLException sqlException = new SQLException("Erreur d'ecriture du flux");
						sqlException.initCause(e);
						throw sqlException;
					}
					break;
				default:
					throw new IllegalArgumentException(TYPE_INCONNU + dataType);
			}
		}
	}

	/** {@inheritDoc} */
	public Object getValueForCallableStatement(final CallableStatement callableStatement, final int index, final KDataType dataType) throws SQLException {
		Object o;
		switch (dataType) {
			case Integer:
				o = callableStatement.getInt(index);
				break;
			case Long:
				o = callableStatement.getLong(index);
				break;
			case Boolean:
				o = callableStatement.getBoolean(index);
				break;
			case Double:
				o = callableStatement.getDouble(index);
				break;
			case BigDecimal:
				o = callableStatement.getBigDecimal(index);
				break;
			case String:
				o = callableStatement.getString(index);
				break;
			case Date:

				//Pour avoir une date avec les heures (Sens Java !)
				//il faut récupérer le timeStamp
				//Puis le transformer en java.util.Date (Date+heure)
				final Timestamp timestamp = callableStatement.getTimestamp(index); //peut etre null !!
				if (timestamp != null) {
					o = new java.util.Date(timestamp.getTime());
				} else {
					o = null;
				}
				break;
			default:
				throw new IllegalArgumentException(TYPE_INCONNU + dataType);
		}
		if (callableStatement.wasNull()) {
			o = null;
		}
		return o;
	}

	/** {@inheritDoc} */
	public Object getValueForResultSet(final ResultSet rs, final int col, final KDataType dataType) throws SQLException {
		final Object value;
		switch (dataType) {
			case String:
				if (rs.getMetaData().getColumnType(col) == Types.CLOB) {
					final Clob clob = rs.getClob(col);
					//Si la valeur est null rs renvoie bien null
					if (clob != null) {
						final Long len = clob.length();
						value = clob.getSubString(1L, len.intValue());
					} else {
						value = null;
					}
				} else {
					//Si la valeur est null rs renvoie bien null
					value = rs.getString(col);
				}
				break;
			case Integer:
				final int vi = rs.getInt(col);
				value = rs.wasNull() ? null : vi;
				break;
			case Long:
				final long vl = rs.getLong(col);
				value = rs.wasNull() ? null : vl;
				break;
			case Boolean:
				final int vb = rs.getInt(col);
				value = rs.wasNull() ? null : vb != 0 ? Boolean.TRUE : Boolean.FALSE;
				break;
			case Double:
				final double vd = rs.getDouble(col);
				value = rs.wasNull() ? null : vd;
				break;
			case BigDecimal:

				//Si la valeur est null rs renvoie bien null
				value = rs.getBigDecimal(col);
				break;
			case Date:

				//Si la valeur est null rs renvoie bien null
				final Timestamp timestamp = rs.getTimestamp(col);

				//Pour avoir une date avec les heures (Sens Java !)
				//il faut récupérer le timeStamp
				//Puis le transformer en java.util.Date (Date+heure)
				value = timestamp == null ? null : new java.util.Date(timestamp.getTime());
				break;
			case DataStream:
				value = DataStreamMappingUtil.getDataStream(rs, col);
				break;
			default:
				throw new IllegalArgumentException(TYPE_INCONNU + dataType);
		}
		return value;
	}

}