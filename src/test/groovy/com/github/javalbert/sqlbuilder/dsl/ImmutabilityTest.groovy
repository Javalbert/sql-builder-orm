package com.github.javalbert.sqlbuilder.dsl

import static com.github.javalbert.sqlbuilder.dsl.DSL.*

import com.github.javalbert.sqlbuilder.dsl.ImmutabilityTest.FooTableAlias

import spock.lang.Specification
import spock.lang.Unroll

class ImmutabilityTest extends Specification {
	private static final Table Foo = new Table("Foo");
	
	private static final FooTableAlias f = new FooTableAlias();
	
	public static class FooTableAlias extends TableAlias {
		public final TableColumn bar = of(new TableColumn("bar"));
		
		public FooTableAlias() {
			super("f");
		}
	}
	
	@Unroll('BooleanExpression.#methodName returns new immutable instance')
	def 'Test immutability of BooleanExpression methods'() {
		given: 'BooleanExpression object'
		BooleanExpression exp = f.bar.eq(1)
		
		when: 'a BooleanExpression method is called'
		BooleanExpression exp2 = method(exp)
		
		then: 'the method returned a new BooleanExpression instance'
		exp2.is(exp) == false
		
		where: 'different BooleanExpression methods'
		methodName	|	method
		'and'	|	{ BooleanExpression b -> b.and(f.bar.eq(1)) }
		'or'	|	{ BooleanExpression b -> b.or(f.bar.eq(2)) }
	}
	
	@Unroll('SelectStatement.#methodName returns new immutable instance')
	def 'Test immutability of SelectStatement methods'() {
		given: 'SelectStatement object'
		SelectStatement stmt = select()
		
		when: 'a SelectStatement method is called'
		SelectStatement stmt2 = method(stmt)
		
		then: 'the method returned a new SelectStatement instance'
		stmt2.is(stmt) == false
		
		where: 'different SelectStatement methods'
		methodName	|	method
		'as(String)'	|	{ SelectStatement s -> s.as('') }
		'as(TableAlias)'	|	{ SelectStatement s -> s.as(f) }
		'distinct'	|	{ SelectStatement s -> s.distinct(true) }
		'except'	|	{ SelectStatement s -> s.except(select()) }
		'from'	|	{ SelectStatement s -> s.from(Foo) }
		'groupBy'	|	{ SelectStatement s -> s.groupBy(f.bar) }
		'having'	|	{ SelectStatement s -> s.having(count(f.bar).gt(1)) }
		'intersect'	|	{ SelectStatement s -> s.intersect(select()) }
		'orderBy'	|	{ SelectStatement s -> s.orderBy(f.bar) }
		'setOperations'	|	{ SelectStatement s -> s.setOperations(unionAll(select())) }
		'union'	|	{ SelectStatement s -> s.union(select()) }
		'unionAll'	|	{ SelectStatement s -> s.unionAll(select()) }
		'where'	|	{ SelectStatement s -> s.where(f.bar.eq(true)) }
		'with(CommonTableExpression)'	|	{ SelectStatement s -> s.with(with(Foo)) }
		'with(CteList)'	|	{ SelectStatement s -> s.with(with(Foo).as(select()).with(Foo)) }
	}
}
