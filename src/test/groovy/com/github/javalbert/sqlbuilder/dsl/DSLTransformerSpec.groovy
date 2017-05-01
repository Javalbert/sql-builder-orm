package com.github.javalbert.sqlbuilder.dsl

import static com.github.javalbert.sqlbuilder.dsl.DSL.*

import com.github.javalbert.sqlbuilder.ArithmeticOperator
import com.github.javalbert.sqlbuilder.ColumnList
import com.github.javalbert.sqlbuilder.ColumnValues
import com.github.javalbert.sqlbuilder.Delete
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.InValues
import com.github.javalbert.sqlbuilder.Insert
import com.github.javalbert.sqlbuilder.Join
import com.github.javalbert.sqlbuilder.Merge
import com.github.javalbert.sqlbuilder.OrderBy
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.SortType
import com.github.javalbert.sqlbuilder.Update
import com.github.javalbert.sqlbuilder.Where
import com.github.javalbert.sqlbuilder.With
import com.github.javalbert.sqlbuilder.vendor.ANSI

import spock.lang.Specification
import spock.lang.Unroll

class DSLTransformerSpec extends Specification {
	private static final Table Foo = new Table("Foo");
	
	private static final FooTableAlias f = new FooTableAlias();
	
	private static final TableColumn bar = new TableColumn("bar")
	
	public static class FooTableAlias extends TableAlias {
		public final TableColumn bar = of(new TableColumn("bar"));
		
		public FooTableAlias() {
			super("f");
		}
	}
	
	private DSLTransformer dslTransformer
	
	def 'setup'() {
		dslTransformer = new DSLTransformer()
	}
	
	def 'Transform dsl.DeleteStatement into sqlbuilder.Delete'() {
		given: 'dsl.DeleteStatement object'
		DeleteStatement stmt = delete(Foo)
				.where(f.bar.eq(1))
		
		when: 'building sqlbuilder.Delete from dsl.DeleteStatement'
		Delete delete = dslTransformer.buildDelete(stmt)
		
		then: 'Delete object was built'
		delete != null == true
	}
	
	def 'Transform dsl.InsertStatement into sqlbuilder.Insert, inserting with a column value'() {
		given: 'dsl.InsertStatement'
		InsertStatement stmt = insert(Foo)
				.columns(f.bar)
				.values(literal('foo'))
		
		when: 'building sqlbuilder.Insert from dsl.InsertStatement'
		Insert insert = dslTransformer.buildInsert(stmt)
		
		then: 'Insert object was built with column "bar" with value "foo"'
		ColumnList columns = insert.nodes[1]
		columns.nodes[0].name == 'bar'
		ColumnValues values = insert.nodes[2]
		values.nodes[0].value == 'foo'
	}
	
	def 'Transform dsl.InsertStatement into sqlbuilder.Insert, inserting with a subselect'() {
		given: 'dsl.InsertStatement'
		InsertStatement stmt = insert(Foo)
				.columns(f.bar)
				.subselect(
					select(f.bar)
					.from(Foo)
					)
		
		when: 'building sqlbuilder.Insert from dsl.InsertStatement'
		Insert insert = dslTransformer.buildInsert(stmt)
		
		then: 'Insert object was built with subselect'
		insert.nodes[2] instanceof Select
	}
	
	def 'Transform dsl.MergeStatement into sqlbuilder.Merge, with updates for matched and inserts when not matched'() {
		given: 'dsl.MergeStatement'
		MergeStatement stmt = merge(Foo)
				.using(Foo)
				.on(f.bar.eq(f.bar))
				.whenMatched()
				.then(update().set(f.bar.to(1)))
				.whenNotMatched()
				.then(insert().columns(f.bar).values(literal('foo')))
				
		when: 'building sqlbuilder.Merge from dsl.MergeStatement'
		Merge merge = dslTransformer.buildMerge(stmt)
		
		then: 'Merge object was built with Update and Insert objects'
		merge.nodes[5] instanceof Update
		merge.nodes[8] instanceof Insert
	}
	
	def 'Transform dsl.MergeStatement into sqlbuilder.Merge, with subquery as target table, a WHEN search condition for deletes'() {
		given: 'dsl.MergeStatement'
		MergeStatement stmt = merge(Foo)
				.using(
					select(f.bar)
					.from(Foo)
					)
				.on(f.bar.eq(f.bar))
				.whenMatchedAnd(f.bar.gt(1))
				.thenDelete()
				
		when: 'building sqlbuilder.Merge from dsl.MergeStatement'
		Merge merge = dslTransformer.buildMerge(stmt)
		
		then: 'Merge object was built with target table as a subquery and an optional search condition when deleting'
		merge.nodes[1] instanceof Select
		merge.nodes[4] instanceof com.github.javalbert.sqlbuilder.Condition
	}
	
