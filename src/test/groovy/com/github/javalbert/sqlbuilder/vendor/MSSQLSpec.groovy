package com.github.javalbert.sqlbuilder.vendor

import com.github.javalbert.sqlbuilder.Expression
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.Predicate
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.Where

import spock.lang.Specification

class MSSQLSpec extends Specification {
	private Vendor vendor
	
	def setup() {
		vendor = new MSSQL()
	}
	
	def 'Print SELECT statement with BOOLEAN literals TRUE and FALSE, as 1 and 0 respectively'() {
		given: 'Select object representing SQL string "SELECT * FROM tbl t WHERE t.col1 = TRUE AND t.col2 = FALSE"'
		Select select = new Select().list(new SelectList().column('*')
		).from(new From().tableName('tbl').as('t')
		).where(new Where().predicate(
			new Predicate().tableAlias('t').column('col1').eq().literal(true)
			).and().predicate(
			new Predicate().tableAlias('t').column('col2').eq().literal(false)
			)
		)
		
		when: 'Select is printed'
		String sql = vendor.print(select)
		
		then: 'printed SQL is equal to "SELECT * FROM tbl t WHERE t.col1 = 1 AND t.col2 = 0"'
		sql == 'SELECT * FROM tbl t WHERE t.col1 = 1 AND t.col2 = 0'
	}
	
	def 'Print SELECT statement with string concatentation using plus (+) sign instead of double pipe (||)'() {
		given: "Select object representing SQL string \"SELECT p.first_name || ' ' || p.last_name AS full_name FROM Person p\""
		Select select = new Select().list(new SelectList()
			.expression(new Expression()
			.tableAlias('p').column('first_name')
			.concat().literal(' ')
			.concat().tableAlias('p').column('last_name')
			).as('full_name')
		).from(new From().tableName('Person').as('p')
		)
		
		when: 'Select is printed'
		String sql = vendor.print(select)
		
		then: "printed SQL is equal to \"SELECT p.first_name + ' ' + p.last_name AS full_name FROM Person p\""
		sql == "SELECT p.first_name + ' ' + p.last_name AS full_name FROM Person p"
	}
}
