package chan.shundat.albert.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

import chan.shundat.albert.utils.jdbc.JdbcUtils;

public final class H2 {
	public static void createTables() throws ClassNotFoundException, SQLException {
		executeStatements(
				"CREATE TABLE IF NOT EXISTS User ("
				+ "user_id INT PRIMARY KEY,"
				+ "name VARCHAR(20),"
				+ "version INT DEFAULT 0"
				+ ")",
				"CREATE TABLE IF NOT EXISTS User2 ("
				+ "user_id INT AUTO_INCREMENT PRIMARY KEY,"
				+ "name VARCHAR(20),"
				+ "version INT DEFAULT 0"
				+ ")");
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
				"DROP TABLE IF EXISTS User",
				"DROP TABLE IF EXISTS User2");
	}
	
	private H2() {}
}