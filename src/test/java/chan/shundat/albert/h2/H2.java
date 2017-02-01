package chan.shundat.albert.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import chan.shundat.albert.utils.jdbc.JdbcUtils;

public final class H2 {
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
	}
	
	public static void createTables() throws ClassNotFoundException, SQLException {
		dropTables();
		
		Connection conn = null;
		PreparedStatement createTableUser = null;
		try {
			conn = getConnection();
			createTableUser = conn.prepareStatement(
					"CREATE TABLE User ("
					+ "user_id INT PRIMARY KEY,"
					+ "name VARCHAR(20),"
					+ "version INT DEFAULT 0"
					+ ")");
			createTableUser.executeUpdate();
		} finally {
			JdbcUtils.closeQuietly(createTableUser);
			JdbcUtils.closeQuietly(conn);
		}
	}
	
	public static void dropTables() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		PreparedStatement dropTableUser = null;
		try {
			conn = getConnection();
			dropTableUser = conn.prepareStatement("DROP TABLE IF EXISTS User");
			dropTableUser.executeUpdate();
		} finally {
			JdbcUtils.closeQuietly(dropTableUser);
			JdbcUtils.closeQuietly(conn);
		}
	}
	
	private H2() {}
}