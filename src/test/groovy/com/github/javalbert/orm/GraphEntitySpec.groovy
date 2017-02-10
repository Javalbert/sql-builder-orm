package com.github.javalbert.orm

import java.sql.Connection

import com.github.javalbert.domain.Customer
import com.github.javalbert.domain.Order
import com.github.javalbert.domain.Product
import com.github.javalbert.domain.Store
import com.github.javalbert.h2.H2
import com.github.javalbert.utils.DateUtils
import com.github.javalbert.utils.jdbc.JdbcUtils

import spock.lang.Specification

class GraphEntitySpec extends Specification {
	private JdbcMapper mapper
	private ObjectGraphResolver graphResolver
	
	def setupSpec() {
		H2.deleteRecords()
		H2.createTables()
	}
	
	def setup() {
		mapper = new JdbcMapper()
		mapper.register(Customer.class)
		mapper.register(Store.class)
		mapper.register(Order.class)
		mapper.register(Product.class)
		graphResolver = new BatchResolver(mapper)
	}
	
	def 'Retrieve child records of a parent object in a list'() {
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
		
		when: 'a Relationship is built between Customer and Order where Customer is related to many Orders'
		customerEntity.isRelatedToMany(orderEntity)
			.inList('orderList')
			.joinedBy('customer_id')
			.build()
		
		and: 'get the Customer object with orders'
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
}