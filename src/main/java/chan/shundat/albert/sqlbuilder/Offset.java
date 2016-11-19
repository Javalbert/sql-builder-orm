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

public class Offset implements Node<Offset> {
	private int skipCount;

	public int getSkipCount() { return skipCount; }
	public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
	@Override
	public int getType() {
		return TYPE_OFFSET;
	}
	
	public Offset(int skipCount) {
		this.skipCount = skipCount;
	}
	
	public Offset(Offset offset) {
		this(offset.getSkipCount());
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Offset immutable() {
		Offset offset = new ImmutableOffset(this);
		return offset;
	}
	
	@Override
	public Offset mutable() {
		Offset offset = new Offset(this);
		return offset;
	}
}