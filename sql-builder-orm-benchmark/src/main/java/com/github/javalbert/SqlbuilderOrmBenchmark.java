/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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
package com.github.javalbert;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.sql2o.Sql2o;

import com.github.javalbert.hibernate.DataTypeHolderHibernate;
import com.github.javalbert.hibernate.HibernateUtils;
import com.github.javalbert.orm.JdbcMapper;
import com.github.javalbert.utils.jdbc.JdbcUtils;

public class SqlbuilderOrmBenchmark {
	@State(Scope.Thread)
	public static class RetrievalHibernateStatelessSessionState {
		public int id;
		public StatelessSession session;
		public SessionFactory sessionFactory;
		
		@Setup(Level.Trial)
		public void doSetup() {
			DataTypeHolderHibernate row = new DataTypeHolderHibernate();
			row.setIntVal(Integer.MAX_VALUE);
			row.setBooleanVal(true);
			row.setBigintVal(Long.MAX_VALUE);
			row.setDecimalVal(BigDecimal.TEN);
			row.setDoubleVal(Double.MAX_VALUE);
			row.setRealVal(Float.MAX_VALUE);
			row.setDateVal(Date.valueOf(LocalDate.of(2017, 3, 5)));
			row.setTimestampVal(Timestamp.valueOf(LocalDateTime.of(2017, 3, 5, 20, 45)));
			row.setVarcharVal("Wing Street");
			
			sessionFactory = HibernateUtils.createSessionFactory();
			
			try {
				H2.createTables();
				
				session = sessionFactory.openStatelessSession(H2.getConnection());
				session.beginTransaction();
				id = (int)session.insert(row);
				session.getTransaction().commit();
				session.close();
				
				session = sessionFactory.openStatelessSession(H2.getConnection());
				session.beginTransaction();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		
		@TearDown(Level.Trial)
        public void doTearDown() {
			session.getTransaction().commit();
			session.close();
			sessionFactory.close();
        }
	}
	
	@State(Scope.Thread)
	public static class RetrievalJdbcState {
		public Connection connection;
		public int id;
		
		@Setup(Level.Trial)
		public void doSetup() {
			PreparedStatement stmt = null;
			ResultSet generatedKeys = null;
			try {
				H2.createTables();
				
				connection = H2.getConnection();
				
				stmt = connection.prepareStatement(
						"INSERT INTO DataTypeHolder ("
						+ "int_val,"
						+ "boolean_val,"
						+ "bigint_val,"
						+ "decimal_val,"
						+ "double_val,"
						+ "real_val,"
						+ "date_val,"
						+ "timestamp_val,"
						+ "varchar_val) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						PreparedStatement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, Integer.MAX_VALUE);
				stmt.setBoolean(2, true);
				stmt.setLong(3, Long.MAX_VALUE);
				stmt.setBigDecimal(4, BigDecimal.TEN);
				stmt.setDouble(5, Double.MAX_VALUE);
				stmt.setFloat(6, Float.MAX_VALUE);
				stmt.setDate(7, Date.valueOf(LocalDate.of(2017, 3, 5)));
				stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.of(2017, 3, 5, 20, 45)));
				stmt.setString(9, "Wing Street");
				stmt.executeUpdate();
				
				generatedKeys = stmt.getGeneratedKeys();
				generatedKeys.next();
				id = generatedKeys.getInt(1);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			} finally {
				JdbcUtils.closeQuietly(generatedKeys);
				JdbcUtils.closeQuietly(stmt);
			}
		}
		
		@TearDown(Level.Trial)
        public void doTearDown() {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }
	}
	
	@State(Scope.Thread)
	public static class RetrievalSql2oState {
		private org.sql2o.Connection connection;
		private long id;
		private Sql2o sql2o;

