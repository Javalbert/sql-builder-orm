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

public class Functions {
	public static final Function AVG = new Function("AVG");
	public static final Function CAST = new Function("CAST");
	public static final Function COUNT = new Function("COUNT");
	public static final Function MAX = new Function("MAX");
	public static final Function MIN = new Function("MIN");
	public static final Function SUM = new Function("SUM");
	
	private Functions() {}
}
