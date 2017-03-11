package com.github.javalbert.orm

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp

import com.github.javalbert.orm.JdbcMapper
import com.github.javalbert.orm.JdbcStatement
import com.github.javalbert.sqlbuilder.ColumnList
import com.github.javalbert.sqlbuilder.ColumnValues
import com.github.javalbert.sqlbuilder.Condition
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.Insert
import com.github.javalbert.sqlbuilder.Predicate
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.SetValue
import com.github.javalbert.sqlbuilder.SetValues
import com.github.javalbert.sqlbuilder.Update
import com.github.javalbert.sqlbuilder.Where
import com.github.javalbert.sqlbuilder.With
import com.github.javalbert.utils.jdbc.JdbcUtils
import com.github.javalbert.domain.Customer
import com.github.javalbert.domain.DataTypeHolder
import com.github.javalbert.domain.Order
import com.github.javalbert.domain.Product
import com.github.javalbert.domain.Store
import com.github.javalbert.domain.User
import com.github.javalbert.h2.H2
import com.github.javalbert.utils.DateUtils
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
		'double_val'	|	new Predicate().column('double_val').eq().param('doubleVal')	|	2.6d	|	{ JdbcStatement s, double x -> s.setDouble('doubleVal', x) }	||	[ dtThree(), dtFour() ]
		'real_val'	|	new Predicate().column('real_val').eq().param('realVal')	|	1.76f	|	{ JdbcStatement s, float x -> s.setFloat('realVal', x) }	||	[ dtFive() ]
		'date_val'	|	new Predicate().column('date_val').eq().param('dateVal')	|	DateUtils.newDate(2017, 2, 5)	|	{ JdbcStatement s, Date x -> s.setDate('dateVal', x) }	||	[ dtOne() ]
		'timestamp_val'	|	new Predicate().column('timestamp_val').eq().param('timestampVal')	|	DateUtils.newDate(2015, 1, 6, 11, 30)	|	{ JdbcStatement s, Date x -> s.setTimestamp('timestampVal', x) }	||	[ dtTwo() ]
		'varchar_val'	|	new Predicate().column('varchar_val').eq().param('varcharVal')	|	'1MORE Triple Driver'	|	{ JdbcStatement s, String x -> s.setString('varcharVal', x) }	||	[ dtFour() ]
		'ints'	|	new Predicate().column('int_val').in().param('ints')	|	[ -1, 0 ]	|	{ JdbcStatement s, Collection<Integer> x -> s.setIntegers('ints', x) }	||	[ dtTwo(), dtSix() ]
		'bigints'	|	new Predicate().column('bigint_val').in().param('bigints')	|	[ 500L, 0L ]	|	{ JdbcStatement s, Collection<Long> x -> s.setLongs('bigints', x) }	||	[ dtThree(), dtSix() ]
		'decimals'	|	new Predicate().column('decimal_val').in().param('decimals')	|	[ new BigDecimal('149.99'), new BigDecimal('10697403244') ]	|	{ JdbcStatement s, Collection<BigDecimal> x -> s.setBigDecimals('decimals', x) }	||	[ dtFour(), dtFive() ]
		'doubles'	|	new Predicate().column('double_val').in().param('doubles')	|	[ Double.MAX_VALUE, 2.6d ]	|	{ JdbcStatement s, Collection<Double> x -> s.setDoubles('doubles', x) }	||	[ dtOne(), dtThree(), dtFour() ]
		'reals'	|	new Predicate().column('real_val').in().param('reals')	|	[ 1789.95f, 1.76f ]	|	{ JdbcStatement s, Collection<Float> x -> s.setFloats('reals', x) }	||	[ dtTwo(), dtFive() ]
		'dates'	|	new Predicate().column('date_val').in().param('dates')	|	[ DateUtils.newDate(2014, 9, 1), DateUtils.newDate(1991, 11, 15) ]	|	{ JdbcStatement s, Collection<Date> x -> s.setDates('dates', x) }	||	[ dtTwo(), dtFour() ]
		'timestamps'	|	new Predicate().column('timestamp_val').in().param('timestamps')	|	[ DateUtils.newDate(2017, 2, 5, 22, 5), DateUtils.newDate(2016, 12, 31, 23, 59) ]	|	{ JdbcStatement s, Collection<Date> x -> s.setTimestamps('timestamps', x) }	||	[ dtOne(), dtFive() ]
		'varchars'	|	new Predicate().column('varchar_val').in().param('varchars')	|	[ 'max primitives', 'gaming pc' ]	|	{ JdbcStatement s, Collection<String> x -> s.setStrings('varchars', x) }	||	[ dtOne(), dtTwo() ]
	}
	
	private DataTypeHolder dtOne() { return new DataTypeHolder(1, Integer.MAX_VALUE, false, Long.MAX_VALUE, new BigDecimal('123456789.99'), Double.MAX_VALUE, Float.MAX_VALUE, DateUtils.newDate(2017, 2, 5), DateUtils.newDate(2017, 2, 5, 22, 5), 'max primitives') }
	private DataTypeHolder dtTwo() { return new DataTypeHolder(2, -1, true, 2_147_483_648L, new BigDecimal('1789.95'), 1789.95d, 1789.95f, DateUtils.newDate(2014, 9, 1), DateUtils.newDate(2015, 1, 6, 11, 30), 'gaming pc') }
	private DataTypeHolder dtThree() { return new DataTypeHolder(3, 100, true, 500L, new BigDecimal('0.0'), 2.6d, 4.1f, DateUtils.newDate(2016, 6, 22), DateUtils.newDate(2016, 12, 25, 9), 'google play app page for a company internal mobile app') }
	private DataTypeHolder dtFour() { return new DataTypeHolder(4, 25, true, 1L, new BigDecimal('149.99'), 2.6d, 4.1f, DateUtils.newDate(1991, 11, 15), DateUtils.newDate(2017, 11, 15, 14, 30), '1MORE Triple Driver') }
	private DataTypeHolder dtFive() { return new DataTypeHolder(5, 5_728_337, false, 7_482_460_420L, new BigDecimal('10697403244'), 406.07d, 1.76f, DateUtils.newDate(1760), DateUtils.newDate(2016, 12, 31, 23, 59), 'worldometers, ppm CO2, avg 2016 temp from 20th century') }
	private DataTypeHolder dtSix() { return new DataTypeHolder(6, 0, false, 0L, null, -273.15d, -459.67f, null, null, '') }
	
	@Unroll('Get collection of #column and return #collection')
	def 'Get collection of a single column'() {
		given: 'Five DataTypeHolder entities inserted into the database'
		H2.deleteRecords()
		mapper.register(DataTypeHolder.class)
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new DataTypeHolder(1, 5, true, 5631518994637769878L, new BigDecimal('8.16'), 0.8204935137327953d, 0.85783935f, DateUtils.newDate(2000, 8, 24), DateUtils.newDate(2002, 11, 28, 17, 0), 'd'))
			mapper.save(conn, new DataTypeHolder(2, 1, false, 7285848453178880732L, new BigDecimal('7.77'), 0.6581955280812539d, 0.10136908f, DateUtils.newDate(2013, 2, 23), DateUtils.newDate(2017, 8, 28, 11, 4), 'a'))
			mapper.save(conn, new DataTypeHolder(3, 3, false, -4038734251018366632L, new BigDecimal('9.03'), 0.5171897338384364d, 0.7276574f, DateUtils.newDate(1999, 8, 12), DateUtils.newDate(2016, 5, 19, 22, 31), 'b'))
			mapper.save(conn, new DataTypeHolder(4, 4, true, 1891821427610479213L, new BigDecimal('5.43'), 0.3374258427368241d, 0.26921433f, DateUtils.newDate(2008, 2, 1), DateUtils.newDate(1992, 1, 15, 8, 59), 'c'))
			mapper.save(conn, new DataTypeHolder(5, 2, true, 6371700120128492334L, new BigDecimal('9.17'), 0.7918775653398716d, 0.5695091f, DateUtils.newDate(2013, 8, 6), DateUtils.newDate(2000, 3, 15, 22, 53), 'e'))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Select statement selecting a single column'
		Select select = new Select().list(new SelectList().column(column)
		).from(new From().tableName('DataTypeHolder'))
		
		when: 'executing the Select and returning a collection'
		Collection result = null
		try {
			conn = H2.getConnection()
			result = mapper.createQuery(select)
				.toJdbcDataTypeCollection(conn, clazz, new ArrayList<>())
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'result is equal to collection of values'
		result == collection
		
		where: 'column returns collection'
		column	|	clazz	||	collection
		'int_val'	|	Integer.class	||	[ 5, 1, 3, 4, 2 ]
		'boolean_val'	|	Boolean.class	||	[ true, false, false, true, true ]
		'bigint_val'	|	Long.class	||	[ 5631518994637769878L, 7285848453178880732L, -4038734251018366632L, 1891821427610479213L, 6371700120128492334L ]
		'decimal_val'	|	BigDecimal.class	||	[ new BigDecimal('8.16'), new BigDecimal('7.77'), new BigDecimal('9.03'), new BigDecimal('5.43'), new BigDecimal('9.17') ]
		'double_val'	|	Double.class	||	[ 0.8204935137327953d, 0.6581955280812539d, 0.5171897338384364d, 0.3374258427368241d, 0.7918775653398716d ]
		'real_val'	|	Float.class	||	[ 0.85783935f, 0.10136908f, 0.7276574f, 0.26921433f, 0.5695091f ]
		'date_val'	|	Date.class	||	[ DateUtils.newDate(2000, 8, 24), DateUtils.newDate(2013, 2, 23), DateUtils.newDate(1999, 8, 12), DateUtils.newDate(2008, 2, 1), DateUtils.newDate(2013, 8, 6) ]
		'timestamp_val'	|	Timestamp.class	||	[ DateUtils.newDate(2002, 11, 28, 17, 0), DateUtils.newDate(2017, 8, 28, 11, 4), DateUtils.newDate(2016, 5, 19, 22, 31), DateUtils.newDate(1992, 1, 15, 8, 59), DateUtils.newDate(2000, 3, 15, 22, 53) ]
		'varchar_val'	|	String.class	||	[ 'd', 'a', 'b', 'c', 'e' ]
	}
	
	def 'Execute SELECT statement with multiple instances of the same IN parameter'() {
		given: "SELECT statement which gets a customer's bought products joined by other products by product name and in similiar prices"
		mapper.register(Customer.class)
		mapper.register(Store.class)
		mapper.register(Order.class)
		mapper.register(Product.class)
		Select select = new Select()
		.list(new SelectList().tableAlias('bprod').column('order_id')
			.tableAlias('bprod').column('product_id')
			.tableAlias('bprod').column('product_name')
			.tableAlias('bprod').column('price')
			.tableAlias('other_prod').column('order_id')
			.tableAlias('other_prod').column('product_id')
			.tableAlias('other_prod').column('product_name')
			.tableAlias('other_prod').column('price')
		).from(new From().inlineView(
			new Select().list(new SelectList()
				.tableAlias('bprod').column('order_id')
				.tableAlias('bprod').column('product_id')
				.tableAlias('bprod').column('product_name')
				.tableAlias('bprod').column('price')
				).from(new From().tableName('Product').as('bprod')
				).where(new Where().predicate(new Predicate().tableAlias('bprod').column('price').in().param('prices'))
				.and().predicate(new Predicate().exists().subquery(
					new Select().list(new SelectList().column('*')
					).from(new From().tableName('Orders').as('ord')
					).where(new Where().predicate(new Predicate().tableAlias('bprod').column('order_id').eq().tableAlias('ord').column('order_id')
					).and().predicate(new Predicate().exists().subquery(
						new Select().list(new SelectList().column('*')
						).from(new From().tableName('Customer').as('subject')
						).where(new Where().predicate(new Predicate().tableAlias('ord').column('customer_id').eq().tableAlias('subject').column('customer_id')
						).and().predicate(new Predicate().tableAlias('subject').column('full_name').eq().param('customerName')
							)
						)
						)
						)
					)
					)
				)
				)
			).as('bprod').innerJoin().tableName('Product').as('other_prod')
			.on(new Condition().predicate(new Predicate().tableAlias('bprod').column('product_name').eq().tableAlias('other_prod').column('product_name'))
			)
		).where(new Where().predicate(new Predicate().tableAlias('other_prod').column('price').in().param('prices'))
			.and().predicate(new Predicate().tableAlias('other_prod').column('product_id').noteq().tableAlias('bprod').column('product_id'))
		)
		
		and: 'a Store, Customer records, their Orders, and Products'
		H2.deleteRecords()
		Connection conn = null
		try {
			conn = H2.getConnection()
			
			mapper.save(conn, new Store(''))
			
			mapper.save(conn, new Customer('Albert Chan'))
			mapper.save(conn, new Order(1, 1))
			mapper.save(conn, new Product(1, 'Skipping Rope', new BigDecimal('7.99')))
			mapper.save(conn, new Product(1, 'Chinese Candy', new BigDecimal('13.99')))
			
			mapper.save(conn, new Customer('Patrick Pu'))
			mapper.save(conn, new Order(2, 1))
			mapper.save(conn, new Product(2, 'Skipping Rope', new BigDecimal('189.00')))
			mapper.save(conn, new Product(2, 'Chinese Candy', new BigDecimal('7.99')))
			mapper.save(conn, new Product(2, 'Shawarma', new BigDecimal('13.99')))
			
			mapper.save(conn, new Customer('Raymond Ren'))
			mapper.save(conn, new Order(3, 1))
			mapper.save(conn, new Product(3, 'Chinese Candy', new BigDecimal('13.99')))
			
			mapper.save(conn, new Customer('Sandor Balo'))
			mapper.save(conn, new Order(4, 1))
			mapper.save(conn, new Product(4, 'Skipping Rope', new BigDecimal('13.99')))
			mapper.save(conn, new Product(4, 'GTX 1060', new BigDecimal('189.00')))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'SQL statement is executed'
		List<Object[]> resultList = null
		try {
			conn = H2.getConnection()
			resultList = mapper.createQuery(select)
					.setString('customerName', 'Albert Chan')
					.setBigDecimals('prices', Arrays.asList(new BigDecimal('7.99'), new BigDecimal('13.99')))
					.toResultList(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: '"Chinese Candy" product matches with 2 other products from other orders'
		int otherOrderId = 4
		resultList[0][otherOrderId] == 2
		resultList[1][otherOrderId] == 3
		
		and: '"Skipping Rope" product matches with 1 other product with different price'
		resultList[2][otherOrderId] == 4
	}
}