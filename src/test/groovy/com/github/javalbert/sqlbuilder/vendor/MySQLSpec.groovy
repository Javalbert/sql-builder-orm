package com.github.javalbert.sqlbuilder.vendor

import com.github.javalbert.sqlbuilder.Expression
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList

import spock.lang.Specification

class MySQLSpec extends Specification {
	private Vendor vendor
	
	def setup() {
		vendor = new MySQL()
	}
	
	def "Print SELECT statement with LIMIT OFFSET syntax"() {
		given: 'Select object representing SQL string "SELECT * FROM tbl LIMIT 5 OFFSET 20"'
		Select select = new Select().list(new SelectList().column('*')
		).from(new From().tableName('tbl')
		).offset(20)
		.fetch(5)
		
		when: 'Select is printed'
		String sql = vendor.print(select)
		
		then: 'printed SQL is equal to "SELECT * FROM tbl LIMIT 5 OFFSET 20"'
		sql == 'SELECT * FROM tbl LIMIT 5 OFFSET 20'
	}
	
	def 'Print SELECT statement with string concatenation that uses CONCAT() function'() {
		given: "Select object in ANSI SQL \"SELECT t.col1 || ' ' || (SELECT col2 FROM tbl2) FROM tbl t\""
		Select select = new Select().list(new SelectList()
			.expression(new Expression()
				.tableAlias('t').column('col1')
				.concat().literal(' ')
				.concat().subquery(
					new Select().list(new SelectList()
					.column('col2')
					).from(new From().tableName('tbl2')
					)
				)
			)
		).from(new From().tableName('tbl').as('t')
		)
		
		when: 'Select is printed'
		String sql = vendor.print(select)
		
		then: "printed SQL is equal to \"SELECT CONCAT(t.col1, ' ', (SELECT col2 FROM tbl2)) FROM tbl t\""
		sql == "SELECT CONCAT(t.col1, ' ', (SELECT col2 FROM tbl2)) FROM tbl t"
	}
}
