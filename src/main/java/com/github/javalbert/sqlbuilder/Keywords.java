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

import java.util.Set;

import com.github.javalbert.utils.collections.CollectionUtils;

public final class Keywords {
	/* BEGIN Reserved words */
	
	public static final String ALL = "ALL";
	public static final String AND = "AND";
	public static final String AS = "AS";
	public static final String ASC = "ASC";
	public static final String BETWEEN = "BETWEEN";
	public static final String BY = "BY";
	public static final String CASE = "CASE";
	public static final String DEFAULT = "DEFAULT";
	public static final String DELETE = "DELETE";
	public static final String DESC = "DESC";
	public static final String DISTINCT = "DISTINCT";
	public static final String ELSE = "ELSE";
	public static final String END = "END";
	public static final String EXCEPT = "EXCEPT";
	public static final String EXISTS = "EXISTS";
	public static final String FALSE = "FALSE";
	public static final String FETCH = "FETCH";
	public static final String FIRST = "FIRST";
	public static final String FROM = "FROM";
	public static final String FULL = "FULL";
	public static final String GROUP = "GROUP";
	public static final String HAVING = "HAVING";
	public static final String IN = "IN";
	public static final String INNER = "INNER";
	public static final String INSERT = "INSERT";
	public static final String INTERSECT = "INTERSECT";
	public static final String INTO = "INTO";
	public static final String IS = "IS";
	public static final String JOIN = "JOIN";
	public static final String LEFT = "LEFT";
	public static final String LIKE = "LIKE";
	public static final String MATCHED = "MATCHED";
	public static final String MERGE = "MERGE";
	public static final String NEXT = "NEXT";
	public static final String NOT = "NOT";
	public static final String NULL = "NULL";
	public static final String OFFSET = "OFFSET";
	public static final String ON = "ON";
	public static final String ONLY = "ONLY";
	public static final String OR = "OR";
	public static final String ORDER = "ORDER";
	public static final String OUTER = "OUTER";
	public static final String RIGHT = "RIGHT";
	public static final String ROW = "ROW";
	public static final String ROWS = "ROWS";
	public static final String SELECT = "SELECT";
	public static final String SET = "SET";
	public static final String THEN = "THEN";
	public static final String TRUE = "TRUE";
	public static final String UNION = "UNION";
	public static final String UPDATE = "UPDATE";
	public static final String USING = "USING";
	public static final String VALUES = "VALUES";
	public static final String WHEN = "WHEN";
	public static final String WHERE = "WHERE";
	public static final String WITH = "WITH";
	
	/* END Reserved words */
	
	/* BEGIN Compound reserved words */

	public static final String DELETE_FROM = DELETE + " " + FROM;
	public static final String FULL_OUTER_JOIN = FULL + " " + OUTER + " " + JOIN;
	public static final String GROUP_BY = GROUP + " " + BY;
	public static final String INNER_JOIN = INNER + " " + JOIN;
	public static final String IS_NOT_NULL = IS + " " + NOT + " " + NULL;
	public static final String IS_NULL = IS + " " + NULL;
	public static final String LEFT_JOIN = LEFT + " " + JOIN;
	public static final String LEFT_OUTER_JOIN = LEFT + " " + OUTER + " " + JOIN;
	public static final String NOT_BETWEEN = NOT + " " + BETWEEN;
	public static final String NOT_EXISTS = NOT + " " + EXISTS;
	public static final String NOT_IN = NOT + " " + IN;
	public static final String NOT_LIKE = NOT + " " + LIKE;
	public static final String ORDER_BY = ORDER + " " + BY;
	public static final String RIGHT_JOIN = RIGHT + " " + JOIN;
	public static final String RIGHT_OUTER_JOIN = RIGHT + " " + OUTER + " " + JOIN;
	public static final String UNION_ALL = UNION + " " + ALL;
	public static final String WHEN_MATCHED = WHEN + " " + MATCHED + " ";
	public static final String WHEN_NOT_MATCHED = WHEN + " " + NOT + " " + MATCHED + " ";
	
	/* END Compound reserved words */

	/* BEGIN Non-reserved words */
	
	public static final String AVG = "AVG";
	public static final String CAST = "CAST";
	public static final String COUNT = "COUNT";
	public static final String MAX = "MAX";
	public static final String MIN = "MIN";
	public static final String SUM = "SUM";

	/* END Non-reserved words */
	
	public static final Set<String> KEYWORD_SET = CollectionUtils.immutableHashSet(
		/* BEGIN Reserved words */
		ALL,
		AND,
		AS,
		ASC,
		BETWEEN,
		BY,
		CASE,
		DEFAULT,
		DELETE,
		DESC,
		DISTINCT,
		ELSE,
		END,
		EXCEPT,
		EXISTS,
		FALSE,
		FETCH,
		FIRST,
		FROM,
		FULL,
		GROUP,
		HAVING,
		IN,
		INNER,
		INSERT,
		INTERSECT,
		INTO,
		IS,
		JOIN,
		LEFT,
		LIKE,
		MATCHED,
		MERGE,
		NEXT,
		NOT,
		NULL,
		OFFSET,
		ON,
		ONLY,
		OR,
		ORDER,
		OUTER,
		RIGHT,
		ROW,
		ROWS,
		SELECT,
		SET,
		THEN,
		TRUE,
		UNION,
		UPDATE,
		USING,
		VALUES,
		WHEN,
		WHERE,
		WITH,
		/* END Reserved words */
		
		/* BEGIN Compound reserved words */
		DELETE_FROM,
		FULL_OUTER_JOIN,
		GROUP_BY,
		INNER_JOIN,
		IS_NOT_NULL,
		IS_NULL,
		LEFT_JOIN,
		LEFT_OUTER_JOIN,
		NOT_BETWEEN,
		NOT_EXISTS,
		NOT_IN,
		NOT_LIKE,
		ORDER_BY,
		RIGHT_JOIN,
		RIGHT_OUTER_JOIN,
		UNION_ALL,
		WHEN_MATCHED,
		WHEN_NOT_MATCHED,
		/* END Compound reserved words */

		/* BEGIN Non-reserved words */
		AVG,
		CAST,
		COUNT,
		MAX,
		MIN,
		SUM
		/* BEGIN Non-reserved words */
	);
	
	private Keywords() {}
}