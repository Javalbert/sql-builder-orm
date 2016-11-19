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
package chan.shundat.albert.sqlbuilder.vendor;

import chan.shundat.albert.sqlbuilder.BinaryOperator;
import chan.shundat.albert.sqlbuilder.Literal;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.Token;

@SuppressWarnings("rawtypes")
public class MSSQL extends ANSI {
	public static final String LITERAL_BOOLEAN_FALSE = "0";
	public static final String LITERAL_BOOLEAN_TRUE = "1";
	public static final String STRING_CONCAT = "+";
	
	@Override
	public String print(Literal literal) {
		StringBuilder builder = new StringBuilder();
		switch (literal.getType()) {
			case Node.TYPE_LITERAL_BOOLEAN:
				builder.append((boolean)literal.getValue() ? LITERAL_BOOLEAN_TRUE : LITERAL_BOOLEAN_FALSE);
				break;
			default:
				return super.print(literal);
		}
		appendAsKeyword(builder, literal);
		return builder.toString();
	}
	
	@Override
	public String print(Token token) {
		if (token == BinaryOperator.CONCAT) {
			return STRING_CONCAT;
		}
		return super.print(token);
	}
}