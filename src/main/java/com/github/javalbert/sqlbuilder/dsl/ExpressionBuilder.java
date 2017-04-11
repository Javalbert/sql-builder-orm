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

public interface ExpressionBuilder {
	default Expression concat(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.CONCAT);
	}
	
	default Expression divide(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.DIVIDE);
	}
	
	default Expression minus(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.MINUS);
	}
	
	default Expression mod(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.MOD);
	}
	
	default Expression multiply(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.MULTIPLY);
	}
	
	default Expression plus(ExpressionBuilder right) {
		return new Expression(this, right, ExpressionOperator.PLUS);
	}
}
