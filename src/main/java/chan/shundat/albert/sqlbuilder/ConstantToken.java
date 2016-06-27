/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

/**
 * Token.immutable() and Token.mutable() methods do nothing other than returning itself
 * @author Albert
 *
 */
public class ConstantToken extends ImmutableToken {
	public ConstantToken(String token) {
		super(token);
	}
	
	public ConstantToken(String token, boolean isNextNodeAnExpression) {
		super(token, isNextNodeAnExpression);
	}

	public ConstantToken(Token token) {
		this(token.getToken(), token.isNextNodeAnExpression());
	}
	
	@Override
	public Token immutable() {
		return this;
	}
	
	@Override
	public Token mutable() {
		return this;
	}
}