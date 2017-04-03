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

import java.sql.SQLException;

import com.github.javalbert.EntityByIdBenchmark.RetrievalHibernateStatelessSessionState;
import com.github.javalbert.EntityByIdBenchmark.RetrievalJdbcState;
import com.github.javalbert.EntityByIdBenchmark.RetrievalJooqState;
import com.github.javalbert.EntityByIdBenchmark.RetrievalSql2oState;
import com.github.javalbert.EntityByIdBenchmark.RetrievalSqlbuilderOrmState;
import com.github.javalbert.hibernate.DataTypeHolderHibernate;

public class EntityByIdNonJMH {
	public long hibernateGetByIdTime;
	public long hibernateQueryByIdTime;
	public long jdbcTime;
	public long jooqTime;
	public long sql2oTime;
	public long sqlbOrmGetByIdTime;
	public long sqlbOrmQueryByIdTime;
	
	private EntityByIdBenchmark benchmark = new EntityByIdBenchmark();
	
	public void run() {
		// Shut down Hibernate logging
		// CREDIT: http://stackoverflow.com/a/18323888
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);

		System.out.println("Warmup started");
		
		for (int i = 0; i < 1000; i++) {
			testHibernateGetById();
			testHibernateQueryById();
			testJdbc();
			testJooq();
			testSql2o();
			testSqlbOrmGetById();
			testSqlbOrmQueryById();
		}
		
		hibernateGetByIdTime = 0L;
		hibernateQueryByIdTime = 0L;
		jooqTime = 0L;
		jdbcTime = 0L;
		sql2oTime = 0L;
		sqlbOrmGetByIdTime = 0L;
		sqlbOrmQueryByIdTime = 0L;
		
		try {
			Thread.sleep(1000L);
			System.out.println("Warmup ended");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < 1000; i++) {
			testHibernateGetById();
			testHibernateQueryById();
			testJdbc();
			testJooq();
			testSql2o();
			testSqlbOrmGetById();
			testSqlbOrmQueryById();
		}

		print("Hibernate (get by ID)", hibernateGetByIdTime);
		print("Hibernate (query by ID)", hibernateQueryByIdTime);
		print("JDBC", jdbcTime);
		print("jOOQ", jooqTime);
		print("Sql2o", sql2oTime);
		print("SqlbORM (get by ID)", sqlbOrmGetByIdTime);
		print("SqlbORM (query by ID)", sqlbOrmQueryByIdTime);
	}
	
	public DataTypeHolderHibernate testHibernateGetById() {
		RetrievalHibernateStatelessSessionState hibernateState = new RetrievalHibernateStatelessSessionState();
		hibernateState.doSetup();
		long start = System.nanoTime();
		DataTypeHolderHibernate holder = benchmark.testRetrievalHibernateGetById(hibernateState);
		hibernateGetByIdTime += System.nanoTime() - start;
		hibernateState.doTearDown();
		return holder;
	}
	
	public DataTypeHolderHibernate testHibernateQueryById() {
		RetrievalHibernateStatelessSessionState hibernateState = new RetrievalHibernateStatelessSessionState();
		hibernateState.doSetup();
		long start = System.nanoTime();
		DataTypeHolderHibernate holder = benchmark.testRetrievalHibernateQueryById(hibernateState);
		hibernateQueryByIdTime += System.nanoTime() - start;
		hibernateState.doTearDown();
		return holder;
	}
	
	public DataTypeHolder testJdbc() {
		try {
			RetrievalJdbcState jdbcState = new RetrievalJdbcState();
			jdbcState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = benchmark.testRetrievalJdbc(jdbcState);
			jdbcTime += System.nanoTime() - start;
			jdbcState.doTearDown();
			return holder;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DataTypeHolderHibernate testJooq() {
		RetrievalJooqState jooqState = new RetrievalJooqState();
		jooqState.doSetup();
		long start = System.nanoTime();
		DataTypeHolderHibernate holder = benchmark.testRetrievalJooq(jooqState);
		jooqTime += System.nanoTime() - start;
		jooqState.doTearDown();
		return holder;
	}
	
	public DataTypeHolder testSql2o() {
		RetrievalSql2oState sql2oState = new RetrievalSql2oState();
		sql2oState.doSetup();
		long start = System.nanoTime();
		DataTypeHolder holder = benchmark.testRetrievalSql2o(sql2oState);
		sql2oTime += System.nanoTime() - start;
		sql2oState.doTearDown();
		return holder;
	}
	
	public DataTypeHolder testSqlbOrmGetById() {
		try {
			RetrievalSqlbuilderOrmState sqlbOrmState = new RetrievalSqlbuilderOrmState();
			sqlbOrmState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = benchmark.testRetrievalSqlbOrmGetById(sqlbOrmState);
			sqlbOrmGetByIdTime += System.nanoTime() - start;
			sqlbOrmState.doTearDown();
			return holder;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DataTypeHolder testSqlbOrmQueryById() {
		try {
			RetrievalSqlbuilderOrmState sqlbOrmState = new RetrievalSqlbuilderOrmState();
			sqlbOrmState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = benchmark.testRetrievalSqlbOrmQueryById(sqlbOrmState);
			sqlbOrmQueryByIdTime += System.nanoTime() - start;
			sqlbOrmState.doTearDown();
			return holder;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void print(String library, long time) {
		System.out.println(library + ": " + String.format("%.2f", time / 1000000.0));
	}
}
