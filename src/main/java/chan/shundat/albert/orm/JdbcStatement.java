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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.NodeVisitor;
import chan.shundat.albert.sqlbuilder.Param;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.SqlStatement;
import chan.shundat.albert.utils.ClassUtils;
import chan.shundat.albert.utils.jdbc.JdbcUtils;
import chan.shundat.albert.utils.jdbc.PreparedStatementImpl;
import chan.shundat.albert.utils.jdbc.ResultSetHelper;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JdbcStatement {
	private final static Logger logger = LoggerFactory.getLogger(JdbcStatement.class);
	
	private static final int PARAM_TYPE_BIG_DECIMAL = 1;
	private static final int PARAM_TYPE_BOOLEAN = 2;
	private static final int PARAM_TYPE_DATE = 3;
	private static final int PARAM_TYPE_DOUBLE = 4;
	private static final int PARAM_TYPE_FLOAT = 5;
	private static final int PARAM_TYPE_INTEGER = 6;
	private static final int PARAM_TYPE_LIST_BIG_DECIMAL = 7;
	private static final int PARAM_TYPE_LIST_DATE = 8;
	private static final int PARAM_TYPE_LIST_DOUBLE = 9;
	private static final int PARAM_TYPE_LIST_FLOAT = 10;
	private static final int PARAM_TYPE_LIST_INTEGER = 11;
	private static final int PARAM_TYPE_LIST_LONG = 12;
	private static final int PARAM_TYPE_LIST_STRING = 13;
	private static final int PARAM_TYPE_LIST_TIMESTAMP = 14;
	private static final int PARAM_TYPE_LONG = 15;
	private static final int PARAM_TYPE_PRIMITIVE_BOOLEAN = 16;
	private static final int PARAM_TYPE_PRIMITIVE_DOUBLE = 17;
	private static final int PARAM_TYPE_PRIMITIVE_FLOAT = 18;
	private static final int PARAM_TYPE_PRIMITIVE_INT = 19;
	private static final int PARAM_TYPE_PRIMITIVE_LONG = 20;
	private static final int PARAM_TYPE_STRING = 21;
	private static final int PARAM_TYPE_TIMESTAMP = 22;
	
	private static final Pattern PARAM_PATTERN = Pattern.compile(":\\w+");
	
	private static <T, C extends Collection<T>> C toJdbcDataTypeCollection(Class<T> clazz, ResultSetHelper rs, C collection) throws SQLException {
		switch (clazz.getCanonicalName()) {
			case ClassUtils.NAME_BOOLEAN:
			case ClassUtils.NAME_JAVA_LANG_BOOLEAN:
				while (rs.next()) {
					collection.add((T)rs.getBoolean2(1));
				}
				break;
			case ClassUtils.NAME_DOUBLE:
			case ClassUtils.NAME_JAVA_LANG_DOUBLE:
				while (rs.next()) {
					collection.add((T)rs.getDouble2(1));
				}
				break;
			case ClassUtils.NAME_FLOAT:
			case ClassUtils.NAME_JAVA_LANG_FLOAT:
				while (rs.next()) {
					collection.add((T)rs.getFloat2(1));
				}
				break;
			case ClassUtils.NAME_JAVA_LANG_INTEGER:
				while (rs.next()) {
					collection.add((T)rs.getInt2(1));
				}
				break;
			case ClassUtils.NAME_LONG:
			case ClassUtils.NAME_JAVA_LANG_LONG:
				while (rs.next()) {
					collection.add((T)rs.getLong2(1));
				}
				break;
			case ClassUtils.NAME_JAVA_LANG_STRING:
				while (rs.next()) {
					collection.add((T)rs.getString(1));
				}
				break;
			case ClassUtils.NAME_JAVA_MATH_BIG_DECIMAL:
				while (rs.next()) {
					collection.add((T)rs.getBigDecimal(1));
				}
				break;
			case ClassUtils.NAME_JAVA_SQL_TIMESTAMP:
				while (rs.next()) {
					collection.add((T)rs.getTimestamp(1));
				}
				break;
			case ClassUtils.NAME_JAVA_UTIL_DATE:
				while (rs.next()) {
					collection.add((T)rs.getDate2(1));
				}
				break;
		}
		return collection;
	}
	
	private List<int[]> batchRowCountsList;
	private int batchSize = 0;
	/**
	 * Caching the prepared statement is useful for: 
	 * <ul>
	 * 	<li>loops -  http://stackoverflow.com/q/11971635</li>
	 * 	<li>batch updates (NOTE: JDBC does not support batch reads, see http://stackoverflow.com/q/21592224)</li>
	 * </ul>
	 */
	private boolean cachePreparedStatement;
	private String jdbcSql;
	private final JdbcMapper jdbcMapper;
	private int maxBatchSize;
	private final List<ParamIndex> paramIndices = new ArrayList<>();
	private final Map<String, JdbcParam> params = new HashMap<>();
	private PreparedStatement preparedStatement;
	private boolean shouldInitJdbcSql = true;
	private boolean shouldInitSql = true;
	private boolean shouldReplacePreparedStatement;
	private String sql;
	private SqlStatement sqlStatement;
	
	public List<int[]> getBatchRowCountsList() { return batchRowCountsList; }
	public int getMaxBatchSize() { return maxBatchSize; }
	public SqlStatement getSqlStatement() { return sqlStatement; }
	
	public JdbcStatement(JdbcMapper jdbcMapper) {
		this(jdbcMapper, null);
	}
	
	/**
	 * WARN: SqlStatement may still be mutable
	 * @param sqlStatement
	 * @param jdbcMapper
	 */
	public JdbcStatement(JdbcMapper jdbcMapper, SqlStatement sqlStatement) {
		if (jdbcMapper == null) {
			throw new NullPointerException("jdbcMapper cannot be null");
		}
		
		this.jdbcMapper = jdbcMapper;
		sqlStatement(sqlStatement);
	}

	/**
	 * 
	 * @param stmt the <code>PreparedStatement</code> to close unless it is the cached statement
	 * because <code>JdbcStatement.cachePreparedStatement(boolean)</code> was called with true
	 */
	public void close(PreparedStatement stmt) {
		if (stmt == preparedStatement) {
			return;
		}
		
		JdbcUtils.closeQuietly(stmt);
	}
	
	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		return createPreparedStatement(connection, false);
	}
	
	public PreparedStatement createPreparedStatement(Connection connection, boolean autoGeneratedKeys) throws SQLException {
		initJdbcSql();
		PreparedStatement stmt = null;
		try {
			stmt = autoGeneratedKeys 
					? connection.prepareStatement(jdbcSql, Statement.RETURN_GENERATED_KEYS) 
					: connection.prepareStatement(jdbcSql);
			stmt = new PreparedStatementImpl(stmt, connection);
			setParameters(stmt);
			return stmt;
		} catch (SQLException e) {
			close(stmt);
			throw e;
		}
	}

	public int executeUpdate(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		
		try {
			stmt = createPreparedStatement(connection);
			int rowCount = stmt.executeUpdate();
			return rowCount;
		} catch (SQLException e) {
			throw e;
		} finally {
			close(stmt);
		}
	}

	/**
	 * 
	 * @param connection
	 * @return May return the same <code>PreparedStatement</code> object if <code>PreparedStatement.cachePreparedStatement(boolean)</code> was called with true
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(Connection connection) throws SQLException {
		return getPreparedStatement(connection, false);
	}
	
	/**
	 * 
	 * @param connection
	 * @param autoGeneratedKeys
	 * @return May return the same <code>PreparedStatement</code> object if <code>PreparedStatement.cachePreparedStatement(boolean)</code> was called with true
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(Connection connection, boolean autoGeneratedKeys) throws SQLException {
		initJdbcSql();

		if (preparedStatement != null) {
			if (!shouldReplacePreparedStatement) {
				setParameters(preparedStatement);
				return preparedStatement;
			}
			
			closePreparedStatement();
		}
		
		PreparedStatement stmt = createPreparedStatement(connection, autoGeneratedKeys);
		
		if (cachePreparedStatement) {
			preparedStatement = stmt;
			shouldReplacePreparedStatement = false;
		}
		return stmt;
	}
	
	public void setParameters(PreparedStatement stmt) throws SQLException {
		for (ParamIndex index : paramIndices) {
			JdbcParam param = index.getParam();
			int parameterIndex = index.getIndex();
			
			switch (param.getParamType()) {
				case PARAM_TYPE_BIG_DECIMAL:
					stmt.setBigDecimal(parameterIndex, param.getValue());
					break;
				case PARAM_TYPE_BOOLEAN:
					Boolean booleanObj = param.getValue();
					if (booleanObj != null) {
						stmt.setBoolean(parameterIndex, booleanObj);
					} else {
						stmt.setNull(parameterIndex, Types.BOOLEAN);
					}
					break;
				case PARAM_TYPE_DATE:
					stmt.setDate(parameterIndex, param.getValue());
					break;
				case PARAM_TYPE_DOUBLE:
					Double doubleObj = param.getValue();
					if (doubleObj != null) {
						stmt.setDouble(parameterIndex, doubleObj);
					} else {
						stmt.setNull(parameterIndex, Types.DOUBLE);
					}
					break;
				case PARAM_TYPE_FLOAT:
					Float floatObj = param.getValue();
					if (floatObj != null) {
						stmt.setFloat(parameterIndex, floatObj);
					} else {
						stmt.setNull(parameterIndex, Types.FLOAT);
					}
					break;
				case PARAM_TYPE_INTEGER:
					Integer integer = param.getValue();
					if (integer != null) {
						stmt.setInt(parameterIndex, integer);
					} else {
						stmt.setNull(parameterIndex, Types.INTEGER);
					}
					break;
				case PARAM_TYPE_LIST_BIG_DECIMAL:
					CollectionParam bigDecimalsParam = (CollectionParam)param;
					
					for (BigDecimal bigDecimal : (Collection<BigDecimal>)bigDecimalsParam.getCollection()) {
						stmt.setBigDecimal(parameterIndex++, bigDecimal);
					}
					break;
				case PARAM_TYPE_LIST_DATE:
					CollectionParam datesParam = (CollectionParam)param;
					
					for (java.sql.Date date : (Collection<java.sql.Date>)datesParam.getCollection()) {
						stmt.setDate(parameterIndex++, date);
					}
					break;
				case PARAM_TYPE_LIST_DOUBLE:
					CollectionParam doublesParam = (CollectionParam)param;
					
					for (Double doubleElement : (Collection<Double>)doublesParam.getCollection()) {
						if (doubleElement != null) {
							stmt.setDouble(parameterIndex++, doubleElement);
						} else {
							stmt.setNull(parameterIndex++, Types.DOUBLE);
						}
					}
					break;
				case PARAM_TYPE_LIST_FLOAT:
					CollectionParam floatsParam = (CollectionParam)param;
					
					for (Float floatElement : (Collection<Float>)floatsParam.getCollection()) {
						if (floatElement != null) {
							stmt.setFloat(parameterIndex++, floatElement);
						} else {
							stmt.setNull(parameterIndex++, Types.FLOAT);
						}
					}
					break;
				case PARAM_TYPE_LIST_INTEGER:
					CollectionParam integersParam = (CollectionParam)param;
					
					for (Integer integerElement : (Collection<Integer>)integersParam.getCollection()) {
						if (integerElement != null) {
							stmt.setInt(parameterIndex++, integerElement);
						} else {
							stmt.setNull(parameterIndex++, Types.INTEGER);
						}
					}
					break;
				case PARAM_TYPE_LIST_LONG:
					CollectionParam longsParam = (CollectionParam)param;
					
					for (Long longElement : (Collection<Long>)longsParam.getCollection()) {
						if (longElement != null) {
							stmt.setLong(parameterIndex++, longElement);
						} else {
							stmt.setNull(parameterIndex++, Types.BIGINT);
						}
					}
					break;
				case PARAM_TYPE_LIST_STRING:
					CollectionParam stringsParam = (CollectionParam)param;
					
					for (String str : (Collection<String>)stringsParam.getCollection()) {
						stmt.setString(parameterIndex++, str);
					}
					break;
				case PARAM_TYPE_LIST_TIMESTAMP:
					CollectionParam timestampsParam = (CollectionParam)param;
					
					for (java.sql.Timestamp timestamp : (Collection<java.sql.Timestamp>)timestampsParam.getCollection()) {
						stmt.setTimestamp(parameterIndex++, timestamp);
					}
					break;
				case PARAM_TYPE_LONG:
					Long longObj = param.getValue();
					if (longObj != null) {
						stmt.setLong(parameterIndex, longObj);
					} else {
						stmt.setNull(parameterIndex, Types.BIGINT);
					}
					break;
				case PARAM_TYPE_PRIMITIVE_BOOLEAN:
					BooleanParam booleanParam = (BooleanParam)param;
					stmt.setBoolean(parameterIndex, booleanParam.getBooleanValue());
					break;
				case PARAM_TYPE_PRIMITIVE_DOUBLE:
					DoubleParam doubleParam = (DoubleParam)param;
					stmt.setDouble(parameterIndex, doubleParam.getDoubleValue());
					break;
				case PARAM_TYPE_PRIMITIVE_FLOAT:
					FloatParam floatParam = (FloatParam)param;
					stmt.setFloat(parameterIndex, floatParam.getFloatValue());
					break;
				case PARAM_TYPE_PRIMITIVE_INT:
					IntParam intParam = (IntParam)param;
					stmt.setInt(parameterIndex, intParam.getIntValue());
					break;
				case PARAM_TYPE_PRIMITIVE_LONG:
					LongParam longParam = (LongParam)param;
					stmt.setLong(parameterIndex, longParam.getLongValue());
					break;
				case PARAM_TYPE_STRING:
					stmt.setString(parameterIndex, param.getValue());
					break;
				case PARAM_TYPE_TIMESTAMP:
					stmt.setTimestamp(parameterIndex, param.getValue());
					break;
			}
		}
	}

	public <T, C extends Collection<T>> C toJdbcDataTypeCollection(Connection connection, 
			Class<T> clazz, 
			C collection) 
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSetHelper rs = null;
		
		try {
			stmt = createPreparedStatement(connection);
			rs = new ResultSetHelper(stmt.executeQuery());
			
			return toJdbcDataTypeCollection(clazz, rs, collection);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}

	public <T, C extends Collection<T>> C toCollection(
			Class<T> clazz, 
			ResultSet rs, 
			C collection) 
			throws SQLException {
		return jdbcMapper.toCollection(clazz, (Select)sqlStatement, rs, collection);
	}
	
	public <T, C extends Collection<T>> C toCollection(
			Connection connection, 
			Class<T> clazz, 
			C collection) 
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			
			return toCollection(clazz, rs, collection);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}
	
	public <T, C extends Collection<T>> C toCollection(
			Connection connection, 
			GraphEntity graphEntity, 
			C collection, 
			ObjectGraphResolver graphResolver) 
			throws SQLException {
		return graphResolver.toCollection(connection, this, graphEntity, collection);
	}
	
	public List<JsonObject> toJsonList(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = createPreparedStatement(connection);
			rs = stmt.executeQuery();
			
			return JdbcUtils.toJsonList(stmt);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}

	public <T> LinkedList<T> toLinkedList(Connection connection, Class<T> clazz) throws SQLException {
		return toCollection(connection, clazz, new LinkedList<>());
	}
	
	public <K, T> Map<K, T> toLinkedMap(Connection connection, Class<T> clazz) throws SQLException {
		return toLinkedMap(connection, clazz, null);
	}
	
	public <K, T> Map<K, T> toLinkedMap(Connection connection, Class<T> clazz, String mapKeyName) throws SQLException {
		return toMap(connection, clazz, mapKeyName, new LinkedHashMap<>());
	}
	
	/**
	 * May have removed duplicates if <code>clazz</code> implements <code>equals()</code> or <code>hashcode()</code> methods
	 * @param connection
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public <T> Set<T> toLinkedSet(Connection connection, Class<T> clazz) throws SQLException {
		return toCollection(connection, clazz, new LinkedHashSet<>());
	}
	
	public <T> List<T> toList(Connection connection, Class<T> clazz) throws SQLException {
		return toCollection(connection, clazz, new ArrayList<>());
	}

	public <K, T> Map<K, T> toMap(Connection connection, Class<T> clazz) throws SQLException {
		return toMap(connection, clazz, null);
	}
	
	public <K, T> Map<K, T> toMap(Connection connection, Class<T> clazz, String mapKeyName) throws SQLException {
		return toMap(connection, clazz, mapKeyName, new HashMap<>());
	}
	
	public <K, T> Map<K, T> toMap(Connection connection, Class<T> clazz, String mapKeyName, Map<K, T> map) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			
			return jdbcMapper.toMap(clazz, (Select)sqlStatement, rs, map, mapKeyName);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}
	
	/**
	 * May have removed duplicates if <code>clazz</code> implements <code>equals()</code> or <code>hashcode()</code> methods
	 * @param connection
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public <T> Set<T> toSet(Connection connection, Class<T> clazz) throws SQLException {
		return toCollection(connection, clazz, new HashSet<>());
	}

	public <T> Stack<T> toStack(Connection connection, Class<T> clazz) throws SQLException {
		return toCollection(connection, clazz, new Stack<>());
	}
	
	public <T> T uniqueResult(Connection connection, Class<T> clazz) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				return null;
			}
			T object = jdbcMapper.toObject(clazz, (Select)sqlStatement, rs);
			
			if (rs.next()) {
				throw new SQLException("object of the type (" + clazz + ") is not unique");
			}
			return object;
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}
	
	/* BEGIN Fluent API */

	/**
	 * NOTE: Batch processing for SELECT statements are not supported. See <code>http://stackoverflow.com/q/7899543</code>.
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement addBatch() throws SQLException {
		if (preparedStatement == null) {
			throw new IllegalStateException("call JdbcStatement.addBatch(Connection) to initialize private PreparedStatement object for batch processing");
		}
		
		try {
			setParameters(preparedStatement);
			addBatch0();
		} catch (SQLException e) {
			closePreparedStatement();
			throw e;
		}
		return this;
	}
	
	/**
	 * NOTE: Batch processing for SELECT statements are not supported. See <code>http://stackoverflow.com/q/7899543</code>.
	 * @return
	 * @throws SQLException
	 */
	public JdbcStatement addBatch(Connection connection) throws SQLException {
		if (preparedStatement != null) {
			return addBatch();
		}

		if (batchRowCountsList == null) {
			batchRowCountsList = new ArrayList<>();
		} else {
			batchRowCountsList.clear();
		}
		
		try {
			cachePreparedStatement(true);
			getPreparedStatement(connection);
			addBatch0();
		} catch (SQLException e) {
			closePreparedStatement();
			throw e;
		}
		return this;
	}

	/**
	 * Must call <code>JdbcStatement.closePreparedStatement()</code> after use.
	 * @param cacheStatement If true, caches and reuses the <code>PreparedStatement</code> object instead of creating new instances for every database operation.
	 * The exception is when any of the <code>set*(String, Collection)</code> methods are called because an IN parameter list e.g. IN (?[, ...]), is changed.
	 * @return
	 */
	public JdbcStatement cachePreparedStatement(boolean cachePreparedStatement) {
		this.cachePreparedStatement = cachePreparedStatement;
		return this;
	}
	
	/**
	 * Closes cached <code>PreparedStatement</code> object. See <code>JdbcStatement.cachePreparedStatement(boolean)</code> method.
	 * @return
	 */
	public JdbcStatement closePreparedStatement() {
		JdbcUtils.closeQuietly(preparedStatement);
		preparedStatement = null;
		return this;
	}
	
	public JdbcStatement executeBatch() throws SQLException {
		return executeBatch(true);
	}
	
	public JdbcStatement executeBatch(boolean close) throws SQLException {
		try {
			if (batchSize > 0) {
				int[] rowCounts = preparedStatement.executeBatch();
				batchRowCountsList.add(rowCounts);
			}
			
			batchSize = 0;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (close) {
				closePreparedStatement();
			}
		}
		return this;
	}

	public JdbcStatement maxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
		return this;
	}
	
	public JdbcStatement setBigDecimal(String name, BigDecimal x) {
		return setObject(name, PARAM_TYPE_BIG_DECIMAL, x);
	}

	public JdbcStatement setBigDecimals(String name, Collection<BigDecimal> x) {
		return setCollection(name, PARAM_TYPE_LIST_BIG_DECIMAL, x);
	}
	
	public JdbcStatement setBoolean(String name, Boolean x) {
		return setObject(name, PARAM_TYPE_BOOLEAN, x);
	}
	
	public JdbcStatement setBoolean(String name, boolean x) {
		BooleanParam param = (BooleanParam)params.get(name);
		
		if (param == null) {
			param = new BooleanParam(name, x);
			params.put(name, param);
		} else {
			param.setBooleanValue(x);
		}
		return this;
	}
	
	/**
	 * Will create a java.sql.Date object if x is not null
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setDate(String name, Date x) {
		if (x != null) {
			x = new java.sql.Date(x.getTime());
		}
		return setObject(name, PARAM_TYPE_DATE, x);
	}
	
	/**
	 * Will create an ArrayList of java.sql.Date objects
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setDates(String name, Collection<Date> x) {
		List<java.sql.Date> dates = new ArrayList<>();
		
		for (Date date : x) {
			if (date == null) {
				continue;
			}
			dates.add(new java.sql.Date(date.getTime()));
		}
		return setCollection(name, PARAM_TYPE_LIST_DATE, dates);
	}
	
	public JdbcStatement setDouble(String name, Double x) {
		return setObject(name, PARAM_TYPE_DOUBLE, x);
	}
	
	public JdbcStatement setDouble(String name, double x) {
		DoubleParam param = (DoubleParam)params.get(name);
		
		if (param == null) {
			param = new DoubleParam(name, x);
			params.put(name, param);
		} else {
			param.setDoubleValue(x);
		}
		return this;
	}
	
	public JdbcStatement setDoubles(String name, Collection<Double> x) {
		return setCollection(name, PARAM_TYPE_LIST_DOUBLE, x);
	}
	
	public JdbcStatement setFloat(String name, Float x) {
		return setObject(name, PARAM_TYPE_FLOAT, x);
	}
	
	public JdbcStatement setFloat(String name, float x) {
		FloatParam param = (FloatParam)params.get(name);
		
		if (param == null) {
			param = new FloatParam(name, x);
			params.put(name, param);
		} else {
			param.setFloatValue(x);
		}
		return this;
	}
	
	public JdbcStatement setFloats(String name, Collection<Float> x) {
		return setCollection(name, PARAM_TYPE_LIST_FLOAT, x);
	}
	
	public JdbcStatement setInteger(String name, Integer x) {
		return setObject(name, PARAM_TYPE_INTEGER, x);
	}
	
	public JdbcStatement setInteger(String name, int x) {
		IntParam param = (IntParam)params.get(name);
		
		if (param == null) {
			param = new IntParam(name, x);
			params.put(name, param);
		} else {
			param.setIntValue(x);
		}
		return this;
	}
	
	public JdbcStatement setIntegers(String name, Collection<Integer> x) {
		return setCollection(name, PARAM_TYPE_LIST_INTEGER, x);
	}
	
	public JdbcStatement setLong(String name, Long x) {
		return setObject(name, PARAM_TYPE_LONG, x);
	}
	
	public JdbcStatement setLong(String name, long x) {
		LongParam param = (LongParam)params.get(name);
		
		if (param == null) {
			param = new LongParam(name, x);
			params.put(name, param);
		} else {
			param.setLongValue(x);
		}
		return this;
	}
	
	public JdbcStatement setLongs(String name, Collection<Long> x) {
		return setCollection(name, PARAM_TYPE_LIST_LONG, x);
	}
	
	public JdbcStatement setParameter(FieldColumnMapping fieldColumnMapping, Object x) {
		String name = fieldColumnMapping.getColumn();
		
		switch (fieldColumnMapping.getJdbcType()) {
			case FieldColumnMapping.JDBC_TYPE_BIG_DECIMAL: setBigDecimal(name, (BigDecimal)x); break;
			case FieldColumnMapping.JDBC_TYPE_BOOLEAN: setBoolean(name, (Boolean)x); break;
			case FieldColumnMapping.JDBC_TYPE_DATE: setDate(name, (Date)x); break;
			case FieldColumnMapping.JDBC_TYPE_DOUBLE: setDouble(name, (Double)x); break;
			case FieldColumnMapping.JDBC_TYPE_FLOAT: setFloat(name, (Float)x); break;
			case FieldColumnMapping.JDBC_TYPE_INTEGER: setInteger(name, (Integer)x); break;
			case FieldColumnMapping.JDBC_TYPE_LONG: setLong(name, (Long)x); break;
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_BOOLEAN: setBoolean(name, (boolean)x); break;
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_DOUBLE: setDouble(name, (double)x); break;
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_FLOAT: setFloat(name, (float)x); break;
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_INT: setInteger(name, (int)x); break;
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_LONG: setLong(name, (long)x); break;
			case FieldColumnMapping.JDBC_TYPE_STRING: setString(name, (String)x); break;
			case FieldColumnMapping.JDBC_TYPE_TIMESTAMP: setTimestamp(name, (Date)x); break;
		}
		return this;
	}
	
	public JdbcStatement setParameterFrom(Object paramSrc, FieldColumnMapping fieldColumnMapping) {
		Object x = fieldColumnMapping.get(paramSrc);
		return setParameter(fieldColumnMapping, x);
	}
	
	public JdbcStatement setParametersFrom(Object paramSrc) {
		ClassRowMapping classRowMapping = jdbcMapper.getMappings().get(paramSrc.getClass());
		return setParametersFrom(paramSrc, classRowMapping);
	}
	
	public JdbcStatement setParametersFrom(Object paramSrc, ClassRowMapping classRowMapping) {
		for (FieldColumnMapping fieldColumnMapping : classRowMapping.getFieldColumnMappingList()) {
			setParameterFrom(paramSrc, fieldColumnMapping);
		}
		return this;
	}

	public JdbcStatement setParameterList(String name, FieldColumnMapping fieldColumnMapping, Collection x) {
		return setParameterList(name, fieldColumnMapping.getJdbcType(), x);
	}
	
	public JdbcStatement setParameterList(String name, int jdbcType, Collection x) {
		int paramType = 0;
		switch (jdbcType) {
			case FieldColumnMapping.JDBC_TYPE_BIG_DECIMAL: paramType = PARAM_TYPE_LIST_BIG_DECIMAL; break;
			case FieldColumnMapping.JDBC_TYPE_DATE: paramType = PARAM_TYPE_LIST_DATE; break;
			case FieldColumnMapping.JDBC_TYPE_DOUBLE:
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_DOUBLE: paramType = PARAM_TYPE_LIST_DOUBLE; break;
			case FieldColumnMapping.JDBC_TYPE_FLOAT:
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_FLOAT: paramType = PARAM_TYPE_LIST_FLOAT; break;
			case FieldColumnMapping.JDBC_TYPE_INTEGER:
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_INT: paramType = PARAM_TYPE_LIST_INTEGER; break;
			case FieldColumnMapping.JDBC_TYPE_LONG:
			case FieldColumnMapping.JDBC_TYPE_PRIMITIVE_LONG: paramType = PARAM_TYPE_LIST_LONG; break;
			case FieldColumnMapping.JDBC_TYPE_STRING: paramType = PARAM_TYPE_LIST_STRING; break;
			case FieldColumnMapping.JDBC_TYPE_TIMESTAMP: paramType = PARAM_TYPE_LIST_TIMESTAMP; break;
			default:
				throw new IllegalArgumentException("jdbcType (" + jdbcType + ") cannot be converted to any param types");
		}
		return setCollection(name, paramType, x);
	}
	
	public JdbcStatement setString(String name, String x) {
		return setObject(name, PARAM_TYPE_STRING, x);
	}
	
	public JdbcStatement setStrings(String name, Collection<String> x) {
		return setCollection(name, PARAM_TYPE_LIST_STRING, x);
	}
	
	/**
	 * Will create a java.sql.Timestamp object if x is not null
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setTimestamp(String name, Date x) {
		if (x != null) {
			x = new java.sql.Timestamp(x.getTime());
		}
		return setObject(name, PARAM_TYPE_TIMESTAMP, x);
	}
	
	/**
	 * Will create an ArrayList of java.sql.Timestamp objects
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setTimestamps(String name, Collection<Date> x) {
		List<java.sql.Timestamp> timestamps = new ArrayList<>();
		
		for (Date date : x) {
			if (date == null) {
				continue;
			}
			timestamps.add(new java.sql.Timestamp(date.getTime()));
		}
		return setCollection(name, PARAM_TYPE_LIST_TIMESTAMP, x);
	}
	
	public JdbcStatement sqlStatement(SqlStatement sqlStatement) {
		this.sqlStatement = sqlStatement;
		shouldInitJdbcSql = true;
		shouldInitSql = true;
		return this;
	}
	
	/* END Fluent API */
	
	/* BEGIN Private methods */

	private void addBatch0() throws SQLException {
		preparedStatement.addBatch();
		batchSize++;
		
		if (maxBatchSize > 0 && batchSize >= maxBatchSize) {
			executeBatch(false);
		}
	}
	
	private void addParamIndex(JdbcParam param) {
		int index = 1;
		
		ParamIndex prevIndex = !paramIndices.isEmpty() 
				? paramIndices.get(paramIndices.size() - 1) : null;
		if (prevIndex != null) {
			index = prevIndex.getNextIndex();
		}
		
		ParamIndex paramIndex = new ParamIndex(param, index);
		paramIndices.add(paramIndex);
	}
	
	private void appendPlaceholders(StringBuilder builder, JdbcParam param) {
		switch (param.getParamType()) {
			case PARAM_TYPE_LIST_BIG_DECIMAL:
			case PARAM_TYPE_LIST_DATE:
			case PARAM_TYPE_LIST_DOUBLE:
			case PARAM_TYPE_LIST_FLOAT:
			case PARAM_TYPE_LIST_INTEGER:
			case PARAM_TYPE_LIST_LONG:
			case PARAM_TYPE_LIST_STRING:
			case PARAM_TYPE_LIST_TIMESTAMP:
				builder.append("(");
				
				CollectionParam collectionParam = (CollectionParam)param;
				int placeholders = collectionParam.getCollection().size();
				
				for (int i = 0; i < placeholders; i++) {
					if (i > 0) {
						builder.append(", ");
					}
					builder.append("?");
				}
				builder.append(")");
				break;
			default:
				builder.append("?");
				break;
		}
	}
	
	private Set<String> findParams() {
		ParamFinder finder = new ParamFinder();
		sqlStatement.accept(finder);
		return finder.getNames();
	}
	
	/**
	 * jdbcSql and paramIndices are nullified and cleared respectively
	 * @return true if JDBC SQL string was (re-)initialized
	 */
	private void initJdbcSql() {
		if (!shouldInitJdbcSql) {
			return;
		}
		initSql();
		
		jdbcSql = null;
		paramIndices.clear();
		shouldInitJdbcSql = false;
		shouldReplacePreparedStatement = true;
		
		StringBuilder builder = new StringBuilder();
		int index = 0;
		Set<String> paramNames = findParams();
		
		Matcher matcher = PARAM_PATTERN.matcher(sql);
		while (matcher.find()) {
			String beforeParam = sql.substring(index, matcher.start());
			builder.append(beforeParam);
			
			String paramPart = sql.substring(matcher.start(), matcher.end());
			String paramName = paramPart.substring(1); // Ignores the ':' at the start
			
			JdbcParam param = paramNames.contains(paramName) ? 
					params.get(paramName) : null;
			if (param != null) {
				addParamIndex(param);
				appendPlaceholders(builder, param);
			} else {
				builder.append(paramPart);
			}
			
			index = matcher.end();
		}
		if (index < sql.length()) {
			builder.append(sql.substring(index));
		}
		
		jdbcSql = builder.toString();
	}
	
	private void initSql() {
		if (!shouldInitSql) {
			return;
		} else if (sqlStatement == null) {
			throw new IllegalStateException("sqlStatement is null, call JdbcStatement.sqlStatement(SqlStatement) method or construct JdbcStatement with SqlStatement parameter");
		}
		
		sql = jdbcMapper.getVendor().print(sqlStatement);
		logger.debug("JdbcStatement sql: {}", sql);
		shouldInitSql = false;
	}
	
	private JdbcStatement setCollection(String name, int paramType, Collection x) {
		if (x == null || x.isEmpty()) {
			throw new IllegalArgumentException("collection of param '" + name + "' cannot be null or empty");
		}
		
		CollectionParam param = (CollectionParam)params.get(name);
		
		if (param == null) {
			param = new CollectionParam(name, paramType, x);
			params.put(name, param);
		} else {
			if (!shouldInitJdbcSql && param.getCollection().size() != x.size()) {
				shouldInitJdbcSql = true;
			}
			
			param.setCollection(x);
		}
		return this;
	}
	
	private JdbcStatement setObject(String name, int paramType, Object x) {
		JdbcParam param = params.get(name);
		
		if (param == null) {
			param = new JdbcParam(name, paramType, x);
			params.put(name, param);
		} else {
			param.setValue(x);
		}
		return this;
	}
	
	/* END Private methods */
	
	/* BEGIN Inner classes */
	
	private class BooleanParam extends JdbcParam {
		private boolean booleanValue;
		
		public boolean getBooleanValue() { return booleanValue; }
		public void setBooleanValue(boolean booleanValue) { this.booleanValue = booleanValue; }

		public BooleanParam(String name, boolean booleanValue) {
			super(name, PARAM_TYPE_PRIMITIVE_BOOLEAN);
			this.booleanValue = booleanValue;
		}
	}
	
	private class CollectionParam extends JdbcParam {
		private Collection collection;
		
		public Collection getCollection() { return collection; }
		public void setCollection(Collection collection) { this.collection = collection; }
		
		public CollectionParam(String name, int paramType, Collection collection) {
			super(name, paramType);
			this.collection = collection;
		}
	}
	
	private class DoubleParam extends JdbcParam {
		private double doubleValue;
		
		public double getDoubleValue() { return doubleValue; }
		public void setDoubleValue(double doubleValue) { this.doubleValue = doubleValue; }
		
		public DoubleParam(String name, double doubleValue) {
			super(name, PARAM_TYPE_PRIMITIVE_DOUBLE);
			this.doubleValue = doubleValue;
		}
	}
	
	private class FloatParam extends JdbcParam {
		private float floatValue;
		
		public float getFloatValue() { return floatValue; }
		public void setFloatValue(float floatValue) { this.floatValue = floatValue; }
		
		public FloatParam(String name, float floatValue) {
			super(name, PARAM_TYPE_PRIMITIVE_FLOAT);
			this.floatValue = floatValue;
		}
	}
	
	private class IntParam extends JdbcParam {
		private int intValue;
		
		public int getIntValue() { return intValue; }
		public void setIntValue(int intValue) { this.intValue = intValue; }
		
		public IntParam(String name, int intValue) {
			super(name, PARAM_TYPE_PRIMITIVE_INT);
			this.intValue = intValue;
		}
	}

	private class JdbcParam {
		private final int paramType;
		private final String name;
		private Object value;
		
		public int getParamType() { return paramType; }
		@SuppressWarnings("unused")
		public String getName() { return name; }
		public <T> T getValue() { return (T)value; }
		public void setValue(Object value) { this.value = value; }
		
		public JdbcParam(String name, int paramType, Object value) {
			this.paramType = paramType;
			this.name = name;
			this.value = value;
		}
		
		protected JdbcParam(String name, int paramType) {
			this(name, paramType, null);
		}
	}
	
	private class LongParam extends JdbcParam {
		private long longValue;
		
		public long getLongValue() { return longValue; }
		public void setLongValue(long longValue) { this.longValue = longValue; }
		
		public LongParam(String name, long longValue) {
			super(name, PARAM_TYPE_PRIMITIVE_LONG);
			this.longValue = longValue;
		}
	}
	
	private class ParamFinder implements NodeVisitor {
		private final Set<String> names = new LinkedHashSet<>();
		
		public Set<String> getNames() { return names; }
		
		@Override
		public boolean visit(Node node) {
			if (node.getType() != Node.TYPE_PARAM) {
				return true;
			}
			
			Param param = (Param)node;
			names.add(param.getName());
			return true;
		}
	}
	
	private class ParamIndex {
		private final boolean collection;
		private final JdbcParam param;
		private final int index;
		
		public JdbcParam getParam() { return param; }
		public int getIndex() { return index; }
		
		public ParamIndex(JdbcParam param, int index) {
			switch (param.getParamType()) {
				case PARAM_TYPE_LIST_BIG_DECIMAL:
				case PARAM_TYPE_LIST_DATE:
				case PARAM_TYPE_LIST_DOUBLE:
				case PARAM_TYPE_LIST_FLOAT:
				case PARAM_TYPE_LIST_INTEGER:
				case PARAM_TYPE_LIST_LONG:
				case PARAM_TYPE_LIST_STRING:
				case PARAM_TYPE_LIST_TIMESTAMP:
					collection = true;
					break;
				default:
					collection = false;
					break;
			}
			
			this.index = index;
			this.param = param;
		}
		
		public int getNextIndex() {
			if (collection) {
				CollectionParam collectionParam = (CollectionParam)param;
				return index + collectionParam.getCollection().size();
			}
			return index + 1;
		}
	}

	/* END Inner classes */
}