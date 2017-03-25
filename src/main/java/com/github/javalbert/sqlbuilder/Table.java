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

public class Table implements Aliasable, Node<Table> {
	public static Table name(String name) {
		return new Table(name, null);
	}
	
	protected String alias;
	protected String name;
	
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
	@Override
	public int getType() { return TYPE_TABLE; }

	public Table(String name) {
		this(name, null);
	}
	
	public Table(String name, String alias) {
		this.name = Strings.safeTrim(name);
		this.alias = Strings.safeTrim(alias);
	}
	
	public Table(Table table) {
		this(table.getName(), table.getAlias());
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Table immutable() {
		return new ImmutableTable(this);
	}
	
	@Override
	public Table mutable() {
		return new Table(this);
	}
}