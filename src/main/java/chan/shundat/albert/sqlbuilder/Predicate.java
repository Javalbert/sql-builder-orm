/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class Predicate extends ExpressionBuilder<Predicate> implements Node<Predicate> {
	@Override
	public int getType() { return TYPE_PREDICATE; }
	
	public Predicate() {}
	
	public Predicate(Predicate predicate) {
		nodes = NodeUtils.mutableNodes(predicate);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Predicate immutable() {
		Predicate predicate = new ImmutablePredicate(this);
		return predicate;
	}
	
	@Override
	public Predicate mutable() {
		Predicate predicate = new Predicate(this);
		return predicate;
	}
	
	/* BEGIN Fluent API */
	
	public Predicate and() {
		nodes.add(PredicateOperator.AND);
		return this;
	}
	
	public Predicate between() {
		nodes.add(PredicateOperator.BETWEEN);
		return this;
	}

	public Predicate eq() {
		nodes.add(PredicateOperator.EQ);
		return this;
	}
	
	public Predicate exists() {
		nodes.add(PredicateOperator.EXISTS);
		return this;
	}
	
	public Predicate gt() {
		nodes.add(PredicateOperator.GT);
		return this;
	}
	
	public Predicate gteq() {
		nodes.add(PredicateOperator.GT_EQ);
		return this;
	}
	
	public Predicate in() {
		nodes.add(PredicateOperator.IN);
		return this;
	}
	
	public Predicate isNotNull() {
		nodes.add(PredicateOperator.IS_NOT_NULL);
		return this;
	}
	
	public Predicate isNull() {
		nodes.add(PredicateOperator.IS_NULL);
		return this;
	}
	
	public Predicate like() {
		nodes.add(PredicateOperator.LIKE);
		return this;
	}
	
	public Predicate lt() {
		nodes.add(PredicateOperator.LT);
		return this;
	}
	
	public Predicate lteq() {
		nodes.add(PredicateOperator.LT_EQ);
		return this;
	}
	
	public Predicate notBetween() {
		nodes.add(PredicateOperator.NOT_BETWEEN);
		return this;
	}
	
	public Predicate noteq() {
		nodes.add(PredicateOperator.NOT_EQ);
		return this;
	}
	
	public Predicate notExists() {
		nodes.add(PredicateOperator.NOT_EXISTS);
		return this;
	}
	
	public Predicate notIn() {
		nodes.add(PredicateOperator.NOT_IN);
		return this;
	}
	
	public Predicate notLike() {
		nodes.add(PredicateOperator.NOT_LIKE);
		return this;
	}
	
	public Predicate values(InValues values) {
		if (values == null) {
			throw new NullPointerException("values cannot be null");
		}
		nodes.add(values);
		return this;
	}
	
	/* END Fluent API */
}