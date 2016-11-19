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

public class BinaryOperator extends Token {
	public static final String STRING_CONCAT = "||";
	
	public static final BinaryOperator CONCAT = new BinaryOperator(STRING_CONCAT);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	@Override
	public int getType() {
		return TYPE_BINARY_OPERATOR;
	}
	
	public BinaryOperator(BinaryOperator operator) {
		this(operator.getToken());
	}
	
	public BinaryOperator(String operator) {
		super(operator, true);
	}
	
	@Override
	public BinaryOperator immutable() {
		return this;
	}
	
	@Override
	public BinaryOperator mutable() {
		return this;
	}
}