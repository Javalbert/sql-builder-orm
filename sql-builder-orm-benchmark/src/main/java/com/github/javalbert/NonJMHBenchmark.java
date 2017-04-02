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

import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalHibernateStatelessSessionState;
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalJdbcState;
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalJooqState;
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalSql2oState;
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalSqlbuilderOrmState;
import com.github.javalbert.hibernate.DataTypeHolderHibernate;

public class NonJMHBenchmark {
	public long hibernateTime;
	public long jdbcTime;
	public long jooqTime;
	public long sql2oTime;
	public long sqlbOrmTime;
	
	private SqlbuilderOrmBenchmark benchmark = new SqlbuilderOrmBenchmark();
	
	public void run() {
		// Shut down Hibernate logging
		// CREDIT: http://stackoverflow.com/a/18323888
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);

		System.out.println("Warmup started");
		
		for (int i = 0; i < 1000; i++) {
			testHibernate();
			testJdbc();
			testJooq();
			testSql2o();
			testSqlbOrm();
		}
		
		hibernateTime = 0L;
		jooqTime = 0L;
		jdbcTime = 0L;
		sql2oTime = 0L;
		sqlbOrmTime = 0L;
		
		try {
			Thread.sleep(1000L);
			System.out.println("Warmup ended");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < 1000; i++) {
			testHibernate();
			testJdbc();
			testJooq();
			testSql2o();
			testSqlbOrm();
		}
		
		print("Hibernate", hibernateTime);
		print("JDBC", jdbcTime);
		print("jOOQ", jooqTime);
		print("Sql2o", sql2oTime);
		print("SqlbORM", sqlbOrmTime);
	}
	
	public DataTypeHolderHibernate testHibernate() {
		RetrievalHibernateStatelessSessionState hibernateState = new RetrievalHibernateStatelessSessionState();
		hibernateState.doSetup();
		long start = System.nanoTime();
		DataTypeHolderHibernate holder = benchmark.testRetrievalHibernateStatelessSession(hibernateState);
		hibernateTime += System.nanoTime() - start;
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
	
	public DataTypeHolder testSqlbOrm() {
		try {
			RetrievalSqlbuilderOrmState sqlbOrmState = new RetrievalSqlbuilderOrmState();
			sqlbOrmState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = benchmark.testRetrievalSqlbuilderOrm(sqlbOrmState);
			sqlbOrmTime += System.nanoTime() - start;
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
