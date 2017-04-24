package com.github.javalbert.sqlbuilder.dsl

import static com.github.javalbert.sqlbuilder.dsl.DSL.*

import com.github.javalbert.sqlbuilder.ColumnList
import com.github.javalbert.sqlbuilder.ColumnValues
import com.github.javalbert.sqlbuilder.Delete
import com.github.javalbert.sqlbuilder.Insert
import com.github.javalbert.sqlbuilder.Merge
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.Update
import com.github.javalbert.sqlbuilder.Where
import spock.lang.Specification

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
}
