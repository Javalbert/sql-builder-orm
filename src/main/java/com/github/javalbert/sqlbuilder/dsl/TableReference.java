/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder.dsl;

public interface TableReference {
	public static final int TABLE_TABLE = 1;
	public static final int TABLE_JOINED_TABLE = 2;
	public static final int TABLE_INLINE_VIEW = 3;
	
	int getTableType();
	
	default JoinedTable fullOuterJoin(TableReference table) {
		return new JoinedTable(this, Utils.nestedJoin(table), JoinType.FULL);
	}
	
	default JoinedTable innerJoin(TableReference table) {
		return new JoinedTable(this, Utils.nestedJoin(table), JoinType.INNER);
	}
	
	default JoinedTable leftOuterJoin(TableReference table) {
		return new JoinedTable(this, Utils.nestedJoin(table), JoinType.LEFT);
	}
	
	default JoinedTable rightOuterJoin(TableReference table) {
		return new JoinedTable(this, Utils.nestedJoin(table), JoinType.RIGHT);
	}
}
