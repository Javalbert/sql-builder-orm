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
	
	@Unroll('Case.#methodName returns new immutable instance')
	def 'Test immutability of Case methods'() {
		given: 'Case object'
		Case sqlCase0 = initialMethod()
		
		when: 'a Case method is called'
		Case sqlCase1 = method(sqlCase0)
		
		then: 'the method returned a new Case instance'
		sqlCase1.is(sqlCase0) == false
		
		where: 'different Case methods'
		methodName	|	initialMethod	|	method
		'as'	|	{ -> sqlCase() }	|	{ Case c -> c.as('') }
		'ifElse'	|	{ -> sqlCase().when(f.bar.eq(3)).then('Three') }	|	{ Case c -> c.ifElse('NaN') }
		'then'	|	{ -> sqlCase().when(f.bar.eq(3)) }	|	{ Case c -> c.then('Three') }
		'when'	|	{ -> sqlCase() }	|	{ Case c -> c.when(f.bar.eq(3)) }
	}
	
	@Unroll('CommonTableExpression.#methodName returns new immutable instance')
	def 'Test immutability of CommonTableExpression methods'() {
		given: 'CommonTableExpression object'
		CommonTableExpression cte = with(Foo)
		
		when: 'a CommonTableExpression method is called'
		CommonTableExpression cte2 = method(cte)
		
		then: 'the method returned a new CommonTableExpression instance'
		cte2.is(cte) == false
		
		where: 'different CommonTableExpression methods'
		methodName	|	method
		'as'	|	{ CommonTableExpression c -> c.as(select()) }
		'columns'	|	{ CommonTableExpression c -> c.columns(f.bar) }
	}
	
	def 'DSL.group(Condition) returns new immutable instance'() {
		given: 'Condition object'
		Condition condition = f.bar.isNotNull().and(f.bar.noteq(''))
		
		when: 'DSL.group() method is called with the Condition object'
		Condition condition2 = group(condition)
		
		then: 'the method returned a new Condition instance'
		condition2.is(condition) == false
	}
	
	@Unroll('CteList.#methodName returns new immutable instance')
	def 'Test immutability of CteList methods'() {
		given: 'CteList object'
		CteList cteList = initialMethod()
		
		when: 'a CteList method is called'
		CteList cteList2 = method(cteList)
		
		then: 'the method returned a new CteList instance'
		cteList2.is(cteList) == false
		
		where: 'different CteList methods'
		methodName	|	initialMethod	|	method
		'as'	|	{ -> with(Foo).as(select()).with(Foo) }	|	{ CteList c -> c.as(select()) }
		'columns'	|	{ -> with(Foo).as(select()).with(Foo) }	|	{ CteList c -> c.columns(f.bar) }
		'with'	|	{ -> with(Foo).as(select()).with(Foo).as(select()) }	|	{ CteList c -> c.with(Foo) }
	}
	
	@Unroll('DeleteStatement.#methodName returns new immutable instance')
	def 'Test immutability of DeleteStatement methods'() {
		given: 'DeleteStatement object'
		DeleteStatement stmt = delete(Foo)
		
		when: 'a DeleteStatement method is called'
		DeleteStatement stmt2 = method(stmt)
		
		then: 'the method returned a new DeleteStatement instance'
		stmt2.is(stmt) == false
		
		where: 'different DeleteStatement methods'
		methodName	|	method
		'where'	|	{ DeleteStatement d -> d.where(f.bar.eq(1)) }
		'with(CommonTableExpression)'	|	{ DeleteStatement d -> d.with(with(Foo)) }
		'with(CteList)'	|	{ DeleteStatement d -> d.with(with(Foo).as(select()).with(Foo)) }
	}
	
	@Unroll('Expression.#methodName returns new immutable instance')
	def 'Test immutability of Expression methods'() {
		given: 'Expression object'
		Expression exp = f.bar.concat('2000')
		
		when: 'a Expression method is called'
		Expression exp2 = method(exp)
		
		then: 'the method returned a new Expression instance'
		exp2.is(exp) == false
		
		where: 'different Expression methods'
		methodName	|	method
		'as'	|	{ Expression e -> e.as('') }
		'concat'	|	{ Expression e -> e.concat('') }
		'divide(Number)'	|	{ Expression e -> e.divide(2) }
		'divide(ExpressionBuilder)'	|	{ Expression e -> e.divide(f.bar.divide(2)) }
		'minus(Number)'	|	{ Expression e -> e.minus(2) }
		'minus(ExpressionBuilder)'	|	{ Expression e -> e.minus(f.bar.divide(2)) }
		'mod(Number)'	|	{ Expression e -> e.mod(2) }
		'mod(ExpressionBuilder)'	|	{ Expression e -> e.mod(f.bar.divide(2)) }
		'multiply(Number)'	|	{ Expression e -> e.multiply(2) }
		'multiply(ExpressionBuilder)'	|	{ Expression e -> e.multiply(f.bar.divide(2)) }
		'plus(Number)'	|	{ Expression e -> e.plus(2) }
		'plus(ExpressionBuilder)'	|	{ Expression e -> e.plus(f.bar.divide(2)) }
	}
	
	@Unroll('Function.#methodName returns new immutable instance')
	def 'Test immutability of Function methods'() {
		given: 'Function object'
		Function func0 = new Function('Foo')
		
		when: 'a Function method is called'
		Function func1 = method(func0)
		
		then: 'the method returned a new Function instance'
		func1.is(func0) == false
		
		where: 'different Function methods'
		methodName	|	method
		'as'	|	{ Function func -> func.as('') }
		'call()'	|	{ Function func -> func.call() }
		'call(ValueExpression)'	|	{ Function func -> func.call(literal('bar')) }
		'call(ValueExpression...)'	|	{ Function func -> func.call(literal('bar'), literal(2000)) }
	}
	
	@Unroll('InsertStatement.#methodName returns new immutable instance')
	def 'Test immutability of InsertStatement methods'() {
		given: 'InsertStatement object'
		InsertStatement stmt = insert(Foo)
		
		when: 'a InsertStatement method is called'
		InsertStatement stmt2 = method(stmt)
		
		then: 'the method returned a new InsertStatement instance'
		stmt2.is(stmt) == false
		
		where: 'different InsertStatement methods'
		methodName	|	method
		'columns'	|	{ InsertStatement i -> i.columns(f.bar) }
		'subselect'	|	{ InsertStatement i -> i.subselect(select()) }
		'values'	|	{ InsertStatement i -> i.values(literal('bar'), literal(2000)) }
		'with(CommonTableExpression)'	|	{ InsertStatement i -> i.with(with(Foo)) }
		'with(CteList)'	|	{ InsertStatement i -> i.with(with(Foo).as(select()).with(Foo)) }
	}
	
	@Unroll('JoinedTable.#methodName returns new immutable instance')
	def 'Test immutability of JoinedTable methods'() {
		given: 'JoinedTable object'
		JoinedTable tbl = Foo.innerJoin(Foo)
		
		when: 'a JoinedTable method is called'
		JoinedTable tbl2 = method(tbl)
		
		then: 'the method returned a new JoinedTable instance'
		tbl2.is(tbl) == false
		
		where: 'different JoinedTable methods'
		methodName	|	method
		'on'	|	{ JoinedTable j -> j.on(f.bar.eq(f.bar)) }
	}
	
	def 'DSL.nest(JoinedTable) returns new immutable instance'() {
		given: 'JoinedTable object'
		JoinedTable tbl = Foo.leftOuterJoin(Foo)
		
		when: 'DSL.nest() method is called with the JoinedTable object'
		JoinedTable tbl2 = nest(tbl)
		
		then: 'the method returned a new JoinedTable instance'
		tbl2.is(tbl) == false
	}
	
	@Unroll('LiteralBoolean.#methodName returns new immutable instance')
	def 'Test immutability of LiteralBoolean methods'() {
		given: 'LiteralBoolean object'
		LiteralBoolean val = literal(true)
		
		when: 'a LiteralBoolean method is called'
		LiteralBoolean val2 = method(val)
		
		then: 'the method returned a new LiteralBoolean instance'
		val2.is(val) == false
		
		where: 'different LiteralBoolean methods'
		methodName	|	method
		'as'	|	{ LiteralBoolean l -> l.as('Literal') }
	}
	
	@Unroll('LiteralNull.#methodName returns new immutable instance')
	def 'Test immutability of LiteralNull methods'() {
		given: 'LiteralNull object'
		LiteralNull val = literalNull()
		
		when: 'a LiteralNull method is called'
		LiteralNull val2 = method(val)
		
		then: 'the method returned a new LiteralNull instance'
		val2.is(val) == false
		
		where: 'different LiteralNull methods'
		methodName	|	method
		'as'	|	{ LiteralNull l -> l.as('Literal') }
	}
	
	@Unroll('LiteralNumber.#methodName returns new immutable instance')
	def 'Test immutability of LiteralNumber methods'() {
		given: 'LiteralNumber object'
		LiteralNumber val = literal(2000)
		
		when: 'a LiteralNumber method is called'
		LiteralNumber val2 = method(val)
		
		then: 'the method returned a new LiteralNumber instance'
		val2.is(val) == false
		
		where: 'different LiteralNumber methods'
		methodName	|	method
		'as'	|	{ LiteralNumber l -> l.as('Literal') }
	}
	
	@Unroll('LiteralString.#methodName returns new immutable instance')
	def 'Test immutability of LiteralString methods'() {
		given: 'LiteralString object'
		LiteralString val = literal('bar')
		
		when: 'a LiteralString method is called'
		LiteralString val2 = method(val)
		
		then: 'the method returned a new LiteralString instance'
		val2.is(val) == false
		
		where: 'different LiteralString methods'
		methodName	|	method
		'as'	|	{ LiteralString l -> l.as('Literal') }
	}
	
	@Unroll('MergeStatement.#methodName returns new immutable instance')
	def 'Test immutability of MergeStatement methods'() {
		given: 'MergeStatement object'
		MergeStatement stmt = initialMethod()
		
		when: 'a MergeStatement method is called'
		MergeStatement stmt2 = method(stmt)
		
		then: 'the method returned a new MergeStatement instance'
		stmt2.is(stmt) == false
		
		where: 'different MergeStatement methods'
		methodName	|	initialMethod	|	method
		'on'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.on(f.bar.eq(f.bar)) }
		'then(InsertStatement)'	|	{ -> merge(Foo).whenNotMatched() }	|	{ MergeStatement m -> m.then(insert()) }
		'then(UpdateStatement)'	|	{ -> merge(Foo).whenMatched() }	|	{ MergeStatement m -> m.then(update()) }
		'thenDelete'	|	{ -> merge(Foo).whenMatched() }	|	{ MergeStatement m -> m.thenDelete() }
		'using(SelectStatement)'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.using(select()) }
		'using(Table)'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.using(Foo) }
		'whenMatched'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.whenMatched() }
		'whenMatchedAnd'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.whenMatchedAnd(f.bar.gt(1)) }
		'whenNotMatched'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.whenNotMatched() }
		'whenNotMatchedAnd'	|	{ -> merge(Foo) }	|	{ MergeStatement m -> m.whenNotMatchedAnd(f.bar.gt(1)) }
	}
	
	@Unroll('OrderByColumn.#methodName returns new immutable instance')
	def 'Test immutability of OrderByColumn methods'() {
		given: 'OrderByColumn object'
		OrderByColumn col = f.bar
		
		when: 'a OrderByColumn method is called'
		OrderByColumn col2 = method(col)
		
		then: 'the method returned a new OrderByColumn instance'
		col2.is(col) == false
		
		where: 'different OrderByColumn methods'
		methodName	|	method
		'asc'	|	{ OrderByColumn o -> o.asc() }
		'desc'	|	{ OrderByColumn o -> o.desc() }
	}
	
	// Predicand methods returns Predicate objects, which are not Predicands
