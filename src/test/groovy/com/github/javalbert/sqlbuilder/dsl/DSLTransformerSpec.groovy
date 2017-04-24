package com.github.javalbert.sqlbuilder.dsl

import static com.github.javalbert.sqlbuilder.dsl.DSL.*

import com.github.javalbert.sqlbuilder.ColumnList
import com.github.javalbert.sqlbuilder.ColumnValues
import com.github.javalbert.sqlbuilder.Delete
import com.github.javalbert.sqlbuilder.Insert
import com.github.javalbert.sqlbuilder.Merge
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.Update

import spock.lang.Specification

class DSLTransformerSpec extends Specification {
	private static final Table Foo = new Table("Foo");
	
	private static final FooTableAlias f = new FooTableAlias();
	
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
}
