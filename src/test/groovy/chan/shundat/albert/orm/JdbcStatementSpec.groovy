package chan.shundat.albert.orm

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

import chan.shundat.albert.domain.User
import chan.shundat.albert.domain.User2
import chan.shundat.albert.h2.H2
import chan.shundat.albert.sqlbuilder.Predicate
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.Where
import chan.shundat.albert.utils.jdbc.JdbcUtils
import spock.lang.Specification

class JdbcStatementSpec extends Specification {
	private JdbcMapper mapper
	
	def setupSpec() {
		H2.createTables()
	}
	
	def setup() {
		mapper = new JdbcMapper()
	}
	
	def 'Cannot initialize JdbcStatement without a JdbcMapper'() {
		when: 'instantiating a JdbcStatement with null JdbcMapper'
		new JdbcStatement(null)
		
		then: 'NPE is thrown'
		thrown(NullPointerException)
	}
	
	def 'Create PreparedStatement instance with a parameter already set'() {
		given: 'User variable called "user" assigned a new instance and saved into database'
		mapper.register(User.class)
		Connection conn = null
		User user = new User(1, 'Albert')
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'PreparedStatement is returned, ResultSet is processed, and User variable named "user2" is created manually'
		PreparedStatement stmt = null
		ResultSet rs = null
		User user2 = null
		try {
			conn = H2.getConnection()
			JdbcStatement jdbcStmt = mapper.createQuery(mapper.selectById(User.class))
			jdbcStmt.setInteger('user_id', 1) // Use database column name as param name
			
			stmt = jdbcStmt.createPreparedStatement(conn)
			
			rs = stmt.executeQuery()
			if (rs.next()) {
				user2 = new User()
				user2.userId = rs.getInt(1)
				user2.name = rs.getString(2)
				user2.version = rs.getInt(3)
			}
		} finally {
			JdbcUtils.closeQuietly(rs)
			JdbcUtils.closeQuietly(stmt)
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'user is equal to user2'
		user2.equals(user)
	}
	
	def 'Create new query, set parameters, set Select object, then return single result'() {
		given: 'User entity saved into the database'
		mapper.register(User.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new User(2, 'Javalbert'))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Select object to fetch User with name "Javalbert"'
		Select getUserByName = mapper.selectFrom(User.class)
			.where(new Where().predicate(new Predicate().column('name').eq().param('name'))
			)
		
		when: 'executing a new query with Select statement and parameters "Javalbert" for name, and 1 for version (ignored)'
		User user = null
		try {
			conn = H2.getConnection()
			JdbcStatement stmt = mapper.createQuery()
			user = stmt.setString('name', 'Javalbert')
				.sqlStatement(getUserByName) // Doesn't matter when to call sqlStatement()
				.setInteger('version', 1)
				.uniqueResult(conn, User.class)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User entity with ID 2, name "Javalbert", and version 0 is retrieved'
		user.userId == 2
		user.name == 'Javalbert'
		user.version == 0
	}
	
	
	
	def 'asdf'() {
		given: ''
//		mapper.register(User2.class)
		
		when: ''
		
		
		then: ''
		
	}
}