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