package com.github.javalbert.sqlbuilder.vendor

import com.github.javalbert.sqlbuilder.Column
import com.github.javalbert.sqlbuilder.Condition
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.OrderBy
import com.github.javalbert.sqlbuilder.Predicate
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.parser.SqlParser

import spock.lang.Specification

class ANSISpec extends Specification {
	private SqlParser parser
	private Vendor vendor
	
	def setup() {
		parser = new SqlParser()
		vendor = new ANSI()
	}
	
	def 'Check string equality for SELECT statement with two nested JOIN groups'() {
		given: 'Select object representing the SQL string from the last example at http://sqlity.net/en/1435/a-join-a-day-nested-joins/'
		Select select = new Select().list(new SelectList()
			.tableAlias('pers').column('FirstName')
			.tableAlias('pers').column('LastName')
			.tableAlias('cust').column('AccountNumber')
			.tableAlias('soh').column('OrderDate')
			.tableAlias('sod').column('OrderQty')
			.tableAlias('sod').column('LineTotal')
			.tableAlias('prod').column('Name')
			.tableAlias('prod').column('ListPrice')
		).from(new From().tableName('Production.Product').as('prod')
			.innerJoin()
			.leftParens()
				.tableName('Sales.SalesOrderHeader').as('soh')
				.innerJoin().tableName('Sales.SalesOrderDetail').as('sod')
				.on(new Condition().predicate(new Predicate().tableAlias('soh').column('SalesOrderID').eq().tableAlias('sod').column('SalesOrderID')))
			.rightParens()
			.on(new Condition().predicate(new Predicate().tableAlias('sod').column('ProductID').eq().tableAlias('prod').column('ProductID')))
			.innerJoin()
			.leftParens()
				.tableName('Sales.Customer').as('cust')
				.innerJoin().tableName('Person.Person').as('pers')
				.on(new Condition().predicate(new Predicate().tableAlias('cust').column('PersonID').eq().tableAlias('pers').column('BusinessEntityID')))
			.rightParens()
			.on(new Condition().predicate(new Predicate().tableAlias('soh').column('CustomerID').eq().tableAlias('cust').column('CustomerID')))
		).orderBy(new OrderBy()
			.tableAlias('pers').column('BusinessEntityID')
			.tableAlias('soh').column('SalesOrderID')
			.tableAlias('sod').column('SalesOrderDetailID')
		)
		
		and: 'the SQL string parsed from Select object'
		String sql = vendor.print(select)
		
		when: 'SELECT statement is parsed and resulting Select object is printed to string'
		select = parser.parse(sql).sqlStatement
		String reprint = vendor.print(select)
		
		then: 'SQL strings match'
		sql == reprint
	}
	
	def 'Check multiple table references separated by commas in FROM clause, are separated'() {
		given: 'Select object with a FROM clause referencing a table, a joined table, and an inline view'
		Select select = new Select()
		.list(new SelectList()
			.tableAlias('a').column('a1')
		).from(new From()
			.tableName('TableA').as('a')
			.tableName('TableB').as('b').innerJoin().tableName('TableC').as('c').on(new Condition().predicate(
				new Predicate().tableAlias('b').column('id').eq().tableAlias('c').column('bid'))
			).inlineView(
				new Select().list(new SelectList()
					.column('*')
				).from(new From()
					.tableName('TableD')
				)
			).as('d')
		)
		
		when: 'the SQL string parsed from Select object'
		String sql = vendor.print(select)
		
		then: 'SQL string is equal to "SELECT a.a1 FROM TableA a, TableB b INNER JOIN TableC c ON b.id = c.bid, (SELECT * FROM TableD) AS d"'
		sql == 'SELECT a.a1 FROM TableA a, TableB b INNER JOIN TableC c ON b.id = c.bid, (SELECT * FROM TableD) AS d'
	}
}
