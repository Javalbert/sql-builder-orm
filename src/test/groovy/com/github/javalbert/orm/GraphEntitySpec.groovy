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
	
	def 'Fetch child records of a parent object in a list joined by columns with the same name'() {
		given: 'GraphEntity objects for Customer and Order tables'
		GraphEntity<Customer> customerEntity = new GraphEntity<>(Customer.class, 'cus')
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		
		and: 'a Customer record with 2 Orders'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('36.33'), DateUtils.newDate(2014, 6, 18)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('159.83'), DateUtils.newDate(2014, 6, 27)))
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
			customer = mapper.get(conn, customerEntity, 1L, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the Customer has 2 Orders in orders field'
		customer.orders.size() == 2
	}
	
	def 'Fetch child records of a parent object joined by columns with different names'() {
		given: 'GraphEntity objects for Store and Order tables'
		GraphEntity<Store> storeEntity = new GraphEntity<>(Store.class, 's')
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		
		and: 'a Store record with 2 Orders'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('36.33'), DateUtils.newDate(2014, 6, 18)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('159.83'), DateUtils.newDate(2014, 6, 27)))
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
			store = mapper.get(conn, storeEntity, 1L, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the Store has 2 Orders in orders field'
		store.orders.size() == 2
	}
	
	@Unroll('Fetch child container and expect its implementation is #implementationClass')
	def 'Fetch different implementations for holding child objects'() {
		given: 'GraphEntity objects for Order and Product tables'
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		GraphEntity<Product> productEntity = new GraphEntity<>(Product.class, 'prod')
		
		and: 'a Order record with 2 Products'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('436.78'), DateUtils.newDate(2017, 2, 9)))
			mapper.save(conn, new Product(1L, '1MORE Triple Driver', new BigDecimal('149.99')))
			mapper.save(conn, new Product(1L, 'Audio-Technica ATH-M50x', new BigDecimal('286.79')))
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
			order = mapper.get(conn, orderEntity, 1L, graphResolver)
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
	
	def 'Fetch child records of parent objects and set a reference to the parent in the child objects'() {
		given: 'GraphEntity objects for Store and Order tables'
		GraphEntity<Store> storeEntity = new GraphEntity<>(Store.class, 's')
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		
		and: '2 Stores with 2 Orders each'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Store 1'))
			mapper.save(conn, new Store('Store 2'))
			mapper.save(conn, new Order(1L, 1L))
			mapper.save(conn, new Order(1L, 1L))
			mapper.save(conn, new Order(1L, 2L))
			mapper.save(conn, new Order(1L, 2L))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Store has many Orders, and inverse owner field is "store" to set Store reference in each Order'
		storeEntity.isRelatedToMany(orderEntity)
			.inList('orders')
			.joinedBy('store_key', 'store_id')
			.inverseOwnerField('store')
			.build()
		
		and: 'get the Stores'
		List<Store> stores = new ArrayList<>()
		try {
			conn = H2.getConnection()
			mapper.createQuery(mapper.selectFrom(Store.class))
					.toCollection(conn, storeEntity, stores, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: "each Order's store is the same object as the Store containing them"
		stores[0].orders[0].store == stores[0]
		stores[0].orders[1].store == stores[0]
		stores[1].orders[0].store == stores[1]
		stores[1].orders[1].store == stores[1]
	}
	
	def 'Get a list of objects that may have the same parent objects'() {
		given: 'GraphEntity objects for Store and Order tables'
		GraphEntity<Store> storeEntity = new GraphEntity<>(Store.class, 's')
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		
		and: 'a Store record with 2 Orders'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Store 1'))
			mapper.save(conn, new Store('Store 2'))
			mapper.save(conn, new Order(1L, 1L))
			mapper.save(conn, new Order(1L, 1L))
			mapper.save(conn, new Order(1L, 2L))
			mapper.save(conn, new Order(1L, 2L))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Order can belong to one Store and a Store can have many Orders'
		orderEntity.isRelatedToOne(storeEntity)
			.inField('store')
			.joinedBy('store_id', 'store_key')
			.build()
		storeEntity.isRelatedToMany(new GraphEntity<>(Order.class, 'ord2'))
			.inList('orders')
			.joinedBy('store_key', 'store_id')
			.build()
		
		and: 'Orders are retrieved'
		List<Order> orders = new ArrayList<>()
		try {
			conn = H2.getConnection()
			mapper.createQuery(mapper.selectFrom(Order.class))
				.toCollection(conn, orderEntity, orders, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'some Orders have the same Store'
		orders[0].store == orders[1].store
		orders[2].store == orders[3].store
		
		and: 'the Stores contain the same Orders'
		orders[0].store.orders == [ orders[0], orders[1] ]
		orders[2].store.orders == [ orders[2], orders[3] ]
	}
	
	def 'Fetch 2 levels deep of relationships'() {
		given: 'GraphEntity objects for Customer, Store, Order, and Product tables'
		GraphEntity<Customer> customerEntity = new GraphEntity<>(Customer.class, 'cus')
		GraphEntity<Store> storeEntity = new GraphEntity<>(Store.class, 's')
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		GraphEntity<Product> productEntity = new GraphEntity<>(Product.class, 'prod')
		
		and: 'a Customer with a Order that has a Store and a Product'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Store('Amazon.ca'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('21.99'), DateUtils.newDate(2015, 11, 7)))
			mapper.save(conn, new Product(1L, 'CYPRUS Double Walled Heatproof Glass Mug', new BigDecimal('21.99')))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'relationships are built: Customer has many Orders, Order has one Store, Order has many Products'
		customerEntity.isRelatedToMany(orderEntity)
			.inList('orderList')
			.joinedBy('customer_id')
			.build()
		orderEntity.isRelatedToOne(storeEntity)
			.inField('store')
			.joinedBy('store_id', 'store_key')
			.build()
		orderEntity.isRelatedToMany(productEntity)
			.inList('productList')
			.joinedBy('order_id')
			.build()
		
		and: 'get the Customer'
		Customer customer = null
		try {
			conn = H2.getConnection()
			customer = mapper.get(conn, customerEntity, 1L, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'Customer contains 1 Order that has 1 Store and 1 Product'
		customer.orders.size() == 1
		customer.orders[0].store != null
		customer.orders[0].productList.size() == 1
	}
	
	def 'Fetch an ordered list of children'() {
		given: 'GraphEntity objects for Order and Product'
		GraphEntity<Order> orderEntity = new GraphEntity<>(Order.class, 'ord')
		GraphEntity<Product> productEntity = new GraphEntity<>(Product.class, 'prod')
		
		and: 'a Order with 4 Products'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Customer('Albert'))
			mapper.save(conn, new Order(1L, null, new BigDecimal('556.50'), DateUtils.newDate(2016, 12, 29)))
			mapper.save(conn, new Product(1L, 'The Definitive ANTLR 4 Reference', new BigDecimal('48.62')))
			mapper.save(conn, new Product(1L, 'ASUS F555LA 15.6" Full-HD Laptop (Core i3, 4GB RAM, 500GB HDD) with Windows 10', new BigDecimal('489.99')))
			mapper.save(conn, new Product(1L, 'LG G2 Case, MagicMobile® Hybrid Rugged', new BigDecimal('5.99')))
			mapper.save(conn, new Product(1L, 'PThink 0.3mm Ultra-thin Tempered Glass Screen Protector for LG G2', new BigDecimal('11.90')))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		when: 'a Relationship is defined to have Products sorted by their price in descending order'
		orderEntity.isRelatedToMany(productEntity)
			.inList('productList')
			.joinedBy('order_id')
			.descendingOrder('price')
			.build()
		
		and: 'get Order'
		Order order = null
		try {
			conn = H2.getConnection()
			order = mapper.get(conn, orderEntity, 1L, graphResolver)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'list of products is sorted by price in the Order'
		order.productList[0].productId == 2L
		order.productList[1].productId == 1L
		order.productList[2].productId == 4L
		order.productList[3].productId == 3L
	}
}