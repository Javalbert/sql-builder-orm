/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.utils.jdbc;

import java.sql.PreparedStatement;

public interface PreparedStatementDecorator extends PreparedStatement {
	PreparedStatement getDecoratedStatement();
}