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

public class Fetch implements Node<Fetch> {
	private int fetchCount;

	public int getFetchCount() { return fetchCount; }
	public void setFetchCount(int fetchCount) { this.fetchCount = fetchCount; }
	@Override
	public int getType() {
		return TYPE_FETCH;
	}
	
	public Fetch(Fetch fetch) {
		this(fetch.getFetchCount());
	}
	
	public Fetch(int fetchCount) {
		this.fetchCount = fetchCount;
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Fetch immutable() {
		return new ImmutableFetch(this);
	}
	
	@Override
	public Fetch mutable() {
		return new Fetch(this);
	}
}