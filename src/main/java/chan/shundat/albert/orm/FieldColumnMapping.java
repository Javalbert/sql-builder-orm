/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package chan.shundat.albert.orm;

import java.sql.SQLException;

import chan.shundat.albert.utils.ClassUtils;
import chan.shundat.albert.utils.jdbc.ResultSetHelper;
import chan.shundat.albert.utils.reflection.MemberAccess;
import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public abstract class FieldColumnMapping implements MemberAccess {
	public static final int JDBC_TYPE_BIG_DECIMAL = 1;
	public static final int JDBC_TYPE_BOOLEAN = 2;
	public static final int JDBC_TYPE_DATE = 3;
	public static final int JDBC_TYPE_DOUBLE = 4;
	public static final int JDBC_TYPE_FLOAT = 5;
	public static final int JDBC_TYPE_INTEGER = 6;
	public static final int JDBC_TYPE_LONG = 7;
	public static final int JDBC_TYPE_PRIMITIVE_BOOLEAN = 8;
	public static final int JDBC_TYPE_PRIMITIVE_DOUBLE = 9;
	public static final int JDBC_TYPE_PRIMITIVE_FLOAT = 10;
	public static final int JDBC_TYPE_PRIMITIVE_INT = 11;
	public static final int JDBC_TYPE_PRIMITIVE_LONG = 12;
	public static final int JDBC_TYPE_STRING = 13;
	public static final int JDBC_TYPE_TIMESTAMP = 14;
	
	public static Object getFromResultSet(int jdbcType, 
			ResultSetHelper rs, 
			int column) 
			throws SQLException {
		switch (jdbcType) {
			case JDBC_TYPE_BIG_DECIMAL: return rs.getBigDecimal(column);
			case JDBC_TYPE_BOOLEAN: return rs.getBoolean2(column);
			case JDBC_TYPE_DATE: return rs.getDate2(column);
			case JDBC_TYPE_DOUBLE: return rs.getDouble2(column);
			case JDBC_TYPE_FLOAT: return rs.getFloat2(column);
			case JDBC_TYPE_INTEGER: return rs.getInt2(column);
			case JDBC_TYPE_LONG: return rs.getLong2(column);
			case JDBC_TYPE_PRIMITIVE_BOOLEAN: return rs.getBoolean(column);
			case JDBC_TYPE_PRIMITIVE_DOUBLE: return rs.getDouble(column);
			case JDBC_TYPE_PRIMITIVE_FLOAT: return rs.getFloat(column);
			case JDBC_TYPE_PRIMITIVE_INT: return rs.getInt(column);
			case JDBC_TYPE_PRIMITIVE_LONG: return rs.getLong(column);
			case JDBC_TYPE_STRING: return rs.getString(column);
			case JDBC_TYPE_TIMESTAMP: return rs.getTimestamp2(column);
		}
		throw new IllegalArgumentException("Unsupported JDBC type: " + jdbcType);
	}
	
	public static Object getFromResultSet(int jdbcType, 
			ResultSetHelper rs, 
			String columnLabel) 
			throws SQLException {
		switch (jdbcType) {
			case JDBC_TYPE_BIG_DECIMAL: return rs.getBigDecimal(columnLabel);
			case JDBC_TYPE_BOOLEAN: return rs.getBoolean2(columnLabel);
			case JDBC_TYPE_DATE: return rs.getDate2(columnLabel);
			case JDBC_TYPE_DOUBLE: return rs.getDouble2(columnLabel);
			case JDBC_TYPE_FLOAT: return rs.getFloat2(columnLabel);
			case JDBC_TYPE_INTEGER: return rs.getInt2(columnLabel);
			case JDBC_TYPE_LONG: return rs.getLong2(columnLabel);
			case JDBC_TYPE_PRIMITIVE_BOOLEAN: return rs.getBoolean(columnLabel);
			case JDBC_TYPE_PRIMITIVE_DOUBLE: return rs.getDouble(columnLabel);
			case JDBC_TYPE_PRIMITIVE_FLOAT: return rs.getFloat(columnLabel);
			case JDBC_TYPE_PRIMITIVE_INT: return rs.getInt(columnLabel);
			case JDBC_TYPE_PRIMITIVE_LONG: return rs.getLong(columnLabel);
			case JDBC_TYPE_STRING: return rs.getString(columnLabel);
			case JDBC_TYPE_TIMESTAMP: return rs.getTimestamp2(columnLabel);
		}
		throw new IllegalArgumentException("Unsupported JDBC type: " + jdbcType);
	}
	
	public static int getJdbcType(Class clazz) {
		switch (clazz.getCanonicalName()) {
			case ClassUtils.NAME_BOOLEAN: return JDBC_TYPE_PRIMITIVE_BOOLEAN;
			case ClassUtils.NAME_DOUBLE: return JDBC_TYPE_PRIMITIVE_DOUBLE;
			case ClassUtils.NAME_FLOAT: return JDBC_TYPE_PRIMITIVE_FLOAT;
			case ClassUtils.NAME_INT: return JDBC_TYPE_PRIMITIVE_INT;
			case ClassUtils.NAME_JAVA_LANG_BOOLEAN: return JDBC_TYPE_BOOLEAN;
			case ClassUtils.NAME_JAVA_LANG_DOUBLE: return JDBC_TYPE_DOUBLE;
			case ClassUtils.NAME_JAVA_LANG_FLOAT: return JDBC_TYPE_FLOAT;
			case ClassUtils.NAME_JAVA_LANG_INTEGER: return JDBC_TYPE_INTEGER;
			case ClassUtils.NAME_JAVA_LANG_LONG: return JDBC_TYPE_LONG;
			case ClassUtils.NAME_JAVA_LANG_STRING: return JDBC_TYPE_STRING;
			case ClassUtils.NAME_JAVA_MATH_BIG_DECIMAL: return JDBC_TYPE_BIG_DECIMAL;
			case ClassUtils.NAME_JAVA_UTIL_DATE: return JDBC_TYPE_DATE;
			case ClassUtils.NAME_LONG: return JDBC_TYPE_PRIMITIVE_LONG;
		}
		throw new IllegalArgumentException("No JDBC type for class " + clazz);
	}
	
	protected final String alias;
	protected final boolean autoIncrementId;
	protected final String column;
	protected final int jdbcType;
	protected final String mapKeyName;
	protected final boolean primaryKey;
	protected final boolean version;

	public String getAlias() { return alias; }
	public boolean isAutoIncrementId() { return autoIncrementId; }
	public String getColumn() { return column; }
	public int getJdbcType() { return jdbcType; }
	public String getMapKeyName() { return mapKeyName; }
	public boolean isPrimaryKey() { return primaryKey; }
	public boolean isVersion() { return version; }
	
	protected FieldColumnMapping(String column, 
			String alias, 
			int jdbcType, 
			String mapKeyName, 
			boolean primaryKey, 
			GeneratedValue generatedValue, 
			boolean version) {
		this.alias = alias;
		this.autoIncrementId = generatedValue != null;
		this.column = column;
		this.jdbcType = jdbcType;
		this.mapKeyName = mapKeyName;
		this.primaryKey = primaryKey;
		this.version = version;
	}

	public Object getFromResultSet(ResultSetHelper rs, int column) throws SQLException {
		return getFromResultSet(jdbcType, rs, column);
	}
	
	public Object getFromResultSet(ResultSetHelper rs) throws SQLException {
		return getFromResultSet(jdbcType, rs, Strings.isNullOrEmpty(column) ? alias : column);
	}
	
	public Object getFromResultSet(ResultSetHelper rs, String columnLabel) throws SQLException {
		return getFromResultSet(jdbcType, rs, columnLabel);
	}

	public void setFromResultSet(Object instance, ResultSetHelper rs, int column) throws SQLException {
		Object value = getFromResultSet(rs, column);
		set(instance, value);
	}
	
	public void setFromResultSet(Object instance, ResultSetHelper rs) throws SQLException {
		setFromResultSet(instance, rs, Strings.isNullOrEmpty(column) ? alias : column);
	}

	public void setFromResultSet(Object instance, ResultSetHelper rs, String columnLabel) throws SQLException {
		Object value = getFromResultSet(rs, columnLabel);
		set(instance, value);
	}
}