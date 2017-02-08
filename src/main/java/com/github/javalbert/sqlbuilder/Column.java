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
package com.github.javalbert.sqlbuilder;

import com.github.javalbert.utils.string.Strings;

public class Column implements Aliasable, Node<Column> {
	public static Column byAlias(String prefixValue) {
		return new Column(prefixValue, Prefix.TABLE_ALIAS);
	}

	public static Column byTableName(String prefixValue) {
		return new Column(prefixValue, Prefix.TABLE_NAME);
	}
	
	private String alias;
	private String name;
	private Prefix prefix;
	private String prefixValue;

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) {
		this.alias = Strings.safeTrim(alias);
	}
	public String getName() { return name; }
	public void setName(String name) {
		this.name = Strings.safeTrim(name);
	}
	public Prefix getPrefix() { return prefix; }
	public void setPrefix(Prefix prefix) { this.prefix = prefix; }
	public String getPrefixValue() { return prefixValue; }
	public void setPrefixValue(String prefixValue) {
		this.prefixValue = Strings.safeTrim(prefixValue);
	}
	@Override
	public int getType() { return TYPE_COLUMN; }
	
	public Column() {}
	
	public Column(Column column) {
		this(column.getPrefixValue(), column.getPrefix(), column.getName(), column.getAlias());
	}
	
	public Column(String prefixValue, Prefix prefix) {
		this(prefixValue, prefix, null);
	}

	public Column(String prefixValue, Prefix prefix, String name) {
		this(prefixValue, prefix, name, null);
	}

	public Column(String prefixValue, Prefix prefix, String name, String alias) {
		this.alias = Strings.safeTrim(alias);
		this.name = Strings.safeTrim(name);
		this.prefix = prefix;
		this.prefixValue = Strings.safeTrim(prefixValue);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Column immutable() {
		Column column = new ImmutableColumn(this);
		return column;
	}
	
	@Override
	public Column mutable() {
		Column column = new Column(this);
		return column;
	}
}