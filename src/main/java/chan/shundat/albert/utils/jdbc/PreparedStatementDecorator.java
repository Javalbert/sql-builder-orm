package chan.shundat.albert.utils.jdbc;

import java.sql.PreparedStatement;

public interface PreparedStatementDecorator extends PreparedStatement {
	PreparedStatement getDecoratedStatement();
}