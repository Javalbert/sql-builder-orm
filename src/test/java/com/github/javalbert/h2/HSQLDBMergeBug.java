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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.github.javalbert.utils.DateUtils;
import com.github.javalbert.utils.jdbc.JdbcUtils;

public class HSQLDBMergeBug {
	public static final String SQL =
			"MERGE INTO ArchivedOrders arc"
			+ " USING (SELECT"
			+ " ord.order_id,"
			+ " cus.customer_id,"
			+ " st.store_key,"
			+ " cus.full_name AS customer_name,"
			+ " st.store_name,"
			+ " ord.sales_amount,"
			+ " ord.order_datetime"
			+ " FROM Customer cus INNER JOIN Orders ord ON cus.customer_id = ord.customer_id"
			+ " INNER JOIN Store st ON ord.store_id = st.store_key"
			+ " WHERE cus.full_name = ?"
			+ ") AS ord ON arc.order_id = ord.order_id"
			+ " AND arc.customer_id = ord.customer_id"
			+ " AND arc.store_key = ord.store_key"
			+ " WHEN MATCHED THEN DELETE";
	
	public static void main(String[] args) {
		try {
			HSQLDB.createTables();
			HSQLDB.deleteRecords();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		/* given: '4 Orders' */
		
		Connection conn = null;
		try {
			conn = HSQLDB.getConnection();
			executeSql(conn, "INSERT INTO Customer (full_name) VALUES (?)", stmt -> {
				stmt.setString(1, "Albert Chan");
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO Store (store_name) VALUES (?)", stmt -> {
				stmt.setString(1, "Dental");
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO Orders (customer_id, store_id, sales_amount, order_datetime) VALUES (?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setLong(2, 1L);
				stmt.setBigDecimal(3, new BigDecimal("80.0"));
				stmt.setTimestamp(4, new Timestamp(DateUtils.newDate(2017, 3, 3, 16, 20).getTime()));
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO Orders (customer_id, store_id, sales_amount, order_datetime) VALUES (?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setLong(2, 1L);
				stmt.setBigDecimal(3, new BigDecimal("90.0"));
				stmt.setTimestamp(4, new Timestamp(DateUtils.newDate(2017, 3, 3, 16, 20).getTime()));
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO Orders (customer_id, store_id, sales_amount, order_datetime) VALUES (?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setLong(2, 1L);
				stmt.setBigDecimal(3, new BigDecimal("1655.0"));
				stmt.setTimestamp(4, new Timestamp(DateUtils.newDate(2017, 3, 17, 9, 20).getTime()));
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO Orders (customer_id, store_id, sales_amount, order_datetime) VALUES (?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setLong(2, 1L);
				stmt.setBigDecimal(3, new BigDecimal("5.59"));
				stmt.setTimestamp(4, new Timestamp(DateUtils.newDate(2017, 3, 24, 9, 30).getTime()));
				stmt.executeUpdate();
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeQuietly(conn);
		}
		
		/* and: '4 ArchivedOrders (4 of 4 Orders) in the database' */
		
		try {
			conn = HSQLDB.getConnection();
			executeSql(conn, "INSERT INTO ArchivedOrders (customer_id, customer_name, order_datetime, order_id, sales_amount, store_key, store_name) VALUES (?, ?, ?, ?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setString(2, null);
				stmt.setString(3, null);
				stmt.setLong(4, 1L);
				stmt.setBigDecimal(5, null);
				stmt.setLong(6, 1L);
				stmt.setString(7, null);
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO ArchivedOrders (customer_id, customer_name, order_datetime, order_id, sales_amount, store_key, store_name) VALUES (?, ?, ?, ?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setString(2, null);
				stmt.setString(3, null);
				stmt.setLong(4, 2L);
				stmt.setBigDecimal(5, null);
				stmt.setLong(6, 1L);
				stmt.setString(7, null);
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO ArchivedOrders (customer_id, customer_name, order_datetime, order_id, sales_amount, store_key, store_name) VALUES (?, ?, ?, ?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setString(2, null);
				stmt.setString(3, null);
				stmt.setLong(4, 3L);
				stmt.setBigDecimal(5, null);
				stmt.setLong(6, 1L);
				stmt.setString(7, null);
				stmt.executeUpdate();
			});
			executeSql(conn, "INSERT INTO ArchivedOrders (customer_id, customer_name, order_datetime, order_id, sales_amount, store_key, store_name) VALUES (?, ?, ?, ?, ?, ?, ?)", stmt -> {
				stmt.setLong(1, 1L);
				stmt.setString(2, null);
				stmt.setString(3, null);
				stmt.setLong(4, 4L);
				stmt.setBigDecimal(5, null);
				stmt.setLong(6, 1L);
				stmt.setString(7, null);
				stmt.executeUpdate();
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeQuietly(conn);
		}
		
		/* when: 'the MERGE statement is executed' */
		
		try {
			conn = HSQLDB.getConnection();
			executeSql(conn, SQL, stmt -> {
				stmt.setString(1, "Albert Chan");
				int rowCount = stmt.executeUpdate();
				System.out.println("rowCount == 4: " + (rowCount == 4));
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeQuietly(conn);
		}
	}
	
	public static void executeSql(
			Connection conn,
			String sql,
			StatementHandler handler) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			handler.handle(stmt);
		} finally {
			JdbcUtils.closeQuietly(stmt);
		}
	}
	
	@FunctionalInterface
	public static interface StatementHandler {
		void handle(PreparedStatement stmt) throws SQLException;
	}
}
