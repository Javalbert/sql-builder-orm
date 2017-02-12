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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "unchecked" })
public final class CollectionUtils {
	public static final CollectionFactory FACTORY_DEQUE = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new ArrayDeque<>(); }
	};
	
	public static final MapFactory FACTORY_LINKED_MAP = new MapFactory() {
		@Override
		public <K, V> Map<K, V> newInstance() { return new LinkedHashMap<>(); }
	};

	public static final CollectionFactory FACTORY_LINKED_SET = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new LinkedHashSet<>(); }
	};

	public static final CollectionFactory FACTORY_LIST = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new ArrayList<>(); }
	};

	public static final MapFactory FACTORY_MAP = new MapFactory() {
		@Override
		public <K, V> Map<K, V> newInstance() { return new HashMap<>(); }
	};

	public static final CollectionFactory FACTORY_SET = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new HashSet<>(); }
	};

	public static <T> List<T> arrayList(T... items) {
		List<T> list = new ArrayList<>();
		
		for (T item : items) {
			list.add(item);
		}
		return list;
	}
	
	public static <K, V> Map<K, V> hashMap(SimpleEntry<K, V>... entries) {
		Map<K, V> map = new LinkedHashMap<>();
		
		for (SimpleEntry<K, V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}
	
	public static <T> Set<T> hashSet(T... items) {
		Set<T> set = new LinkedHashSet<>();
		
		for (T item : items) {
			set.add(item);
		}
		return set;
	}
	
	public static <T> List<T> immutableArrayList(T... items) {
		return Collections.unmodifiableList(arrayList(items));
	}
	
	public static <K, V> Map<K, V> immutableHashMap(SimpleEntry<K, V>... entries) {
		return Collections.unmodifiableMap(hashMap(entries));
	}
	
	public static <T> Set<T> immutableHashSet(T... items) {
		return Collections.unmodifiableSet(hashSet(items));
	}
	
	private CollectionUtils() {}
}