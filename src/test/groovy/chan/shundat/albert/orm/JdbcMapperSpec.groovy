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
	private JdbcMapper mapper;
	
	def setupSpec() {
		H2.createTables()
	}
	
	def setup() {
		mapper = new JdbcMapper()
	}
	
	def 'Register entity class'() {
		when: 'Registering User class'
		mapper.register(User.class)
		
		then: 'ClassRowMapping object for User class is created'
		ClassRowMapping userMapping = mapper.getMappings().get(User.class)
		userMapping != null
	}
	
	def 'Execute JdbcStatement object representing INSERT statement and verifying entity was inserted'() {
		given: "Insert object for inserting into User (1, 'Albert')"
		mapper.register(User.class)
		Insert insert = new Insert().into('User').columns(
			new ColumnList().column('user_id').column('name'))
			.values(new ColumnValues().literal(1).literal('Albert'))
		
		when: 'creating query with Insert object and executing it'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.createQuery(insert).executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'getting User entity with ID 1'
		User user = null
		try {
			conn = H2.getConnection()
			user = mapper.get(conn, User.class, 1)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User named "Albert" is retrieved with ID of 1'
		user != null
		user.userId == 1
		user.name == 'Albert'
	}
	
	def 'Initialize new entity and save it into the database'() {
		given: 'new User with ID 2'
		mapper.register(User.class)
		User user = new User()
		user.userId = 2
		
		expect: 'User with ID 2 does not yet exist in the database'
		Connection conn = null
		User userInDb = null
		try {
			conn = H2.getConnection()
			userInDb = mapper.get(conn, User.class, 2)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		userInDb == null
		
		when: 'User is saved'
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User with ID 2 was saved'
		try {
			conn = H2.getConnection()
			userInDb = mapper.get(conn, User.class, 2)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		userInDb != null
		userInDb.userId == 2
	}
	
	def 'Delete an entity by passing it'() {
		given: 'User entity with ID 3'
		mapper.register(User.class)
		Connection conn = null
		User user = new User()
		user.userId = 3
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
			user = mapper.get(conn, User.class, 3)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		expect: 'User is not null'
		user != null
		
		when: 'deleting User entity'
		try {
			conn = H2.getConnection()
			mapper.delete(conn, user)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User with ID 3 does not exist'
		boolean exists = true
		try {
			conn = H2.getConnection()
			exists = mapper.get(conn, User.class, 3) != null
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		!exists
	}
	
	def 'Delete an entity by passing an ID instead of instance of entity'() {
		given: 'User entity with ID 4'
		mapper.register(User.class)
		Connection conn = null
		User user = new User()
		user.userId = 4
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
			user = mapper.get(conn, User.class, 4)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		expect: 'User is not null'
		user != null
		
		when: 'deleting an entity whose class is User with ID 4'
		try {
			conn = H2.getConnection()
			mapper.delete(conn, User.class, 4)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User with ID 4 does not exist'
		boolean exists = true
		try {
			conn = H2.getConnection()
			exists = mapper.get(conn, User.class, 4) != null
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		!exists
	}
	
	def 'Refresh object from database after modifying a field'() {
		given: 'User entity with ID 5 and name "Albert"'
		mapper.register(User.class)
		Connection conn = null
		User user = new User()
		user.userId = 5
		user.name = 'Albert'
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
			user = mapper.get(conn, User.class, 5)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		expect: "User's name is \"Albert\""
		user.name == 'Albert'
		
		when: "name of User is changed to \"Javalbert\""
		String newName = 'Javalbert'
		user.name = newName
		
		and: 'then User is refreshed from database'
		try {
			conn = H2.getConnection()
			mapper.refresh(conn, user)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'User name is reverted back'
		user.name == 'Albert'
	}
	
	def 'Update object and verify version was incremented and fields changed'() {
		given: 'User entity with ID 6 and name "Albert"'
		mapper.register(User.class)
		Connection conn = null
		User user = new User()
		user.userId = 6
		user.name = 'Albert'
		try {
			conn = H2.getConnection()
			mapper.save(conn, user)
			user = mapper.get(conn, User.class, 6)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: "name of User is changed to \"Javalbert\""
		user.name = 'Javalbert'
		
		and: 'then User is updated'
		try {
			conn = H2.getConnection()
			mapper.update(conn, user)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'then updated User is retrieved from database'
		User updatedUser = null
		try {
			conn = H2.getConnection()
			updatedUser = mapper.get(conn, User.class, 6)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: "updated User's version is incremented to 1 and name was changed to \"Javalbert\""
		updatedUser.version == 1
		updatedUser.name == 'Javalbert'
	}
}