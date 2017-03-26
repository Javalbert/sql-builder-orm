package com.github.javalbert.sqlbuilder

import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDateTime

import com.github.javalbert.domain.ArchivedOrder
import com.github.javalbert.domain.Customer
import com.github.javalbert.domain.Order
import com.github.javalbert.domain.Store
import com.github.javalbert.h2.HSQLDB
import com.github.javalbert.orm.ClassRowRegistration
import com.github.javalbert.orm.JdbcMapper
import com.github.javalbert.utils.DateUtils
import com.github.javalbert.utils.jdbc.JdbcUtils

import spock.lang.Specification

class MergeSpec extends Specification {
	private static final Merge MERGE_ARCHIVED_ORDERS_FROM_ORDERS = new Merge().into('ArchivedOrders').as('arc')
		.using(
			new Select().list(new SelectList()
				.tableAlias('ord').column('order_id')
				.tableAlias('cus').column('customer_id')
				.tableAlias('st').column('store_key')
				.tableAlias('cus').column('full_name').as('customer_name')
				.tableAlias('st').column('store_name')
				.tableAlias('ord').column('sales_amount')
				.tableAlias('ord').column('order_datetime')
			).from(new From().tableName('Customer').as('cus').innerJoin().tableName('Orders').as('ord')
				.on(new Condition().predicate(new Predicate().tableAlias('cus').column('customer_id').eq().tableAlias('ord').column('customer_id'))
				).innerJoin().tableName('Store').as('st')
				.on(new Condition().predicate(new Predicate().tableAlias('ord').column('store_id').eq().tableAlias('st').column('store_key'))
				)
			).where(new Where().predicate(new Predicate().tableAlias('cus').column('full_name').eq().param('customerName'))
			)
		).as('ord')
		.on(new Condition().predicate(new Predicate().tableAlias('arc').column('order_id').eq().tableAlias('ord').column('order_id')
			).and().predicate(new Predicate().tableAlias('arc').column('customer_id').eq().tableAlias('ord').column('customer_id')
			).and().predicate(new Predicate().tableAlias('arc').column('store_key').eq().tableAlias('ord').column('store_key')
			)
		).immutable()
	
	private JdbcMapper mapper
	
	def setup() {
		mapper = new JdbcMapper()
		mapper.register(Customer.class)
		mapper.register(Store.class)
		mapper.register(Order.class)
		mapper.register(new ClassRowRegistration(ArchivedOrder.class)
			.table('ArchivedOrders')
			.columnInProperty('archivedOrderId', 'archived_order_id', null, ClassRowRegistration.FLAG_ID | ClassRowRegistration.FLAG_GENERATED_VALUE)
			.columnInProperty('orderId', 'order_id', null, 0)
			.columnInProperty('customerId', 'customer_id', null, 0)
			.columnInProperty('storeKey', 'store_key', null, 0)
			.columnInProperty('customerName', 'customer_name', null, 0)
			.columnInProperty('storeName', 'store_name', null, 0)
			.columnInProperty('salesAmount', 'sales_amount', null, 0)
			.columnInProperty('orderDateTime', 'order_datetime', null, 0)
			)
		HSQLDB.createTables()
		HSQLDB.deleteRecords()
	}
	