//	@Unroll('Predicand.#methodName returns new immutable instance')
//	def 'Test immutability of Predicand methods'() {
//		given: 'Predicand object'
//		Predicand pred = f.bar
//		
//		when: 'a Predicand method is called'
//		Predicand pred2 = method(pred)
//		
//		then: 'the method returned a new Predicand instance'
//		pred2.is(pred) == false
//		
//		where: 'different Predicand methods'
//		methodName	|	method
//		'between'	|	{ Predicand p -> p.between(1, 3) }
//		'eq'	|	{ Predicand p -> p.eq(1) }
//		'gt'	|	{ Predicand p -> p.gt(1) }
//		'gteq'	|	{ Predicand p -> p.gteq(1) }
//		'in'	|	{ Predicand p -> p.in(l, 2, 3) }
//		'isNotNull'	|	{ Predicand p -> p.isNotNull() }
//		'isNull'	|	{ Predicand p -> p.isNull() }
//		'like'	|	{ Predicand p -> p.like('') }
//		'lt'	|	{ Predicand p -> p.lt(1) }
//		'lteq'	|	{ Predicand p -> p.lteq(1) }
//		'notBetween'	|	{ Predicand p -> p.notBetween(1, 3) }
//		'noteq'	|	{ Predicand p -> p.noteq(1) }
//		'notIn'	|	{ Predicand p -> p.notIn(l, 2, 3) }
//		'notLike'	|	{ Predicand p -> p.notLike('') }
//	}
	
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
	
	@Unroll('Table.#methodName returns new immutable instance')
	def 'Test immutability of Table methods'() {
		given: 'Table object'
		Table tbl = Foo
		
		when: 'a Table method is called'
		Table tbl2 = method(tbl)
		
		then: 'the method returned a new Table instance'
		tbl2.is(tbl) == false
		
		where: 'different Table methods'
		methodName	|	method
		'as'	|	{ Table t -> t.as(f) }
	}
	
	@Unroll('TableColumn.#methodName returns new immutable instance')
	def 'Test immutability of TableColumn methods'() {
		given: 'TableColumn object'
		TableColumn col = new TableColumn('bar')
		
		when: 'a TableColumn method is called'
		TableColumn col2 = method(col)
		
		then: 'the method returned a new TableColumn instance'
		col2.is(col) == false
		
		where: 'different Table methods'
		methodName	|	method
		'as'	|	{ TableColumn t -> t.as('') }
	}
	
	@Unroll('TableReference.#methodName returns new immutable instance')
	def 'Test immutability of TableReference methods'() {
		given: 'TableReference object'
		TableReference tbl = Foo
		
		when: 'a TableReference method is called'
		TableReference tbl2 = method(tbl)
		
		then: 'the method returned a new TableReference instance'
		tbl2.is(tbl) == false
		
		where: 'different TableReference methods'
		methodName	|	method
		'innerJoin'	|	{ TableReference t -> t.innerJoin(Foo) }
		'leftOuterJoin'	|	{ TableReference t -> t.leftOuterJoin(Foo) }
		'rightOuterJoin'	|	{ TableReference t -> t.rightOuterJoin(Foo) }
	}
	
	@Unroll('UpdateStatement.#methodName returns new immutable instance')
	def 'Test immutability of UpdateStatement methods'() {
		given: 'UpdateStatement object'
		UpdateStatement stmt = update(Foo)
		
		when: 'a UpdateStatement method is called'
		UpdateStatement stmt2 = method(stmt)
		
		then: 'the method returned a new UpdateStatement instance'
		stmt2.is(stmt) == false
		
		where: 'different UpdateStatement methods'
		methodName	|	method
		'set'	|	{ UpdateStatement u -> u.set(f.bar.to('')) }
		'where'	|	{ UpdateStatement u -> u.where(f.bar.eq(1)) }
		'with(CommonTableExpression)'	|	{ UpdateStatement u -> u.with(with(Foo)) }
		'with(CteList)'	|	{ UpdateStatement u -> u.with(with(Foo).as(select()).with(Foo)) }
	}
}
