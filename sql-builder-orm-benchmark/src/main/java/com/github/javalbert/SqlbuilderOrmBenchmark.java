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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.github.javalbert.orm.JdbcMapper;

public class SqlbuilderOrmBenchmark {
	@State(Scope.Thread)
	public static class RetrievalState {
		public Connection connection;
		public DataTypeHolder row = new DataTypeHolder();
		public JdbcMapper jdbcMapper = new JdbcMapper();
		
		@Setup(Level.Trial)
		public void doSetup() {
			try {
				row.setIntVal(Integer.MAX_VALUE);
				row.setBooleanVal(true);
				row.setBigintVal(Long.MAX_VALUE);
				row.setDecimalVal(BigDecimal.TEN);
				row.setDoubleVal(Double.MAX_VALUE);
				row.setRealVal(Float.MAX_VALUE);
				row.setDateVal(Date.valueOf(LocalDate.of(2017, 3, 5)));
				row.setDateVal(Timestamp.valueOf(LocalDateTime.of(2017, 3, 5, 20, 45)));
				row.setVarcharVal("Wing Street");
				
				H2DB.createTables();
				H2DB.deleteRecords();
				connection = H2DB.getConnection();
				
				jdbcMapper.register(DataTypeHolder.class);
				jdbcMapper.save(connection, row);
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
	
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
    @Benchmark
    public void testRetrieval(RetrievalState state) {
    	try {
			state.jdbcMapper.get(state.connection, DataTypeHolder.class, 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}
