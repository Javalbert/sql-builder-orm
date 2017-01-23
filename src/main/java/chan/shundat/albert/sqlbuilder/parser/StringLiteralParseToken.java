package chan.shundat.albert.sqlbuilder.parser;

import chan.shundat.albert.sqlbuilder.SqlStringUtils;

public class StringLiteralParseToken extends ParseToken {
	private String value;

	public String getValue() { return value; }
	
	public StringLiteralParseToken(String str) {
		super(SqlStringUtils.createLiteralToken(str), false);
		value = str;
	}
}