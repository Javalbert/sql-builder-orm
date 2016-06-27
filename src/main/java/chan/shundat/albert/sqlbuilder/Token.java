/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

public class Token implements Node<Token> {
	private boolean isNextNodeAnExpression;
	private String token;
	
	public boolean isNextNodeAnExpression() { return isNextNodeAnExpression; }
	public String getToken() { return token; }
	public void setToken(String token) {
		this.token = Strings.safeTrim(token);
	}
	@Override
	public int getType() { return TYPE_TOKEN; }
	
	public Token(String token) {
		this(token, true);
	}
	
	public Token(String token, boolean isNextNodeAnExpression) {
		this.isNextNodeAnExpression = isNextNodeAnExpression;
		this.token = Strings.safeTrim(token);
	}
	
	public Token(Token token) {
		this(token.getToken(), token.isNextNodeAnExpression());
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Token immutable() {
		Token token = new ImmutableToken(this);
		return token;
	}
	
	@Override
	public Token mutable() {
		Token token = new Token(this);
		return token;
	}
}