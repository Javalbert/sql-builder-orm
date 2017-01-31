package chan.shundat.albert.orm

import java.sql.Connection
import java.sql.SQLException

import chan.shundat.albert.domain.User
import chan.shundat.albert.h2.H2
import chan.shundat.albert.sqlbuilder.ColumnList
import chan.shundat.albert.sqlbuilder.ColumnValues
import chan.shundat.albert.sqlbuilder.Insert
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.SelectList
import chan.shundat.albert.utils.jdbc.JdbcUtils
import spock.lang.Shared
import spock.lang.Specification

class JdbcMapperSpec extends Specification {
	@Shared
	private static JdbcMapper mapper;
	
	def setupSpec() {
		H2.createTables()
		mapper = new JdbcMapper()
	}
	
	def 'Register entity class'() {
		when: 'Registering User class'
		mapper.register(User.class)
		
		then: 'ClassRowMapping object for User class is created'
		ClassRowMapping userMapping = mapper.getMappings().get(User.class)
		userMapping != null
	}
	
	def 'Execute JdbcStatement object representing INSERT statement'() {
		given: "Insert object for inserting into User (DEFAULT, 'Albert')"
		Insert insert = new Insert().into('User').columns(
			new ColumnList().column('user_id').column('name'))
			.values(new ColumnValues().sqlDefault().literal('Albert'))
		
		when: 'creating query with Insert object and executing it'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.createQuery(insert).executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'no SQLException was thrown'
		notThrown(SQLException)
	}
	
	def 'Get object by class and id'() {
		given: 'an ID of 1'
		int id = 1
		
		when: 'retrieving User object with the ID'
		Connection conn = null
		User user = null
		try {
			conn = H2.getConnection()
			user = mapper.get(conn, User.class, id)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User object is retrieved with ID of 1'
		user != null
		user.userId == id
	}
}