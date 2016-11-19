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

public class AggregateFunction extends Function {
	public static AggregateFunction avg() { return new AggregateFunction(Keywords.AVG); }
	public static AggregateFunction count() { return new AggregateFunction(Keywords.COUNT); }
	public static AggregateFunction max() { return new AggregateFunction(Keywords.MAX); }
	public static AggregateFunction min() { return new AggregateFunction(Keywords.MIN); }
	public static AggregateFunction sum() { return new AggregateFunction(Keywords.SUM); }
	
	private AggregateFunction(String name) {
		super(name, 1);
	}
}