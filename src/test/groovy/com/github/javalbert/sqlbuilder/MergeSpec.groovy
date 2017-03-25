package com.github.javalbert.sqlbuilder

import java.sql.Connection

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
	
	def 'Test MERGE inserting'() {
		given: '4 Orders'
		Connection conn = null
		try {
			conn = HSQLDB.getConnection()
			mapper.save(conn, new Customer('Albert Chan'))
			mapper.save(conn, new Store('Dental'))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('80.0'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('90.0'), DateUtils.newDate(2017, 3, 3, 16, 20)))
			mapper.save(conn, new Order(1L, 1L, new BigDecimal('1655.0'), DateUtils.newDate(2017, 3, 17, 9, 20)))
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
		Merge merge = new Merge().into('ArchivedOrders').as('arc')
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
		).whenNotMatchedThen()
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
}
