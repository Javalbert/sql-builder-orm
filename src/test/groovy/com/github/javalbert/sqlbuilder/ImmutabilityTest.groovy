package com.github.javalbert.sqlbuilder

import spock.lang.Specification
import spock.lang.Unroll

class ImmutabilityTest extends Specification {
	@Unroll('Call Case.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Case object'() {
		given: 'Case object'
		Case sqlCase = new Case().immutable()
		
		when: 'a Case method is called'
		caseMethod(sqlCase)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Case methods can throw exception'
		methodName	|	caseMethod
		'setAlias'	|	{ Case c -> c.setAlias('') }
		'and'	|	{ Case c -> c.and() }
		'append(String)'	|	{ Case c -> c.append('') }
		'append(String, boolean)'	|	{ Case c -> c.append('', false) }
		'column'	|	{ Case c -> c.column('') }
		'condition'	|	{ Case c -> c.condition(new Condition()) }
		'end'	|	{ Case c -> c.end() }
		'expression'	|	{ Case c -> c.expression(new Expression()) }
		'function'	|	{ Case c -> c.function(new Function('')) }
		'group'	|	{ Case c -> c.group(new Condition()) }
		'ifElse'	|	{ Case c-> c.ifElse() }
		'literal(Boolean)'	|	{ Case c -> c.literal(false) }
		'literal(Number)'	|	{ Case c -> c.literal(0) }
		'literal(String)'	|	{ Case c -> c.literal('') }
		'literalNull'	|	{ Case c -> c.literalNull() }
		'param'	|	{ Case c -> c.param('') }
		'predicate'	|	{ Case c -> c.predicate(new Predicate()) }
		'or'	|	{ Case c -> c.or() }
		'sqlCase'	|	{ Case c -> c.sqlCase(new Case()) }
		'subquery'	|	{ Case c -> c.subquery(new Select()) }
		'tableAlias'	|	{ Case c -> c.tableAlias('') }
		'tableName'	|	{ Case c -> c.tableName('') }
		'then'	|	{ Case c -> c.then() }
		'when'	|	{ Case c -> c.when() }
	}
	
	@Unroll('Call Column.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Column object'() {
		given: 'Column object'
		Column column = new Column().immutable()
		
		when: 'a Column method is called'
		columnMethod(column)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Column methods can throw exception'
		methodName	|	columnMethod
		'setAlias'	|	{ Column c -> c.setAlias('') }
		'setName'	|	{ Column c -> c.setName('') }
		'setPrefix'	|	{ Column c -> c.setPrefix(Prefix.TABLE_ALIAS) }
		'setPrefixValue'	|	{ Column c -> c.setPrefixValue('') }
	}
	
	@Unroll('Call ColumnList.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable ColumnList object'() {
		given: 'ColumnList object'
		ColumnList list = new ColumnList().immutable()
		
		when: 'a ColumnList method is called'
		columnListMethod(list)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Column methods can throw exception'
		methodName	|	columnListMethod
		'column'	|	{ ColumnList c -> c.column('') }
		'tableAlias'	|	{ ColumnList c -> c.tableAlias('') }
		'tableName'	|	{ ColumnList c -> c.tableName('') }
	}
	
	@Unroll('Call ColumnValues.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable ColumnValues object'() {
		given: 'ColumnValues object'
		ColumnValues values = new ColumnValues().immutable()
		
		when: 'a ColumnValues method is called'
		columnValuesMethod(values)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the ColumnValues methods can throw exception'
		methodName	|	columnValuesMethod
		'append(String)'	|	{ ColumnValues c -> c.append('') }
		'append(String, boolean)'	|	{ ColumnValues c -> c.append('', false) }
		'column'	|	{ ColumnValues c -> c.column('') }
		'expression'	|	{ ColumnValues c -> c.expression(new Expression()) }
		'function'	|	{ ColumnValues c -> c.function(new Function('')) }
		'literal(Boolean)'	|	{ ColumnValues c -> c.literal(false) }
		'literal(Number)'	|	{ ColumnValues c -> c.literal(0) }
		'literal(String)'	|	{ ColumnValues c -> c.literal('') }
		'literalNull'	|	{ ColumnValues c -> c.literalNull() }
		'param'	|	{ ColumnValues c -> c.param('') }
		'sqlCase'	|	{ ColumnValues c -> c.sqlCase(new Case()) }
		'sqlDefault'	|	{ ColumnValues c -> c.sqlDefault() }
		'subquery'	|	{ ColumnValues c -> c.subquery(new Select()) }
		'tableAlias'	|	{ ColumnValues c -> c.tableAlias('') }
		'tableName'	|	{ ColumnValues c -> c.tableName('') }
	}
	
	@Unroll('Call CommonTableExpression.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable CommonTableExpression object'() {
		given: 'CommonTableExpression object'
		CommonTableExpression cte = new CommonTableExpression('').immutable()
		
		when: 'a CommonTableExpression method is called'
		cteMethod(cte)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the CommonTableExpression methods can throw exception'
		methodName	|	cteMethod
		'setColumns'	|	{ CommonTableExpression c -> c.setColumns(Collections.EMPTY_LIST) }
		'setName'	|	{ CommonTableExpression c -> c.setName('') }
		'setSelect'	|	{ CommonTableExpression c -> c.setSelect(new Select()) }
	}
	
	@Unroll('Call Condition.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Condition object'() {
		given: 'Condition object'
		Condition condition = new Condition().immutable()
		
		when: 'a Condition method is called'
		conditionMethod(condition)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Condition methods can throw exception'
		methodName	|	conditionMethod
		'and'	|	{ Condition c -> c.and() }
		'group'	|	{ Condition c -> c.group(new Condition()) }
		'predicate'	|	{ Condition c -> c.predicate(new Predicate()) }
		'or'	|	{ Condition c -> c.or() }
	}
	
	@Unroll('Call Delete.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Delete object'() {
		given: 'Delete object'
		Delete delete = new Delete().immutable()
		
		when: 'a Delete method is called'
		deleteMethod(delete)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Condition methods can throw exception'
		methodName	|	deleteMethod
		'tableName'	|	{ Delete d -> d.tableName('') }
		'where'	|	{ Delete d -> d.where(new Where()) }
		'with'	|	{ Delete d -> d.with(new With()) }
	}
	
	@Unroll('Call Expression.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Expression object'() {
		given: 'Expression object'
		Expression expression = new Expression().immutable()
		
		when: 'a Expression method is called'
		expressionMethod(expression)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Expression methods can throw exception'
		methodName	|	expressionMethod
		'append(String)'	|	{ Expression e -> e.append('') }
		'append(String, boolean)'	|	{ Expression e -> e.append('', false) }
		'column'	|	{ Expression e -> e.column('') }
		'concat'	|	{ Expression e -> e.concat() }
		'divide'	|	{ Expression e -> e.divide() }
		'expression'	|	{ Expression e -> e.expression(new Expression()) }
		'function'	|	{ Expression e -> e.function(new Function('')) }
		'literal(Boolean)'	|	{ Expression e -> e.literal(false) }
		'literal(Number)'	|	{ Expression e -> e.literal(0) }
		'literal(String)'	|	{ Expression e -> e.literal('') }
		'literalNull'	|	{ Expression e -> e.literalNull() }
		'minus'	|	{ Expression e -> e.minus() }
		'mod'	|	{ Expression e -> e.mod() }
		'multiply'	|	{ Expression e -> e.multiply() }
		'param'	|	{ Expression e -> e.param('') }
		'plus'	|	{ Expression e -> e.plus() }
		'setAlias'	|	{ Expression e -> e.setAlias('') }
		'sqlCase'	|	{ Expression e -> e.sqlCase(new Case()) }
		'subquery'	|	{ Expression e -> e.subquery(new Select()) }
		'tableAlias'	|	{ Expression e -> e.tableAlias('') }
		'tableName'	|	{ Expression e -> e.tableName('') }
	}
	
	@Unroll('Call Fetch.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Fetch object'() {
		given: 'Fetch object'
		Fetch fetch = new Fetch(0).immutable()
		
		when: 'a Fetch method is called'
		fetchMethod(fetch)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Fetch methods can throw exception'
		methodName	|	fetchMethod
		'setFetchCount'	|	{ Fetch f -> f.setFetchCount(1) }
	}
	
	@Unroll('Call From.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable From object'() {
		given: 'From object'
		From from = new From().tableName('').immutable()
		
		when: 'a From method is called'
		fromMethod(from)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the From methods can throw exception'
		methodName	|	fromMethod
		'append'	|	{ From f -> f.append('') }
		'as'	|	{ From f -> f.as('') }
		'fullOuterJoin'	|	{ From f -> f.fullOuterJoin() }
		'inlineView'	|	{ From f -> f.inlineView(new Select()) }
		'innerJoin'	|	{ From f -> f.innerJoin() }
		'leftOuterJoin'	|	{ From f -> f.leftOuterJoin() }
		'leftParens'	|	{ From f -> f.leftParens() }
		'on'	|	{ From f -> f.on(new Condition()) }
		'rightOuterJoin'	|	{ From f -> f.rightOuterJoin() }
		'rightParens'	|	{ From f -> f.rightParens() }
		'tableAlias'	|	{ From f -> f.tableAlias('') }
		'tableName'	|	{ From f -> f.tableName('') }
	}
	
	@Unroll('Call Function.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Function object'() {
		given: 'Function object'
		Function function = new Function('').immutable()
		
		when: 'a Function method is called'
		functionMethod(function)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Function methods can throw exception'
		methodName	|	functionMethod
		'append(String)'	|	{ Function f -> f.append('') }
		'append(String, boolean)'	|	{ Function f -> f.append('', false) }
		'column'	|	{ Function f -> f.column('') }
		'expression'	|	{ Function f -> f.expression(new Expression()) }
		'function'	|	{ Function f -> f.function(new Function('')) }
		'literal(Boolean)'	|	{ Function f -> f.literal(false) }
		'literal(Number)'	|	{ Function f -> f.literal(0) }
		'literal(String)'	|	{ Function f -> f.literal('') }
		'literalNull'	|	{ Function f -> f.literalNull() }
		'param'	|	{ Function f -> f.param('') }
		'setAlias'	|	{ Function f -> f.setAlias('') }
		'setMaxArguments'	|	{ Function f -> f.setMaxArguments(0) }
		'setName'	|	{ Function f -> f.setName('') }
		'sqlCase'	|	{ Function f -> f.sqlCase(new Case()) }
		'subquery'	|	{ Function f -> f.subquery(new Select()) }
		'tableAlias'	|	{ Function f -> f.tableAlias('') }
		'tableName'	|	{ Function f -> f.tableName('') }
	}
	
	@Unroll('Call GroupBy.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable GroupBy object'() {
		given: 'GroupBy object'
		GroupBy groupBy = new GroupBy().immutable()
		
		when: 'a GroupBy method is called'
		groupByMethod(groupBy)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the GroupBy methods can throw exception'
		methodName	|	groupByMethod
		'column'	|	{ GroupBy g -> g.column('') }
		'tableAlias'	|	{ GroupBy g -> g.tableAlias('') }
		'tableName'	|	{ GroupBy g -> g.tableName('') }
	}
	
	@Unroll('Call Having.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Having object'() {
		given: 'Having object'
		Having having = new Having().immutable()
		
		when: 'a Having method is called'
		havingMethod(having)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Having methods can throw exception'
		methodName	|	havingMethod
		'and'	|	{ Having h -> h.and() }
		'group'	|	{ Having h -> h.group(new Condition()) }
		'predicate'	|	{ Having h -> h.predicate(new Predicate()) }
		'or'	|	{ Having h -> h.or() }
	}
	
	@Unroll('Call Insert.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable Insert object'() {
		given: 'Insert object'
		Insert insert = new Insert().immutable()
		
		when: 'a Insert method is called'
		insertMethod(insert)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the Insert methods can throw exception'
		methodName	|	insertMethod
		'columns'	|	{ Insert i -> i.columns(new ColumnList()) }
		'into'	|	{ Insert i -> i.into('') }
		'values'	|	{ Insert i -> i.values(new ColumnValues()) }
		'subselect'	|	{ Insert i -> i.subselect(new Select()) }
		'with'	|	{ Insert i -> i.with(new With()) }
	}
	
	@Unroll('Call InValues.#methodName method and throw exception')
	def 'Throw error when trying to modify immutable InValues object'() {
		given: 'InValues object'
		InValues values = new InValues().immutable()
		
		when: 'a InValues method is called'
		inValuesMethod(values)
		
		then: 'exception is thrown'
		thrown(UnsupportedOperationException)
		
		where: 'each of the InValues methods can throw exception'
		methodName	|	inValuesMethod
		'append(String)'	|	{ InValues i -> i.append('') }
		'append(String, boolean)'	|	{ InValues i -> i.append('', false) }
		'bigDecimals'	|	{ InValues i -> i.bigDecimals(Arrays.asList(new BigDecimal('0.0'))) }
		'column'	|	{ InValues i -> i.column('') }
		'doubles'	|	{ InValues i -> i.doubles(Arrays.asList(0.0d)) }
		'expression'	|	{ InValues i -> i.expression(new Expression()) }
		'floats'	|	{ InValues i -> i.floats(Arrays.asList(0.0f)) }
		'function'	|	{ InValues i -> i.function(new Function('')) }
		'integers'	|	{ InValues i -> i.integers(Arrays.asList(0)) }
		'literal(Boolean)'	|	{ InValues i -> i.literal(false) }
		'literal(Number)'	|	{ InValues i -> i.literal(0) }
		'literal(String)'	|	{ InValues i -> i.literal('') }
		'literalNull'	|	{ InValues i -> i.literalNull() }
		'longs'	|	{ InValues i -> i.longs(Arrays.asList(0L)) }
		'param'	|	{ InValues i -> i.param('') }
		'sqlCase'	|	{ InValues i -> i.sqlCase(new Case()) }
		'strings'	|	{ InValues i -> i.strings(Arrays.asList('')) }
		'subquery'	|	{ InValues i -> i.subquery(new Select()) }
		'tableAlias'	|	{ InValues i -> i.tableAlias('') }
		'tableName'	|	{ InValues i -> i.tableName('') }
	}
}
