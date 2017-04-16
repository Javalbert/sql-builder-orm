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
package com.github.javalbert.orm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.NodeVisitor;
import com.github.javalbert.sqlbuilder.Param;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SqlStatement;
import com.github.javalbert.sqlbuilder.parser.SqlParser;
import com.github.javalbert.utils.ClassUtils;
import com.github.javalbert.utils.jdbc.JdbcUtils;
import com.github.javalbert.utils.jdbc.PreparedStatementImpl;
import com.github.javalbert.utils.jdbc.ResultSetHelper;

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
	private static final int PARAM_TYPE_LIST_LOCAL_DATE = 12;
	private static final int PARAM_TYPE_LIST_LOCAL_DATE_TIME = 13;
	private static final int PARAM_TYPE_LIST_LONG = 14;
	private static final int PARAM_TYPE_LIST_STRING = 15;
	private static final int PARAM_TYPE_LIST_TIMESTAMP = 16;
	private static final int PARAM_TYPE_LOCAL_DATE = 17;
	private static final int PARAM_TYPE_LOCAL_DATE_TIME = 18;
	private static final int PARAM_TYPE_LONG = 19;
	private static final int PARAM_TYPE_PRIMITIVE_BOOLEAN = 20;
	private static final int PARAM_TYPE_PRIMITIVE_DOUBLE = 21;
	private static final int PARAM_TYPE_PRIMITIVE_FLOAT = 22;
	private static final int PARAM_TYPE_PRIMITIVE_INT = 23;
	private static final int PARAM_TYPE_PRIMITIVE_LONG = 24;
	private static final int PARAM_TYPE_STRING = 25;
	private static final int PARAM_TYPE_TIMESTAMP = 26;
	
	private static final Pattern PARAM_PATTERN = Pattern.compile(":\\w+");
	
	private static <T> Collection<T> toJdbcDataTypeCollection(
			Class<T> clazz,
			ResultSetHelper rs,
			Collection<T> collection) throws SQLException {
		switch (clazz.getCanonicalName()) {
			case ClassUtils.NAME_BOOLEAN:
			case ClassUtils.NAME_JAVA_LANG_BOOLEAN:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getBoolean2(1)));
				}
				break;
			case ClassUtils.NAME_DOUBLE:
			case ClassUtils.NAME_JAVA_LANG_DOUBLE:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getDouble2(1)));
				}
				break;
			case ClassUtils.NAME_FLOAT:
			case ClassUtils.NAME_JAVA_LANG_FLOAT:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getFloat2(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_LANG_INTEGER:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getInt2(1)));
				}
				break;
			case ClassUtils.NAME_LONG:
			case ClassUtils.NAME_JAVA_LANG_LONG:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getLong2(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_LANG_STRING:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getString(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_MATH_BIG_DECIMAL:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getBigDecimal(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_TIME_LOCAL_DATE:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getLocalDate(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_TIME_LOCAL_DATE_TIME:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getLocalDateTime(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_SQL_TIMESTAMP:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getTimestamp(1)));
				}
				break;
			case ClassUtils.NAME_JAVA_UTIL_DATE:
				while (rs.next()) {
					collection.add(clazz.cast(rs.getDate2(1)));
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
	@SuppressWarnings("rawtypes")
	private SqlStatement sqlStatement;
	
	public List<int[]> getBatchRowCountsList() { return batchRowCountsList; }
	public int getMaxBatchSize() { return maxBatchSize; }
	void setSql(String sql) {
		this.sql = Objects.requireNonNull(sql, "sql cannot be null");
		shouldInitSql = false;
	}
	@SuppressWarnings("rawtypes")
	public SqlStatement getSqlStatement() { return sqlStatement; }
	
	/* START Constructors */
	
	public JdbcStatement(JdbcMapper jdbcMapper) {
		this(jdbcMapper, (SqlStatement<?>)null);
	}
	
	/**
	 * WARN: SqlStatement may still be mutable
	 * @param sqlStatement
	 * @param jdbcMapper
	 */
	public JdbcStatement(JdbcMapper jdbcMapper, SqlStatement<?> sqlStatement) {
		this.jdbcMapper = Objects.requireNonNull(jdbcMapper, "jdbcMapper cannot be null");
		sqlStatement(sqlStatement);
	}
	
	/**
	 * If <b>sql</b> contains any parameters, they must be named parameters
	 * i.e. instead of <code>?</code>, use <code>:parameterName</code>
	 * @param jdbcMapper
	 * @param sql
	 */
	public JdbcStatement(JdbcMapper jdbcMapper, String sql) {
		this.jdbcMapper = Objects.requireNonNull(jdbcMapper, "jdbcMapper cannot be null");
		
		// Calling sqlStatement() will set shouldInitSql = true ...
		sqlStatement(new SqlParser().parse(sql).getSqlStatement());
		// ... while calling setSql() will set shouldInitSql = false
		// which should improve performance
		setSql(sql);
	}
	
	/* END Constructors */

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
	
	public <T> void forEach(Connection connection, Class<T> clazz, BiConsumer<? super T, ResultSetHelper> consumer)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			
			jdbcMapper.forEach(clazz, (Select)sqlStatement, rs, consumer);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}
	
	public void forEachRow(Connection connection, Consumer<ResultSetHelper> consumer)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSetHelper rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = new ResultSetHelper(stmt.executeQuery());
			
			while (rs.next()) {
				consumer.accept(rs);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
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
	
	public <T> T getSingleResult(Connection connection, Class<T> clazz) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			return rs.next() ? jdbcMapper.toObject(clazz, (Select)sqlStatement, rs) : null;
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}
	
	public void setParameters(PreparedStatement stmt) throws SQLException {
		for (ParamIndex index : paramIndices) {
			JdbcParam param = index.getParam();
			int parameterIndex = index.getIndex();
			
			switch (param.getParamType()) {
				case PARAM_TYPE_BIG_DECIMAL:
					stmt.setBigDecimal(parameterIndex, (BigDecimal)param.getValue());
					break;
				case PARAM_TYPE_BOOLEAN:
					Boolean booleanObj = (Boolean)param.getValue();
					if (booleanObj != null) {
						stmt.setBoolean(parameterIndex, booleanObj);
					} else {
						stmt.setNull(parameterIndex, Types.BOOLEAN);
					}
					break;
				case PARAM_TYPE_DATE:
				case PARAM_TYPE_LOCAL_DATE:
					stmt.setDate(parameterIndex, (java.sql.Date)param.getValue());
					break;
				case PARAM_TYPE_DOUBLE:
					Double doubleObj = (Double)param.getValue();
					if (doubleObj != null) {
						stmt.setDouble(parameterIndex, doubleObj);
					} else {
						stmt.setNull(parameterIndex, Types.DOUBLE);
					}
					break;
				case PARAM_TYPE_FLOAT:
					Float floatObj = (Float)param.getValue();
					if (floatObj != null) {
						stmt.setFloat(parameterIndex, floatObj);
					} else {
						stmt.setNull(parameterIndex, Types.FLOAT);
					}
					break;
				case PARAM_TYPE_INTEGER:
					Integer integer = (Integer)param.getValue();
					if (integer != null) {
						stmt.setInt(parameterIndex, integer);
					} else {
						stmt.setNull(parameterIndex, Types.INTEGER);
					}
					break;
				case PARAM_TYPE_LIST_BIG_DECIMAL:
					CollectionParam bigDecimalsParam = (CollectionParam)param;
					
					for (BigDecimal bigDecimal : bigDecimalsParam.<BigDecimal>getCollection()) {
						stmt.setBigDecimal(parameterIndex++, bigDecimal);
					}
					break;
				case PARAM_TYPE_LIST_DATE:
				case PARAM_TYPE_LIST_LOCAL_DATE:
					CollectionParam datesParam = (CollectionParam)param;
					
					for (java.sql.Date date : datesParam.<java.sql.Date>getCollection()) {
						stmt.setDate(parameterIndex++, date);
					}
					break;
				case PARAM_TYPE_LIST_DOUBLE:
					CollectionParam doublesParam = (CollectionParam)param;
					
					for (Double doubleElement : doublesParam.<Double>getCollection()) {
						if (doubleElement != null) {
							stmt.setDouble(parameterIndex++, doubleElement);
						} else {
							stmt.setNull(parameterIndex++, Types.DOUBLE);
						}
					}
					break;
				case PARAM_TYPE_LIST_FLOAT:
					CollectionParam floatsParam = (CollectionParam)param;
					
					for (Float floatElement : floatsParam.<Float>getCollection()) {
						if (floatElement != null) {
							stmt.setFloat(parameterIndex++, floatElement);
						} else {
							stmt.setNull(parameterIndex++, Types.FLOAT);
						}
					}
					break;
				case PARAM_TYPE_LIST_INTEGER:
					CollectionParam integersParam = (CollectionParam)param;
					
					for (Integer integerElement : integersParam.<Integer>getCollection()) {
						if (integerElement != null) {
							stmt.setInt(parameterIndex++, integerElement);
						} else {
							stmt.setNull(parameterIndex++, Types.INTEGER);
						}
					}
					break;
				case PARAM_TYPE_LIST_LOCAL_DATE_TIME:
				case PARAM_TYPE_LIST_TIMESTAMP:
					CollectionParam timestampsParam = (CollectionParam)param;
					
					for (java.sql.Timestamp timestamp : timestampsParam.<java.sql.Timestamp>getCollection()) {
						stmt.setTimestamp(parameterIndex++, timestamp);
					}
					break;
				case PARAM_TYPE_LIST_LONG:
					CollectionParam longsParam = (CollectionParam)param;
					
					for (Long longElement : longsParam.<Long>getCollection()) {
						if (longElement != null) {
							stmt.setLong(parameterIndex++, longElement);
						} else {
							stmt.setNull(parameterIndex++, Types.BIGINT);
						}
					}
					break;
				case PARAM_TYPE_LIST_STRING:
					CollectionParam stringsParam = (CollectionParam)param;
					
					for (String str : stringsParam.<String>getCollection()) {
						stmt.setString(parameterIndex++, str);
					}
					break;
				case PARAM_TYPE_LOCAL_DATE_TIME:
				case PARAM_TYPE_TIMESTAMP:
					stmt.setTimestamp(parameterIndex, (java.sql.Timestamp)param.getValue());
					break;
				case PARAM_TYPE_LONG:
					Long longObj = (Long)param.getValue();
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
					stmt.setString(parameterIndex, (String)param.getValue());
					break;
			}
		}
	}

	/**
	 * A note on {@link Timestamp}:<br>
	 * This method will return a collection of {@link Timestamp} and never {@link Date} objects
	 * <br>
	 * @param connection
	 * @param clazz
	 * @param collection
	 * @return
	 * @throws SQLException
	 */
	public <T> Collection<T> toJdbcDataTypeCollection(
			Connection connection,
			Class<T> clazz,
			Collection<T> collection)
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

	public <T> Collection<T> toCollection(
			Class<T> clazz,
			ResultSet rs,
			Collection<T> collection)
			throws SQLException {
		return jdbcMapper.toCollection(clazz, (Select)sqlStatement, rs, collection);
	}
	
	public <T> Collection<T> toCollection(
			Connection connection,
			Class<T> clazz,
			Collection<T> collection)
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
	
	public <T> Collection<T> toCollection(
			Connection connection,
			GraphEntity<T> graphEntity,
			Collection<T> collection,
			ObjectGraphResolver graphResolver)
			throws SQLException {
		return graphResolver.toCollection(connection, this, graphEntity, collection);
	}

	public <T> Deque<T> toDeque(Connection connection, Class<T> clazz) throws SQLException {
		Deque<T> deque = new ArrayDeque<>();
		toCollection(connection, clazz, deque);
		return deque;
	}
	
	@SuppressWarnings("rawtypes")
	public Map toLinkedMap(Connection connection, Class<?> clazz) throws SQLException {
		return toLinkedMap(connection, clazz, null);
	}
	
	@SuppressWarnings("rawtypes")
	public Map toLinkedMap(Connection connection, Class<?> clazz, String mapKeyName) throws SQLException {
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
		Set<T> set = new LinkedHashSet<>();
		toCollection(connection, clazz, set);
		return set;
	}
	
	public <T> List<T> toList(Connection connection, Class<T> clazz) throws SQLException {
		List<T> list = new ArrayList<>();
		toCollection(connection, clazz, list);
		return list;
	}
	
	public List<Map<String, Object>> toListOfMaps(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			return toListOfMaps(rs);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			close(stmt);
		}
	}

	@SuppressWarnings("rawtypes")
	public Map toMap(Connection connection, Class<?> clazz) throws SQLException {
		return toMap(connection, clazz, null);
	}
	
	@SuppressWarnings("rawtypes")
	public Map toMap(Connection connection, Class<?> clazz, String mapKeyName) throws SQLException {
		return toMap(connection, clazz, mapKeyName, new HashMap<>());
	}
	
	@SuppressWarnings("rawtypes")
	public Map toMap(Connection connection, Class<?> clazz, String mapKeyName, Map map) throws SQLException {
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
	
	public List<Object[]> toResultList(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = getPreparedStatement(connection);
			rs = stmt.executeQuery();
			return toResultList(rs);
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
		Set<T> set = new HashSet<>();
		toCollection(connection, clazz, set);
		return set;
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
			param = new BooleanParam(x);
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
			dates.add(date != null ? new java.sql.Date(date.getTime()) : null);
		}
		return setCollection(name, PARAM_TYPE_LIST_DATE, dates);
	}
	
	public JdbcStatement setDouble(String name, Double x) {
		return setObject(name, PARAM_TYPE_DOUBLE, x);
	}
	
	public JdbcStatement setDouble(String name, double x) {
		DoubleParam param = (DoubleParam)params.get(name);
		
		if (param == null) {
			param = new DoubleParam(x);
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
			param = new FloatParam(x);
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
			param = new IntParam(x);
			params.put(name, param);
		} else {
			param.setIntValue(x);
		}
		return this;
	}
	
	public JdbcStatement setIntegers(String name, Collection<Integer> x) {
		return setCollection(name, PARAM_TYPE_LIST_INTEGER, x);
	}
	
	public JdbcStatement setLocalDate(String name, LocalDate x) {
		return setObject(name, PARAM_TYPE_LOCAL_DATE, x != null ? java.sql.Date.valueOf(x) : null);
	}
	
	public JdbcStatement setLocalDates(String name, Collection<LocalDate> x) {
		List<java.sql.Date> dates = new ArrayList<>();
		
		for (LocalDate localDate : x) {
			dates.add(localDate != null ? java.sql.Date.valueOf(localDate) : null);
		}
		return setCollection(name, PARAM_TYPE_LIST_LOCAL_DATE, dates);
	}
	
	public JdbcStatement setLocalDateTime(String name, LocalDateTime x) {
		return setObject(name, PARAM_TYPE_LOCAL_DATE_TIME, x != null ? java.sql.Timestamp.valueOf(x) : null);
	}
	
	public JdbcStatement setLocalDateTimes(String name, Collection<LocalDateTime> x) {
		List<java.sql.Timestamp> timestamps = new ArrayList<>();
		
		for (LocalDateTime localDateTime : x) {
			timestamps.add(localDateTime != null ? java.sql.Timestamp.valueOf(localDateTime) : null);
		}
		return setCollection(name, PARAM_TYPE_LIST_LOCAL_DATE_TIME, timestamps);
	}
	
	public JdbcStatement setLong(String name, Long x) {
		return setObject(name, PARAM_TYPE_LONG, x);
	}
	
	public JdbcStatement setLong(String name, long x) {
		LongParam param = (LongParam)params.get(name);
		
		if (param == null) {
			param = new LongParam(x);
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
			case FieldColumnMapping.JDBC_TYPE_LOCAL_DATE: setLocalDate(name, (LocalDate)x); break;
			case FieldColumnMapping.JDBC_TYPE_LOCAL_DATE_TIME: setLocalDateTime(name, (LocalDateTime)x); break;
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

	public JdbcStatement setParameterList(String name, FieldColumnMapping fieldColumnMapping, Collection<?> x) {
		return setParameterList(name, fieldColumnMapping.getJdbcType(), x);
	}
	
	public JdbcStatement setParameterList(String name, int jdbcType, Collection<?> x) {
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
			case FieldColumnMapping.JDBC_TYPE_LOCAL_DATE: paramType = PARAM_TYPE_LIST_LOCAL_DATE; break;
			case FieldColumnMapping.JDBC_TYPE_LOCAL_DATE_TIME: paramType = PARAM_TYPE_LIST_LOCAL_DATE_TIME; break;
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
	 * Will create a java.sql.Timestamp object if <b>x</b> is not an instance of java.sql.Timestamp
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setTimestamp(String name, Date x) {
		if (x != null && !(x instanceof java.sql.Timestamp)) {
			x = new java.sql.Timestamp(x.getTime());
		}
		return setObject(name, PARAM_TYPE_TIMESTAMP, x);
	}
	
	/**
	 * Will create an ArrayList of java.sql.Timestamp objects, converting any java.util.Date objects in <b>x</b>.
	 * @param name
	 * @param x
	 * @return
	 */
	public JdbcStatement setTimestamps(String name, Collection<? extends Date> x) {
		List<java.sql.Timestamp> timestamps = new ArrayList<>();
		
		for (Date date : x) {
			timestamps.add(date != null && !(date instanceof java.sql.Timestamp) 
					? new java.sql.Timestamp(date.getTime()) : null);
		}
		return setCollection(name, PARAM_TYPE_LIST_TIMESTAMP, timestamps);
	}
	
	public JdbcStatement sqlStatement(SqlStatement<?> sqlStatement) {
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
		if (param.isCollection()) {
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
		} else {
			builder.append("?");
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
		
		setSql(jdbcMapper.getVendor().print(sqlStatement));
		logger.debug("JdbcStatement sql: {}", sql);
	}
	
	private JdbcStatement setCollection(String name, int paramType, Collection<?> x) {
		if (x == null || x.isEmpty()) {
			throw new IllegalArgumentException("collection of param '" + name + "' cannot be null or empty");
		}
		
		CollectionParam param = (CollectionParam)params.get(name);
		
		if (param == null) {
			param = new CollectionParam(paramType, x);
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
			param = new JdbcParam(paramType, x);
			params.put(name, param);
		} else {
			param.setValue(x);
		}
		return this;
	}
	
	private List<Map<String, Object>> toListOfMaps(ResultSet rs) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columnCount = rsmd.getColumnCount();
		
		List<Map<String, Object>> list = new ArrayList<>();
		while (rs.next()) {
			Map<String, Object> map = new LinkedHashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				map.put(rsmd.getColumnLabel(i), rs.getObject(i));
			}
			list.add(map);
		}
		return list;
	}
	
	private List<Object[]> toResultList(ResultSet rs) throws SQLException {
		final int columnCount = rs.getMetaData().getColumnCount();
		
		List<Object[]> resultList = new ArrayList<>();
		while (rs.next()) {
			Object[] row = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				row[i] = rs.getObject(i + 1);
			}
			resultList.add(row);
		}
		return resultList;
	}
	
	/* END Private methods */
	
	/* BEGIN Inner classes */
	
	private class BooleanParam extends JdbcParam {
		private boolean booleanValue;
		
		public boolean getBooleanValue() { return booleanValue; }
		public void setBooleanValue(boolean booleanValue) { this.booleanValue = booleanValue; }

		public BooleanParam(boolean booleanValue) {
			super(PARAM_TYPE_PRIMITIVE_BOOLEAN);
			this.booleanValue = booleanValue;
		}
	}
	
	private class CollectionParam extends JdbcParam {
		@SuppressWarnings("rawtypes")
		private Collection collection;
		
		@SuppressWarnings("unchecked")
		public <T> Collection<T> getCollection() { return collection; }
		public void setCollection(Collection<?> collection) { this.collection = collection; }
		
		public CollectionParam(int paramType, Collection<?> collection) {
			super(paramType);
			super.isCollection = true;
			this.collection = collection;
		}
	}
	
	private class DoubleParam extends JdbcParam {
		private double doubleValue;
		
		public double getDoubleValue() { return doubleValue; }
		public void setDoubleValue(double doubleValue) { this.doubleValue = doubleValue; }
		
		public DoubleParam(double doubleValue) {
			super(PARAM_TYPE_PRIMITIVE_DOUBLE);
			this.doubleValue = doubleValue;
		}
	}
	
	private class FloatParam extends JdbcParam {
		private float floatValue;
		
		public float getFloatValue() { return floatValue; }
		public void setFloatValue(float floatValue) { this.floatValue = floatValue; }
		
		public FloatParam(float floatValue) {
			super(PARAM_TYPE_PRIMITIVE_FLOAT);
			this.floatValue = floatValue;
		}
	}
	
	private class IntParam extends JdbcParam {
		private int intValue;
		
		public int getIntValue() { return intValue; }
		public void setIntValue(int intValue) { this.intValue = intValue; }
		
		public IntParam(int intValue) {
			super(PARAM_TYPE_PRIMITIVE_INT);
			this.intValue = intValue;
		}
	}

	private class JdbcParam {
		protected boolean isCollection;
		private final int paramType;
		private Object value;
		
		public boolean isCollection() { return isCollection; }
		public int getParamType() { return paramType; }
		public Object getValue() { return value; }
		public void setValue(Object value) { this.value = value; }
		
		public JdbcParam(int paramType, Object value) {
			this.paramType = paramType;
			this.value = value;
		}
		
		protected JdbcParam(int paramType) {
			this(paramType, null);
		}
	}
	
	private class LongParam extends JdbcParam {
		private long longValue;
		
		public long getLongValue() { return longValue; }
		public void setLongValue(long longValue) { this.longValue = longValue; }
		
		public LongParam(long longValue) {
			super(PARAM_TYPE_PRIMITIVE_LONG);
			this.longValue = longValue;
		}
	}
	
	private class ParamFinder implements NodeVisitor {
		private final Set<String> names = new LinkedHashSet<>();
		
		public Set<String> getNames() { return names; }
		
		@Override
		public boolean visit(@SuppressWarnings("rawtypes") Node node) {
			if (node.getType() != Node.TYPE_PARAM) {
				return true;
			}
			
			Param param = (Param)node;
			names.add(param.getName());
			return true;
		}
	}
	
	private class ParamIndex {
		private final JdbcParam param;
		private final int index;
		
		public JdbcParam getParam() { return param; }
		public int getIndex() { return index; }
		
		public ParamIndex(JdbcParam param, int index) {
			this.index = index;
			this.param = param;
		}
		
		public int getNextIndex() {
			if (param.isCollection()) {
				CollectionParam collectionParam = (CollectionParam)param;
				return index + collectionParam.getCollection().size();
			}
			return index + 1;
		}
	}

	/* END Inner classes */
}