	def 'Transform dsl.UpdateStatement into sqlbuilder.Update'() {
		given: 'dsl.DeleteStatement object'
		UpdateStatement stmt = update(Foo)
				.set(f.bar.to('foo'))
				.where(f.bar.eq(1))
		
		when: 'building sqlbuilder.Update from dsl.UpdateStatement'
		Update update = dslTransformer.buildUpdate(stmt)
		
		then: 'Update object was built'
		update != null == true
	}
	
	def 'Read BETWEEN predicate and verify sqlbuilder nodes'() {
		given: 'DeleteStatement with BETWEEN predicate'
		DeleteStatement stmt = delete(Foo)
				.where(f.bar.between(1, 2))
		
		when: 'Delete object is built and BETWEEN predicate is retrieved from WHERE clause'
		Delete delete = dslTransformer.buildDelete(stmt)
		// nodes[1] instanceof Where
		com.github.javalbert.sqlbuilder.Predicate between = delete.nodes[1].nodes[0]
		
		then: 'the f.bar should be between 1 and 2'
		between.nodes[2].value == 1
		between.nodes[4].value == 2
	}
	
	def 'Read "simple case" expression and verify sqlbuilder nodes'() {
		given: 'SelectStatement with CASE expression with f.bar as the simple expression'
		SelectStatement stmt = select(
			sqlCase(f.bar)
				.when(1).then('One')
				.when(2).then('Two')
				.ifElse('Zero')
			)
		
		when: 'Select is built and the Case is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Case caseExp = select.nodes[0].nodes[0]
		
		then: 'the simple expression being evaluated is column "bar"'
		caseExp.nodes[0].name == 'bar'
		
		and: 'WHEN conditions check for 1 and 2 numeric literals'
		caseExp.nodes[2].value == 1
		caseExp.nodes[6].value == 2
		
		and: 'THEN expressions return string literals "One" or "Two" respectively'
		caseExp.nodes[4].value == 'One'
		caseExp.nodes[8].value == 'Two'
		
		and: 'an ELSE expression returns "Zero"'
		caseExp.nodes[10].value == 'Zero'
	}
	
	def 'Read "searched case" expression and verify sqlbuilder nodes'() {
		given: 'SelectStatement with CASE expression with if-else-if boolean expression structure'
		SelectStatement stmt = select(
			sqlCase()
				.when(f.bar.gt(9)).then('> 9')
				.when(f.bar.lt(0)).then('< 1')
			)
		
		when: 'Select is built and the Case is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Case caseExp = select.nodes[0].nodes[0]
		
		then: 'the first Case node is the WHEN clause constant token, not a simple expression'
		caseExp.nodes[0] == com.github.javalbert.sqlbuilder.Case.WHEN
		
		and: 'the second Case node is a boolean expression'
		caseExp.nodes[1] instanceof com.github.javalbert.sqlbuilder.Condition
	}
	
	def 'Read EXISTS predicate and verify sqlbuilder nodes'() {
		given: 'DeleteStatement with EXISTS predicate'
		DeleteStatement stmt = delete(Foo)
				.where(exists(
					select()
					.from(Foo)
					.where(Foo.of(bar).gt(bar))
					))
		
		when: 'Delete is built and EXISTS Predicate object is retrieved'
		Delete delete = dslTransformer.buildDelete(stmt)
		com.github.javalbert.sqlbuilder.Predicate exists = delete.nodes[1].nodes[0]
		
		then: 'first node of predicate is PredicateOperator.EXISTS constant'
		exists.nodes[0] == com.github.javalbert.sqlbuilder.PredicateOperator.EXISTS
		
		and: 'second node is the subquery'
		exists.nodes[1] instanceof Select
	}
	
	def 'Read expression with string concatenation and verify sqlbuilder nodes'() {
		given: 'SelectStatement with string concatenation'
		SelectStatement stmt = select(
			f.bar.concat(' ').concat(f.bar)
			).from(Foo)
		
		when: 'Select is built and Expression is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Expression expression = select.nodes[0].nodes[0]
		
		then: 'first node and fifth nodes are column f.bar'
		expression.nodes[0].name == 'bar'
		expression.nodes[4].name == 'bar'
	}
	
