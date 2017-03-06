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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

import com.github.javalbert.utils.jdbc.JdbcUtils;

public final class H2DB {
	public static void createTables() throws ClassNotFoundException, SQLException {
		executeStatements(
				"CREATE TABLE IF NOT EXISTS DataTypeHolder ("
				+ "id INT PRIMARY KEY,"
				+ "int_val INT NOT NULL,"
				+ "boolean_val BOOLEAN NOT NULL,"
				+ "bigint_val BIGINT NOT NULL,"
				+ "decimal_val DECIMAL(13, 2),"
				+ "double_val DOUBLE NOT NULL,"
				+ "real_val REAL NOT NULL,"
				+ "date_val DATE,"
				+ "timestamp_val TIMESTAMP,"
				+ "varchar_val VARCHAR(255)"
				+ ")");
	}
	
	public static void deleteRecords() {
		try {
			executeStatements(
					"DELETE FROM DataTypeHolder");
		} catch (Exception ignored) {}
	}
	
	public static void executeStatements(String...sqlStatements) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Deque<PreparedStatement> preparedStatements = new ArrayDeque<>();
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			for (String sqlStatement : sqlStatements) {
				PreparedStatement stmt = conn.prepareStatement(sqlStatement);
				preparedStatements.push(stmt);
				stmt.executeUpdate();
			}
			conn.commit();
		} finally {
			while (!preparedStatements.isEmpty()) {
				JdbcUtils.closeQuietly(preparedStatements.pop());
			}
			JdbcUtils.closeQuietly(conn);
		}
	}

	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
	}
	
	public static void dropTables() throws ClassNotFoundException, SQLException {
		executeStatements(
				"DROP TABLE IF EXISTS DataTypeHolder");
	}
	
	private H2DB() {}
}