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
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalSql2oState;
import com.github.javalbert.SqlbuilderOrmBenchmark.RetrievalSqlbuilderOrmState;
import com.github.javalbert.hibernate.DataTypeHolderHibernate;

public class NonJMHBenchmark {
	private long hibernate;
	private long jdbc;
	private long sql2o;
	private long sqlborm;
	
	public void run() {
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
		
		for (int i = 0; i < 1000; i++) {
			testHibernate();
			testJdbc();
			testSql2o();
			testSqlborm();
		}
		
		hibernate = 0L;
		jdbc = 0L;
		sql2o = 0L;
		sqlborm = 0L;
		
		try {
			System.out.println("Ready");
			Thread.sleep(1000L);
			System.out.println("Set");
			Thread.sleep(1000L);
			System.out.println("Go!!!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < 1000; i++) {
			testHibernate();
			testJdbc();
			testSql2o();
			testSqlborm();
		}
		
		System.out.println("Hibernate: " + (hibernate / 1000000.0));
		System.out.println("JDBC: " + (jdbc / 1000000.0));
		System.out.println("Sql2o: " + (sql2o / 1000000.0));
		System.out.println("SqlbORM: " + (sqlborm / 1000000.0));
	}
	
	private DataTypeHolderHibernate testHibernate() {
		RetrievalHibernateStatelessSessionState hibernateState = new RetrievalHibernateStatelessSessionState();
		hibernateState.doSetup();
		long start = System.nanoTime();
		DataTypeHolderHibernate holder = new SqlbuilderOrmBenchmark().testRetrievalHibernateStatelessSession(hibernateState);
		hibernate += System.nanoTime() - start;
		hibernateState.doTearDown();
		return holder;
	}
	
	private DataTypeHolder testJdbc() {
		try {
			RetrievalJdbcState jdbcState = new RetrievalJdbcState();
			jdbcState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = new SqlbuilderOrmBenchmark().testRetrievalJdbc(jdbcState);
			jdbc += System.nanoTime() - start;
			jdbcState.doTearDown();
			return holder;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private DataTypeHolder testSql2o() {
		RetrievalSql2oState sql2oState = new RetrievalSql2oState();
		sql2oState.doSetup();
		long start = System.nanoTime();
		DataTypeHolder holder = new SqlbuilderOrmBenchmark().testRetrievalSql2o(sql2oState);
		sql2o += System.nanoTime() - start;
		sql2oState.doTearDown();
		return holder;
	}
	
	private DataTypeHolder testSqlborm() {
		try {
			RetrievalSqlbuilderOrmState sqlbormState = new RetrievalSqlbuilderOrmState();
			sqlbormState.doSetup();
			long start = System.nanoTime();
			DataTypeHolder holder = new SqlbuilderOrmBenchmark().testRetrievalSqlbuilderOrm(sqlbormState);
			sqlborm += System.nanoTime() - start;
			sqlbormState.doTearDown();
			return holder;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