	def 'Read expression with arithmetic operators and nested expressions'() {
		given: 'SelectStatement with arithmetic operations'
		SelectStatement stmt = select(
			f.bar.multiply(literal(1).plus(f.bar))
			).from(Foo)
		
		when: 'Select is built and Expression is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Expression expression = select.nodes[0].nodes[0]
		
		then: 'first node is f.bar and second node is the multiplication operator'
		expression.nodes[0].name == 'bar'
		expression.nodes[1] == ArithmeticOperator.MULTIPLY
		
		and: 'third node is a nested expression'
		expression.nodes[2] instanceof com.github.javalbert.sqlbuilder.Expression
	}
	
	def 'Read FROM clause and verify sqlbuilder nodes'() {
		given: 'SelectStatement'
		SelectStatement stmt = select(f.bar)
				.from(
					Foo,
					Foo.innerJoin(Foo).on(f.bar.eq(f.bar)),
					select(f.bar).as(f)
					)
		
		when: 'Select is built and From is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		From from = select.nodes[1]
		
		then: 'first node is table Foo'
		from.nodes[0].name == 'Foo'
		
		and: 'third node is an inner join'
		from.nodes[2] == Join.INNER_JOIN
		
		and: 'sixth node is an inline view'
		from.nodes[5] instanceof Select
	}
	
	def 'Read FROM clause with nested join syntax'() {
		given: 'SelectStatement'
		SelectStatement stmt = select(f.bar)
				.from(
					Foo.leftOuterJoin(Foo.innerJoin(Foo).on(f.bar.eq(f.bar))
						).on(f.bar.eq(f.bar))
					)
		
		when: 'Select is built and From is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		From from = select.nodes[1]
		
		then: 'third and eighth nodes are left and right parentheses respectively'
		from.nodes[2] == From.LEFT_PARENTHESIS
		from.nodes[7] == From.RIGHT_PARENTHESIS
	}
	
	def 'Read function and verify sqlbuilder nodes'() {
		given: 'A COALESCE() function'
		Function coalesce = new Function('COALESCE')
		
		and: 'SelectStatement which calls COALESCE() function'
		SelectStatement stmt = select(
			coalesce.call(f.bar, literal('Foobar'))
			).from(Foo)
		
		when: 'Select is built and sqlbuilder.Function is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Function function = select.nodes[0].nodes[0]
		
		then: "Function's name is 'COALESCE'"
		function.name == 'COALESCE'
		
		and: 'first parameter is column f.bar'
		function.nodes[0].prefixValue == 'f'
		function.nodes[0].name == 'bar'
		
		and: 'second parameter is string literal "Foobar"'
		function.nodes[1].value == 'Foobar'
	}
	
	def 'Read IN predicate and verify sqlbuilder nodes'() {
		given: 'DeleteStatement with IN predicate with string values'
		DeleteStatement stmt = delete(Foo)
		.where(bar.in('Started', 'Completed'))
		
		when: 'Delete is built and Predicate is retrieved from Condition'
		Delete delete = dslTransformer.buildDelete(stmt)
		com.github.javalbert.sqlbuilder.Predicate predicate = delete.nodes[1].nodes[0]
		
		then: 'second node in Predicate is IN keyword'
		predicate.nodes[1] == com.github.javalbert.sqlbuilder.PredicateOperator.IN
		
		and: 'and IN predicate contains values "Started" and "Completed"'
		InValues values = predicate.nodes[2]
		values.nodes[0].value == 'Started'
		values.nodes[1].value == 'Completed'
	}
	
	def 'Read NOT IN predicate with subquery'() {
		given: 'SelectStatement with NOT IN subquery'
		SelectStatement stmt = select(f.bar)
				.from(Foo)
				.where(f.bar.notIn(
					select(f.bar)
					.from(Foo)
					))
		
		when: 'Select is built and Predicate is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		com.github.javalbert.sqlbuilder.Predicate predicate = select.nodes[2].nodes[0]
		
		then: 'second node in Predicate is NOT IN keyword'
		predicate.nodes[1] == com.github.javalbert.sqlbuilder.PredicateOperator.NOT_IN
		
		and: 'third node is the subquery'
		predicate.nodes[2] instanceof Select
	}
	
	def 'Read ORDER BY and verify sqlbuilder nodes'() {
		given: 'SelectStatement with ORDER BY with TableColumn.asc() and ColumnAlias.desc()'
		SelectStatement stmt = select(f.bar)
			.from(Foo)
			.orderBy(f.bar.asc(),
				new ColumnAlias('Foobar').desc())
		
		when: 'Select is built and OrderBy is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		OrderBy orderBy = select.nodes[2]
		
		then: 'first node is table column "bar" in ascending order'
		orderBy.nodes[0].name == 'bar'
		orderBy.nodes[1] == SortType.ASC
		
		and: 'second node is column alias "Foobar" in ascending order'
		orderBy.nodes[2].alias == 'Foobar'
		orderBy.nodes[3] == SortType.DESC
	}
	
