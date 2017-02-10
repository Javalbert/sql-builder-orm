package com.github.javalbert.orm

import java.sql.Connection

import com.github.javalbert.domain.Customer
import com.github.javalbert.domain.Order
import com.github.javalbert.domain.Product
import com.github.javalbert.domain.Store
import com.github.javalbert.h2.H2
import com.github.javalbert.orm.Relationship.Builder
import com.github.javalbert.utils.DateUtils
import com.github.javalbert.utils.jdbc.JdbcUtils

import spock.lang.Specification
import spock.lang.Unroll

class GraphEntitySpec extends Specification {
	private JdbcMapper mapper
	private ObjectGraphResolver graphResolver
	
	def setupSpec() {
		H2.createTables()
	}
	
	def setup() {
		H2.deleteRecords()
		mapper = new JdbcMapper()
		mapper.register(Customer.class)
		mapper.register(Store.class)
		mapper.register(Order.class)
		mapper.register(Product.class)
		graphResolver = new BatchResolver(mapper)
	}
	
	def 'Retrieve child records of a parent object in a list joined by columns with the same name'() {
		given: 'GraphEntity objects for Customer and Order tables'
		GraphEntity customerEntity = new GraphEntity(Customer.class, 'cus')
		GraphEntity orderEntity = new GraphEntity(Order.class, 'ord')
		
		and: 'a Customer record with 2 Orders'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1, 1, new BigDecimal('36.33'), DateUtils.newDate(2014, 6, 18)))
			mapper.save(conn, new Order(1, 1, new BigDecimal('159.83'), DateUtils.newDate(2014, 6, 27)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Relationship is built between Customer and Order joined by "customer_id" columns'
		customerEntity.isRelatedToMany(orderEntity)
			.inList('orderList')
			.joinedBy('customer_id')
			.build()
		
		and: 'get the Customer object with Orders'
		Customer customer = null
		try {
			conn = H2.getConnection()
			customer = mapper.get(conn, customerEntity, 1, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the Customer has 2 Orders in orders field'
		customer.orders.size() == 2
	}
	
	def 'Retrieve child records of a parent object joined by columns with different names'() {
		given: 'GraphEntity objects for Store and Order tables'
		GraphEntity storeEntity = new GraphEntity(Store.class, 's')
		GraphEntity orderEntity = new GraphEntity(Order.class, 'ord')
		
		and: 'a Store record with 2 Orders'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1, 1, new BigDecimal('36.33'), DateUtils.newDate(2014, 6, 18)))
			mapper.save(conn, new Order(1, 1, new BigDecimal('159.83'), DateUtils.newDate(2014, 6, 27)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Relationship is built between Store and Order by linking "store_key" on Store to "store_id" on Orders table'
		storeEntity.isRelatedToMany(orderEntity)
			.inList('orders')
			.joinedBy('store_key', 'store_id')
			.build()
		
		and: 'get the Store object with Orders'
		Store store = null
		try {
			conn = H2.getConnection()
			store = mapper.get(conn, storeEntity, 1, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the Store has 2 Orders in orders field'
		store.orders.size() == 2
	}
	
	@Unroll('Retrieve child container and expect its implementation is #implementationClass')
	def 'Retrieve different implementations for holding child objects'() {
		given: 'GraphEntity objects for Order and Product tables'
		GraphEntity orderEntity = new GraphEntity(Order.class, 'ord')
		GraphEntity productEntity = new GraphEntity(Product.class, 'prod')
		
		and: 'a Order record with 2 Products'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1, 1, new BigDecimal('436.78'), DateUtils.newDate(2017, 2, 9)))
			mapper.save(conn, new Product(1, '1MORE Triple Driver', new BigDecimal('149.99')))
			mapper.save(conn, new Product(1, 'Audio-Technica ATH-M50x', new BigDecimal('286.79')))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Relationship is built between Order and Product where Order is related to many Products'
		Builder builder = orderEntity.isRelatedToMany(productEntity)
		inFieldType(builder)
		builder.joinedBy('order_id').build()
		
		and: 'get the Order object with Products'
		Order order = null
		try {
			conn = H2.getConnection()
			order = mapper.get(conn, orderEntity, 1, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'children are contained in the implementation class'
		getChildren(order).getClass() == implementationClass
		
		where: 'Products are added in a collection or map type in Order class and retrieved collection or map is the implementation class'
		inFieldType	|	getChildren	||	implementationClass
		{ Builder b -> b.inLinkedMap('productMap') }	|	{ Order o -> o.productMap }	||	LinkedHashMap.class
		{ Builder b -> b.inLinkedSet('productSet') }	|	{ Order o -> o.productSet }	||	LinkedHashSet.class
		{ Builder b -> b.inList('productList') }	|	{ Order o -> o.productList }	||	ArrayList.class
		{ Builder b -> b.inMap('productMap') }	|	{ Order o -> o.productMap }	||	HashMap.class
		{ Builder b -> b.inSet('productSet') }	|	{ Order o -> o.productSet }	||	HashSet.class
	}
}