	def 'Test MERGE DELETE'() {
		given: '4 Orders'
		Connection conn = null
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new Customer('Albert Chan'))
			mapper.save(conn, new Store('Dental'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('80.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('90.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('1655.00'), DateUtils.newDate(2017, 3, 17, 9, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('5.59'), DateUtils.newDate(2017, 3, 24, 9, 30)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: '4 ArchivedOrders (4 of 4 Orders) in the database'
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new ArchivedOrder(1L, 1L, 1L))
			mapper.save(conn, new ArchivedOrder(2L, 1L, 1L))
			mapper.save(conn, new ArchivedOrder(3L, 1L, 1L))
			mapper.save(conn, new ArchivedOrder(4L, 1L, 1L))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Merge object that deletes ArchivedOrders that matches Orders'
		Merge merge = MERGE_ARCHIVED_ORDERS_FROM_ORDERS.mutable()
		.whenMatchedThen()
		.delete()
		
		when: 'the MERGE statement is executed'
		int rowCount = 4 // See com.github.javalbert.h2.HSQLDBMergeBug under src/test/java
		try {
			conn = HSQLDB.getConnection()
			rowCount = mapper.createQuery(merge)
					.setString('customerName', 'Albert Chan')
					.executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the 4 Orders are removed from the archive table'
		thrown(SQLException) // See com.github.javalbert.h2.HSQLDBMergeBug under src/test/java
		rowCount == 4
	}
	
	def 'Test MERGE INSERT'() {
		given: '4 Orders'
		Connection conn = null
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new Customer('Albert Chan'))
			mapper.save(conn, new Store('Dental'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('80.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('90.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('1655.00'), DateUtils.newDate(2017, 3, 17, 9, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('5.59'), DateUtils.newDate(2017, 3, 24, 9, 30)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: '2 ArchivedOrders (2 of 4 Orders) in the database'
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new ArchivedOrder(1L, 1L, 1L))
			mapper.save(conn, new ArchivedOrder(2L, 1L, 1L))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Merge object that inserts missing ArchivedOrders from Orders'
		Merge merge = MERGE_ARCHIVED_ORDERS_FROM_ORDERS.mutable()
		.whenNotMatchedThen()
		.insert(
			new Insert().columns(new ColumnList()
				.column('order_id')
				.column('customer_id')
				.column('store_key')
				.column('customer_name')
				.column('store_name')
				.column('sales_amount')
				.column('order_datetime')
			).values(new ColumnValues()
				.tableAlias('ord').column('order_id')
				.tableAlias('ord').column('customer_id')
				.tableAlias('ord').column('store_key')
				.tableAlias('ord').column('customer_name')
				.tableAlias('ord').column('store_name')
				.tableAlias('ord').column('sales_amount')
				.tableAlias('ord').column('order_datetime')
			)
		)
		
		when: 'the Orders are merged into ArchivedOrders'
		int rowCount = -1
		try {
			conn = HSQLDB.getConnection()
			rowCount = mapper.createQuery(merge)
					.setString('customerName', 'Albert Chan')
					.executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: '2 Orders were archived'
		rowCount == 2
	}
	
	def 'Test MERGE UPDATE'() {
		given: '4 Orders'
		Connection conn = null
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new Customer('Albert Chan'))
			mapper.save(conn, new Store('Dental'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('80.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('90.00'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('1655.00'), DateUtils.newDate(2017, 3, 17, 9, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('5.59'), DateUtils.newDate(2017, 3, 24, 9, 30)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: '2 ArchivedOrders (2 of 4 Orders) in the database that have incorrect sales_amount and order_datetime'
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new ArchivedOrder(1L, 1L, 1L, new BigDecimal('1700.00'), LocalDateTime.of(2017, 3, 18, 9, 0)))
			mapper.save(conn, new ArchivedOrder(2L, 1L, 1L, new BigDecimal('6.00'), LocalDateTime.of(2017, 3, 25, 9, 30)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Merge object that updates sales_amount and order_datetime in matching ArchivedOrders'
		Merge merge = MERGE_ARCHIVED_ORDERS_FROM_ORDERS.mutable()
		.whenMatchedThen()
		.update(
			new Update().set(new SetValues()
				.add(new SetValue().tableAlias('arc').column('sales_amount').tableAlias('ord').column('sales_amount')
				).add(new SetValue().tableAlias('arc').column('order_datetime').tableAlias('ord').column('order_datetime')
				)
			)
		)
		
		when: 'the ArchivedOrders are updated'
		int rowCount = -1
		try {
			conn = HSQLDB.getConnection()
			rowCount = mapper.createQuery(merge)
					.setString('customerName', 'Albert Chan')
					.executeUpdate(conn)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: '2 ArchivedOrders were updated'
		rowCount == 2
	}
}
