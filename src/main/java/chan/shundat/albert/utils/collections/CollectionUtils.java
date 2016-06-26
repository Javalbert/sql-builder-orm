package chan.shundat.albert.utils.collections;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings({ "unchecked" })
public final class CollectionUtils {
	public static final CollectionFactory FACTORY_LINKED_LIST = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new LinkedList<>(); }
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

	public static final CollectionFactory FACTORY_STACK = new CollectionFactory() {
		@Override
		public <T> Collection<T> newInstance() { return new Stack<>(); }
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