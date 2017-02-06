package chan.shundat.albert.orm

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

import chan.shundat.albert.domain.DataTypeHolder
import chan.shundat.albert.domain.User
import chan.shundat.albert.h2.H2
import chan.shundat.albert.sqlbuilder.ColumnList
import chan.shundat.albert.sqlbuilder.ColumnValues
import chan.shundat.albert.sqlbuilder.From
import chan.shundat.albert.sqlbuilder.Insert
import chan.shundat.albert.sqlbuilder.Predicate
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.SelectList
import chan.shundat.albert.sqlbuilder.SetValue
import chan.shundat.albert.sqlbuilder.SetValues
import chan.shundat.albert.sqlbuilder.Update
import chan.shundat.albert.sqlbuilder.Where
import chan.shundat.albert.utils.DateUtils
import chan.shundat.albert.utils.jdbc.JdbcUtils
import spock.lang.Specification
import spock.lang.Unroll

class JdbcStatementSpec extends Specification {
	private JdbcMapper mapper
	
	def setupSpec() {
		H2.deleteRecords()
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
		
		and: 'Select (by ID) object'
		Select selectById = mapper.selectById(User.class)
		
		when: 'PreparedStatement is returned, ResultSet is processed, and User variable named "user2" is created manually'
		PreparedStatement stmt = null
		ResultSet rs = null
		User user2 = null
		try {
			conn = H2.getConnection()
			JdbcStatement jdbcStmt = mapper.createQuery(selectById)
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
	
	def 'Create PreparedStatement that can retrieve auto-generated key'() {
		given: 'Insert statement to insert User2'
		Insert insert = new Insert().into('User2')
			.columns(new ColumnList().column('name'))
			.values(new ColumnValues().param('name'))
		
		when: 'executing the insert statement and retrieving the auto-generated key'
		Connection conn = null
		PreparedStatement stmt = null
		ResultSet rs = null
		int autoGeneratedKey = -1
		try {
			conn = H2.getConnection()
			stmt = mapper.createQuery(insert)
				.setString('name', 'Albert')
				.createPreparedStatement(conn, true)
			stmt.executeUpdate()
			rs = stmt.getGeneratedKeys()
			if (rs.next()) {
				autoGeneratedKey = rs.getInt(1)
			}
		} finally {
			JdbcUtils.closeQuietly(rs)
			JdbcUtils.closeQuietly(stmt)
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'auto-generated key is equal to 1'
		autoGeneratedKey == 1
	}
	
	def 'Execute Update statement and retrieve update count'() {
		given: 'two User records in the database'
		H2.deleteRecords()
		mapper.register(User.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new User(1, 'Albert'))
			mapper.save(conn, new User(2, 'Javalbert'))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Update statement to update the two User records'
		Update update = new Update('User').set(new SetValues()
		.add(new SetValue().column('active').param('active'))
		)
		
		when: 'executing Update statement'
		int updateCount = 0
		try {
			conn = H2.getConnection()
			updateCount = mapper.createQuery(update)
				.setBoolean('active', true)
				.executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
				
		then: 'number of updated rows is 2'
		updateCount == 2
	}
	
	def 'Batch insert entity objects'() {
		given: 'Insert statement for inserting Users'
		Insert insertUser = new Insert('User').columns(new ColumnList()
		.column('user_id').column('name').column('active')
		).values(new ColumnValues().param('userId').param('name').param('active')
		)
		
		when: 'adding two batches of User parameters and executing the batch'
		Connection conn = null
		List<int[]> batchCounts = Collections.EMPTY_LIST
		try {
			conn = H2.getConnection()
			batchCounts = mapper.createQuery(insertUser)
				.setInteger('userId', 3)
				.setString('name', 'Albert')
				.setBoolean('active', true)
				.addBatch(conn)
				.setInteger('userId', 4)
				.setString('name', 'Javalbert')
				.setBoolean('active', true)
				.addBatch()
				.executeBatch()
				.getBatchRowCountsList()
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'execution batch size was 1 array containing 2 results (the two Users) of 1 row affected'
		batchCounts == [[1, 1]]
	}
	
	def 'Return a collection of strings from a single column'() {
		given: 'Two Users in the database'
		mapper.register(User.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new User(5, 'Albert'))
			mapper.save(conn, new User(6, 'Javalbert'))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: "Select object to get all User names"
		Select allUserNames = new Select().list(new SelectList()
		.tableAlias('u').column('name')
		).from(new From().tableName('User').as('u')
		)
		
		when: 'executing the Select and returning a set of User names'
		Set<String> userNames = Collections.EMPTY_SET
		try {
			conn = H2.getConnection()
			userNames = mapper.createQuery(allUserNames)
				.toJdbcDataTypeCollection(conn, String.class, new LinkedHashSet<>())
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'user name set is { "Albert", "Javalbert" }'
		userNames == [ 'Albert', 'Javalbert' ] as LinkedHashSet
	}
	
	def 'Pass a new collection and populate it with entity objects from a query'() {
		given: 'Two Users in the database'
		H2.deleteRecords()
		mapper.register(User.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new User(7, 'Albert'))
			mapper.save(conn, new User(8, 'Javalbert'))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'executing a Select statement, passing our own ArrayList, and returning it with User entities'
		List<String> users = Collections.EMPTY_LIST
		try {
			conn = H2.getConnection()
			users = mapper.createQuery(mapper.selectFrom(User.class))
				.toCollection(conn, User.class, new ArrayList<>())
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'our ArrayList containing the two Users is retrieved'
		users == [ new User(7, 'Albert'), new User(8, 'Javalbert') ]
	}
	
	@Unroll('Executing a Select statement and return #implementationClass collection type')
	def 'Return different types of collections for Select statement'() {
		when: 'executing a Select to get all Users and returning a collection'
		mapper.register(User.class)
		Connection conn = null
		Collection collection = null
		try {
			conn = H2.getConnection()
			JdbcStatement stmt = mapper.createQuery(mapper.selectFrom(User.class))
			collection = getCollection(conn, stmt)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: "collection's type matches implementation class"
		collection.getClass() == implementationClass
		
		where: 'collection supplier should return correct implementation class'
		getCollection														||	implementationClass
		{ Connection c, JdbcStatement s -> s.toDeque(c, User.class) }		||	ArrayDeque.class
		{ Connection c, JdbcStatement s -> s.toLinkedSet(c, User.class) }	||	LinkedHashSet.class
		{ Connection c, JdbcStatement s -> s.toList(c, User.class) }		||	ArrayList.class
		{ Connection c, JdbcStatement s -> s.toSet(c, User.class) }			||	HashSet.class
	}
	
	@Unroll('Executing a Select statement and return #implementationClass map type')
	def 'Return different types of maps for Select statement'() {
		when: 'executing a Select to get all Users and returning a map'
		mapper.register(User.class)
		Connection conn = null
		Map map = null
		try {
			conn = H2.getConnection()
			JdbcStatement stmt = mapper.createQuery(mapper.selectFrom(User.class))
			map = getMap(conn, stmt)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: "map's type matches implementation class"
		map.getClass() == implementationClass
		
		where: 'map supplier should return correct implementation class'
		getMap																||	implementationClass
		{ Connection c, JdbcStatement s -> s.toLinkedMap(c, User.class) }	||	LinkedHashMap.class
		{ Connection c, JdbcStatement s -> s.toMap(c, User.class) }			||	HashMap.class
	}
	
	@Unroll('Set #column to #parameter and return #expectedResult')
	def 'Set different types of data as parameters'() {
		given: 'six DataTypeHolder entities inserted into the database'
		H2.deleteRecords()
		mapper.register(DataTypeHolder.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, dtOne())
			mapper.save(conn, dtTwo())
			mapper.save(conn, dtThree())
			mapper.save(conn, dtFour())
			mapper.save(conn, dtFive())
			mapper.save(conn, dtSix())
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Select statement'
		Select select = mapper.selectFrom(DataTypeHolder.class)
			.where(new Where().predicate(predicate)
			)
		
		when: 'executing Select statement'
		List<DataTypeHolder> result = null
		try {
			conn = H2.getConnection()
			JdbcStatement stmt = mapper.createQuery(select)
			setParameter(stmt, parameter)
			result = stmt.toList(conn, DataTypeHolder.class)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'result should be expected'
		result == expectedResult
		
		where: 'predicate condition will set parameter and query will return expected result'
		column	|	predicate	|	parameter	|	setParameter	||	expectedResult
		'int_val'	|	new Predicate().column('int_val').eq().param('intVal')	|	Integer.MAX_VALUE	|	{ JdbcStatement s, int x -> s.setInteger('intVal', x) }	||	[ dtOne() ]
		'boolean_val'	|	new Predicate().column('boolean_val').eq().param('booleanVal')	|	false	|	{ JdbcStatement s, boolean x -> s.setBoolean('booleanVal', x) }	||	[ dtOne(), dtFive(), dtSix() ]
		'bigint_val'	|	new Predicate().column('bigint_val').eq().param('bigintVal')	|	2_147_483_648L	|	{ JdbcStatement s, long x -> s.setLong('bigintVal', x) }	||	[ dtTwo() ]
		'decimal_val'	|	new Predicate().column('decimal_val').eq().param('decimalVal')	|	new BigDecimal('0.0')	|	{ JdbcStatement s, BigDecimal x -> s.setBigDecimal('decimalVal', x) }	||	[ dtThree() ]
		// TODO add more
	}
	
	// TOOD add a new parameterized test to test using different data types for toJdbcTypeCollection() method
	
	private DataTypeHolder dtOne() { return new DataTypeHolder(1, Integer.MAX_VALUE, false, Long.MAX_VALUE, new BigDecimal('123456789.99'), Double.MAX_VALUE, Float.MAX_VALUE, DateUtils.newDate(2017, 2, 5), DateUtils.newDate(2017, 2, 5, 22, 5), 'max primitives') }
	private DataTypeHolder dtTwo() { return new DataTypeHolder(2, -1, true, 2_147_483_648L, new BigDecimal('1789.95'), 1789.95d, 1789.95f, DateUtils.newDate(2014, 9, 1), DateUtils.newDate(2015, 1, 6, 11, 30), 'gaming pc') }
	private DataTypeHolder dtThree() { return new DataTypeHolder(3, 100, true, 500L, new BigDecimal('0.0'), 2.6d, 4.1f, DateUtils.newDate(2016, 6, 22), DateUtils.newDate(2016, 12, 25, 9), 'google play app page for a company internal mobile app') }
	private DataTypeHolder dtFour() { return new DataTypeHolder(4, 25, true, 1L, new BigDecimal('149.99'), 2.6d, 4.1f, DateUtils.newDate(1991, 11, 15), DateUtils.newDate(2017, 11, 15, 14, 30), '1MORE Triple Driver') }
	private DataTypeHolder dtFive() { return new DataTypeHolder(5, 5_728_337, false, 7_482_460_420L, new BigDecimal('10697403244'), 406.07d, 1.76f, DateUtils.newDate(1760), DateUtils.newDate(2016, 12, 31, 23, 59), 'worldometers, ppm CO2, avg 2016 temp from 20th century') }
	private DataTypeHolder dtSix() { return new DataTypeHolder(6, 0, false, 0L, null, -273.15d, -459.67f, null, null, null) }
}