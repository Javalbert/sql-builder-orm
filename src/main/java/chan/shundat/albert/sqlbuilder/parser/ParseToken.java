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
package chan.shundat.albert.sqlbuilder.parser;

import java.util.ArrayList;
import java.util.List;

public class ParseToken {
	private final List<ParseToken> nodes = new ArrayList<>();
	private final String token;
	
	public List<ParseToken> getNodes() { return nodes; }
		public String getToken() { return token; }
	
	public ParseToken(String token) {
		this(token, false);
	}
	
	public ParseToken(String token, boolean keyword) {
		this.token = keyword ? token.toUpperCase() : token;
	}
	
	public void addNode(ParseToken node) {
		nodes.add(node);
	}
	
	@Override
	public String toString() {
		return token + (nodes != null && !nodes.isEmpty() 
				? " (" + nodes.get(nodes.size() - 1).getToken() + ")" : "");
	}
}