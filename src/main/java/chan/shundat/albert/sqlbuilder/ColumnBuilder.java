/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public interface ColumnBuilder<T> {
	T column(String name);

	/**
	 * Should call <pre>{@code column(String)}</pre> afterwards
	 * @param alias
	 * @return
	 */
	T tableAlias(String alias);
	
	/**
	 * Should call <pre>{@code column(String)}</pre> afterwards
	 * @param name
	 * @return
	 */
	T tableName(String name);
}