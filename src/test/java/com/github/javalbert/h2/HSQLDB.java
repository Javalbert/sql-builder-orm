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
package com.github.javalbert.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

import com.github.javalbert.utils.jdbc.JdbcUtils;

/**
 * <a href="http://www.programmingforfuture.com/2011/02/in-process-mode-of-hsqldb-in-web.html">link</a>
 * @author Albert
 *
 */
public final class HSQLDB {
	public static void createTables() throws ClassNotFoundException, SQLException {
		executeStatements(
		/* https://blog.tallan.com/wp-content/uploads/2008/09/onlinesales.gif */
				"CREATE TABLE IF NOT EXISTS Customer ("
				+ "customer_id IDENTITY,"
				+ "full_name VARCHAR(50)"
				+ ")",
				"CREATE TABLE IF NOT EXISTS Store ("
				+ "store_key IDENTITY,"
				+ "store_name VARCHAR(50)"
				+ ")",
				"CREATE TABLE IF NOT EXISTS Orders ("
				+ "order_id IDENTITY,"
				+ "customer_id BIGINT NOT NULL,"
				+ "store_id BIGINT,"
				+ "sales_amount DECIMAL(13, 2),"
				+ "order_datetime TIMESTAMP,"
				+ "FOREIGN KEY (customer_id) REFERENCES Customer (customer_id),"
				+ "FOREIGN KEY (store_id) REFERENCES Store (store_key)"
				+ ")",
				"CREATE TABLE IF NOT EXISTS Product ("
				+ "product_id IDENTITY,"
				+ "order_id BIGINT NOT NULL,"
				+ "product_name VARCHAR(255),"
				+ "price DECIMAL(10, 2),"
				+ "FOREIGN KEY (order_id) REFERENCES Orders (order_id)"
				+ ")",
				"CREATE TABLE IF NOT EXISTS ArchivedOrders ("
				+ "archived_order_id IDENTITY,"
				+ "order_id BIGINT NOT NULL,"
				+ "customer_id BIGINT NOT NULL,"
				+ "store_key BIGINT NOT NULL,"
				+ "customer_name VARCHAR(50),"
				+ "store_name VARCHAR(50),"
				+ "sales_amount DECIMAL(13, 2),"
				+ "order_datetime TIMESTAMP"
				+ ")");
	}
	
	public static void deleteRecords() {
		try {
			executeStatements(
					"DELETE FROM Product",
					"ALTER TABLE Product ALTER COLUMN product_id RESTART WITH 1",
					"DELETE FROM Orders",
					"ALTER TABLE Orders ALTER COLUMN order_id RESTART WITH 1",
					"DELETE FROM Store",
					"ALTER TABLE Store ALTER COLUMN store_key RESTART WITH 1",
					"DELETE FROM Customer",
					"ALTER TABLE Customer ALTER COLUMN customer_id RESTART WITH 1",
					"DELETE FROM ArchivedOrders",
					"ALTER TABLE ArchivedOrders ALTER COLUMN archived_order_id RESTART WITH 1");
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
		Class.forName("org.hsqldb.jdbc.JDBCDriver");
		return DriverManager.getConnection("jdbc:hsqldb:mem:test", "SA", "");
	}
	
	public static void dropTables() throws ClassNotFoundException, SQLException {
		executeStatements(
				"DROP TABLE IF EXISTS Product",
				"DROP TABLE IF EXISTS Orders",
				"DROP TABLE IF EXISTS Store",
				"DROP TABLE IF EXISTS Customer",
				"DROP TABLE IF EXISTS ArchivedOrders");
	}
	
	private HSQLDB() {}
}