	@Unroll('Read "#predicateName" predicate producing operator #predicateOperator.token')
	def 'Read different predicates except for BETWEEN, EXISTS, and IN'() {
		given: 'SelectStatement'
		SelectStatement stmt = select(f.bar)
				.from(Foo)
				.where(
					method()
					)
		
		when: 'Select is built and Where retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		Where where = select.nodes[2]
		
		then: 'first predicate is ='
		where.nodes[0].nodes[1] == predicateOperator
		
		where: 'a predicate Groovy closure is called and produced predicate operator'
		predicateName	|	method	||	predicateOperator
		'equals'	|	{ -> f.bar.eq('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.EQ
		'greater than'	|	{ -> f.bar.gt('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.GT
		'greater than or equal to'	|	{ -> f.bar.gteq('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.GT_EQ
		'IS NOT NULL'	|	{ -> f.bar.isNotNull() }	||	com.github.javalbert.sqlbuilder.PredicateOperator.IS_NOT_NULL
		'IS NULL'	|	{ -> f.bar.isNull() }	||	com.github.javalbert.sqlbuilder.PredicateOperator.IS_NULL
		'LIKE'	|	{ -> f.bar.like('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.LIKE
		'less than'	|	{ -> f.bar.lt('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.LT
		'less than or equal to'	|	{ -> f.bar.lteq('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.LT_EQ
		'not equal'	|	{ -> f.bar.noteq('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.NOT_EQ
		'NOT LIKE'	|	{ -> f.bar.notLike('') }	||	com.github.javalbert.sqlbuilder.PredicateOperator.NOT_LIKE
	}
	
	def 'Transform dsl.SelectStatement with DISTINCT keyword and column alias in SelectList'() {
		given: 'SelectStatement'
		SelectStatement stmt = select(f.bar.as('Foobar'))
			.from(Foo)
			.distinct(true)
		
		when: 'Select is built and SelectList is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		SelectList selectList = select.nodes[0]
		
		then: 'first node is DISTINCT constant token'
		selectList.nodes[0] == SelectList.DISTINCT
		
		and: 'second node is "f.bar" table column with alias "Foobar"'
		selectList.nodes[1].alias == 'Foobar'
	}
	
	def 'Transform dsl.SelectStatement with WITH clause'() {
		given: 'SelectStatement prepended with common table expression'
		SelectStatement stmt = with(Foo).columns(f.bar)
				.as(select(f.bar).from(Foo))
				.select(f.bar)
				.from(Foo)
		
		when: 'Select is built and With is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		With with = select.nodes[0]
		
		then: 'CTE is named "Foo"'
		with.nodes[0].name == 'Foo'
		
		and: 'CTE contains column "bar"'
		with.nodes[0].columns.contains('bar')
		
		and: 'CTE query is not null'
		with.nodes[0].select instanceof Select
	}
	
	def 'Read nested conditions'() {
		given: 'SelectStatement whose WHERE clause contains a nested (wrapped in parentheses) condition'
		SelectStatement stmt = select(f.bar)
				.from(Foo)
				.where(f.bar.eq('')
					.and(f.bar.isNull().or(f.bar.eq('')))
					)
		
		when: 'Select is built and Where is retrieved'
		Select select = dslTransformer.buildSelect(stmt)
		Where where = select.nodes[2]
		
		then: "3rd node is the Condition \"(f.bar IS NULL OR f.bar = '')\""
		where.nodes[2] instanceof com.github.javalbert.sqlbuilder.Condition
	}
	
	def 'Transform dsl.SelectStatement with set operations'() {
		given: 'SelectStatement'
		SelectStatement stmt = select(f.bar)
				.from(Foo)
				.except(select(f.bar)
				.from(Foo))
				.intersect(select(f.bar)
				.from(Foo))
				.union(select(f.bar)
				.from(Foo))
				.unionAll(select(f.bar)
				.from(Foo))
		
		when: 'Select is built'
		Select select = dslTransformer.buildSelect(stmt)
		
		then: 'third node is EXCEPT set operation'
		select.nodes[2] == com.github.javalbert.sqlbuilder.SetOperator.EXCEPT
		
		then: 'fifth node is EXCEPT set operation'
		select.nodes[4] == com.github.javalbert.sqlbuilder.SetOperator.INTERSECT
		
		then: 'seventh node is EXCEPT set operation'
		select.nodes[6] == com.github.javalbert.sqlbuilder.SetOperator.UNION
		
		then: 'ninth node is EXCEPT set operation'
		select.nodes[8] == com.github.javalbert.sqlbuilder.SetOperator.UNION_ALL
	}
}
