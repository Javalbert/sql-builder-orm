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
package com.github.javalbert.utils.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class TwoSetComparison<T> {
	private LinkedHashSet<T> difference = new LinkedHashSet<>();
	private LinkedHashSet<T> lhsSet = new LinkedHashSet<>();
	private LinkedHashSet<T> rhsSet = new LinkedHashSet<>();
	/**
	 * Contains similar elements between the two sets using lhs set
	 */
	private LinkedHashSet<T> sameElements = new LinkedHashSet<>();
	private LinkedHashSet<T> uniqueLhsElements = new LinkedHashSet<>();
	private LinkedHashSet<T> uniqueRhsElements = new LinkedHashSet<>();
	private LinkedHashSet<T> union = new LinkedHashSet<>();
	
	public Set<T> getDifference() {
		return Collections.unmodifiableSet(difference);
	}
	
	public Set<T> getLhsSet() {
		return Collections.unmodifiableSet(lhsSet);
	}

	public Set<T> getRhsSet() {
		return Collections.unmodifiableSet(rhsSet);
	}

	public Set<T> getSameElements() {
		return Collections.unmodifiableSet(sameElements);
	}

	public Set<T> getUniqueLhsElements() {
		return Collections.unmodifiableSet(uniqueLhsElements);
	}

	public Set<T> getUniqueRhsElements() {
		return Collections.unmodifiableSet(uniqueRhsElements);
	}
	
	public Set<T> getUnion() {
		return Collections.unmodifiableSet(union);
	}

	public TwoSetComparison(Iterable<T> lhsIterable, Iterable<T> rhsIterable) {
		addToSet(lhsSet, lhsIterable);
		addToSet(rhsSet, rhsIterable);
		
		Iterator<T> lhsIterator = lhsSet.iterator();
		while (lhsIterator.hasNext()) {
			T lhsElement = lhsIterator.next();
			
			if (rhsSet.contains(lhsElement)) {
				sameElements.add(lhsElement);
			} else {
				uniqueLhsElements.add(lhsElement);
				difference.add(lhsElement);
			}
			union.add(lhsElement);
		}

		Iterator<T> rhsIterator = rhsSet.iterator();
		while (rhsIterator.hasNext()) {
			T rhsElement = rhsIterator.next();
			
			if (!lhsSet.contains(rhsElement)) {
				uniqueRhsElements.add(rhsElement);
				difference.add(rhsElement);
			}
			union.add(rhsElement);
		}
	}
	
	private void addToSet(Set<T> set, Iterable<T> iterable) {
		Iterator<T> lhsIterator = iterable.iterator();
		while (lhsIterator.hasNext()) {
			set.add(lhsIterator.next());
		}
	}
}