		@Setup(Level.Trial)
		public void doSetup() {
			try {
				H2.createTables();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			
			sql2o = new Sql2o("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
			
			Map<String, String> colMaps = new HashMap<>();
			colMaps.put("int_val", "intVal");
			colMaps.put("boolean_val", "booleanVal");
			colMaps.put("bigint_val", "bigintVal");
			colMaps.put("decimal_val", "decimalVal");
			colMaps.put("double_val", "doubleVal");
			colMaps.put("real_val", "realVal");
			colMaps.put("date_val", "dateVal");
			colMaps.put("timestamp_val", "timestampVal");
			colMaps.put("varchar_val", "varcharVal");
			sql2o.setDefaultColumnMappings(colMaps);
			
			connection = sql2o.open();
			
			id = (long)connection.createQuery(
					"INSERT INTO DataTypeHolder ("
					+ "int_val,"
					+ "boolean_val,"
					+ "bigint_val,"
					+ "decimal_val,"
					+ "double_val,"
					+ "real_val,"
					+ "date_val,"
					+ "timestamp_val,"
					+ "varchar_val) "
					+ "VALUES (:intVal, :booleanVal, :bigintVal, :decimalVal, :doubleVal, :realVal, :dateVal, :timestampVal, :varcharVal)",
					true)
			.addParameter("intVal", Integer.MAX_VALUE)
			.addParameter("booleanVal", true)
			.addParameter("bigintVal", Long.MAX_VALUE)
			.addParameter("decimalVal", BigDecimal.TEN)
			.addParameter("doubleVal", Double.MAX_VALUE)
			.addParameter("realVal", Float.MAX_VALUE)
			.addParameter("dateVal", Date.valueOf(LocalDate.of(2017, 3, 5)))
			.addParameter("timestampVal", Timestamp.valueOf(LocalDateTime.of(2017, 3, 5, 20, 45)))
			.addParameter("varcharVal", "Wing Street")
			.executeUpdate()
			.getKey();
		}
		
		@TearDown(Level.Trial)
        public void doTearDown() {
			connection.close();
		}
	}
	
	@State(Scope.Thread)
	public static class RetrievalSqlbuilderOrmState {
		public Connection connection;
		public int id;
		public JdbcMapper jdbcMapper = new JdbcMapper();
		
		@Setup(Level.Trial)
		public void doSetup() {
			DataTypeHolder row = new DataTypeHolder();
			row.setIntVal(Integer.MAX_VALUE);
			row.setBooleanVal(true);
			row.setBigintVal(Long.MAX_VALUE);
			row.setDecimalVal(BigDecimal.TEN);
			row.setDoubleVal(Double.MAX_VALUE);
			row.setRealVal(Float.MAX_VALUE);
			row.setDateVal(Date.valueOf(LocalDate.of(2017, 3, 5)));
			row.setTimestampVal(Timestamp.valueOf(LocalDateTime.of(2017, 3, 5, 20, 45)));
			row.setVarcharVal("Wing Street");
			
			try {
				H2.createTables();
				
				connection = H2.getConnection();
				
				jdbcMapper.register(DataTypeHolder.class);
				jdbcMapper.save(connection, row);
				id = row.getId();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		
		@TearDown(Level.Trial)
        public void doTearDown() {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }
	}
	
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(SqlbuilderOrmBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(3)
				.build();

		new Runner(opt).run();
		
//		new NonJMHBenchmark().run();
	}
	
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Benchmark
	public DataTypeHolderHibernate testRetrievalHibernateStatelessSession(RetrievalHibernateStatelessSessionState state) {
		return (DataTypeHolderHibernate)state.session.get(DataTypeHolderHibernate.class, state.id);
	}
	
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Benchmark
	public DataTypeHolder testRetrievalJdbc(RetrievalJdbcState state) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = state.connection.prepareStatement(
					"SELECT"
					+ " id,"
					+ " int_val,"
					+ " boolean_val,"
					+ " bigint_val,"
					+ " decimal_val,"
					+ " double_val,"
					+ " real_val,"
					+ " date_val,"
					+ " timestamp_val,"
					+ " varchar_val"
					+ " FROM DataTypeHolder"
					+ " WHERE id = ?");
			stmt.setInt(1, state.id);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return null;
			}
			
			DataTypeHolder row = new DataTypeHolder();
			row.setId(rs.getInt(1));
			row.setIntVal(rs.getInt(2));
			row.setBooleanVal(rs.getBoolean(3));
			row.setBigintVal(rs.getLong(4));
			row.setDecimalVal(rs.getBigDecimal(5));
			row.setDoubleVal(rs.getDouble(6));
			row.setRealVal(rs.getFloat(7));
			row.setDateVal(rs.getDate(8));
			row.setTimestampVal(rs.getTimestamp(9));
			row.setVarcharVal(rs.getString(10));
			return row;
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(stmt);
		}
	}
	
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Benchmark
	public DataTypeHolder testRetrievalSql2o(RetrievalSql2oState state) {
		return state.connection.createQuery(
				"SELECT"
				+ " id,"
				+ " int_val,"
				+ " boolean_val,"
				+ " bigint_val,"
				+ " decimal_val,"
				+ " double_val,"
				+ " real_val,"
				+ " date_val,"
				+ " timestamp_val,"
				+ " varchar_val"
				+ " FROM DataTypeHolder"
				+ " WHERE id = :id")
				.addParameter("id", state.id)
				.executeAndFetchFirst(DataTypeHolder.class);
	}
	
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	@Benchmark
	public DataTypeHolder testRetrievalSqlbuilderOrm(RetrievalSqlbuilderOrmState state) throws SQLException {
		return state.jdbcMapper.get(state.connection, DataTypeHolder.class, state.id);
	}
}
