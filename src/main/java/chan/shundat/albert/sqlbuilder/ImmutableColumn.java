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

public class ImmutableColumn extends Column {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setPrefix(Prefix prefix) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setPrefixValue(String prefixValue) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableColumn(Column column) {
		super(column.getPrefixValue(), 
				column.getPrefix(), 
				column.getName(), 
				column.getAlias());
	}
}