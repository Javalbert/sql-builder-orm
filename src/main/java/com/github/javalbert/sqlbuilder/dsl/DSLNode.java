package com.github.javalbert.sqlbuilder.dsl;

public interface DSLNode {
	public static final int TYPE_CASE = 1;
	public static final int TYPE_CONDITION = 2;
	public static final int TYPE_EXPRESSION = 3;
	public static final int TYPE_FUNCTION = 4;
	public static final int TYPE_INSERT_DEFAULT = 5;
	public static final int TYPE_LITERAL_BOOLEAN = 6;
	public static final int TYPE_LITERAL_NULL = 7;
	public static final int TYPE_LITERAL_NUMBER = 8;
	public static final int TYPE_LITERAL_STRING = 9;
	public static final int TYPE_PARAMETER = 10;
	public static final int TYPE_PREDICATE = 11;
	public static final int TYPE_PREDICATE_BETWEEN = 12;
	public static final int TYPE_PREDICATE_EXISTS = 13;
	public static final int TYPE_PREDICATE_IN = 14;
	public static final int TYPE_SELECT_STATEMENT = 15;
	public static final int TYPE_TABLE_COLUMN = 16;
	
	int getNodeType();
}
