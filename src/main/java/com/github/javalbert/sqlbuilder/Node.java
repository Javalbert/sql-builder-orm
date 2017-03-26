/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.sqlbuilder;

@SuppressWarnings("rawtypes")
public interface Node<T extends Node> {
	public static final int TYPE_BINARY_OPERATOR = 1;
	public static final int TYPE_CASE = 2;
	public static final int TYPE_COLUMN = 3;
	public static final int TYPE_COLUMN_LIST = 4;
	public static final int TYPE_COLUMN_VALUES = 5;
	public static final int TYPE_COMMON_TABLE_EXPRESSION = 6;
	public static final int TYPE_CONDITION = 7;
	public static final int TYPE_DELETE = 8;
	public static final int TYPE_EXPRESSION = 9;
	public static final int TYPE_FETCH = 10;
	public static final int TYPE_FROM = 11;
	public static final int TYPE_FUNCTION = 12;
	public static final int TYPE_GROUP_BY = 13;
	public static final int TYPE_HAVING = 14;
	public static final int TYPE_IN_VALUES = 15;
	public static final int TYPE_INSERT = 16;
	public static final int TYPE_LITERAL_BOOLEAN = 17;
	public static final int TYPE_LITERAL_NULL = 18;
	public static final int TYPE_LITERAL_NUMBER = 19;
	public static final int TYPE_LITERAL_STRING = 20;
	public static final int TYPE_MERGE = 21;
	public static final int TYPE_OFFSET = 22;
	public static final int TYPE_ORDER_BY = 23;
	public static final int TYPE_ORDER_BY_SORT = 24;
	public static final int TYPE_PARAM = 25;
	public static final int TYPE_PREDICATE = 26;
	public static final int TYPE_SELECT = 27;
	public static final int TYPE_SELECT_LIST = 28;
	public static final int TYPE_SET_OPERATOR = 29;
	public static final int TYPE_SET_VALUE = 30;
	public static final int TYPE_SET_VALUES = 31;
	public static final int TYPE_TABLE = 32;
	public static final int TYPE_TOKEN = 33;
	public static final int TYPE_UPDATE = 34;
	public static final int TYPE_WHERE = 35;
	public static final int TYPE_WITH = 36;
	
	int getType();

	boolean accept(NodeVisitor visitor);
	/**
	 * Deep copy of the node and recursive child nodes, all of which are then immutable
	 * @return
	 */
	T immutable();
	/**
	 * Deep copy of the node and recursive child nodes, all of which are then mutable
	 * @return
	 */
	T mutable();
}