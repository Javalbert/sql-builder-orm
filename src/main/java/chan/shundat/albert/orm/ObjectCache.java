/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.orm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ObjectCache {
	private final Map<Class, Map<Serializable, Object>> classObjectsMap = new HashMap<>();
	
	public Map<Class, Map<Serializable, Object>> getClassObjectsMap() { return classObjectsMap; }
	
	public void add(Object object, Serializable id) {
		assertObjectNotNull(object);
		getClassObjects(object.getClass()).put(id, object);
	}
	
	public void add(ClassRowMapping classRowMapping, Object object) {
		assertObjectNotNull(object);
		assertEqualClasses(classRowMapping, object);
		
		Serializable id = classRowMapping.getOrCreateId(object);
		add(object, id);
	}
	
	public Object get(Class clazz, Serializable id) {
		return getClassObjects(clazz).get(id);
	}
	
	public Object get(ClassRowMapping classRowMapping, Object object) {
		assertObjectNotNull(object);
		assertEqualClasses(classRowMapping, object);

		Serializable id = classRowMapping.getOrCreateId(object);
		return get(classRowMapping, id);
	}
	
	public Object get(ClassRowMapping classRowMapping, Serializable id) {
		return get(classRowMapping.getClazz(), id);
	}
	
	private void assertEqualClasses(ClassRowMapping classRowMapping, Object object) {
		if (classRowMapping.getClazz() == object.getClass()) {
			return;
		}
		throw new IllegalArgumentException("object's class is not the same as classRowMapping's class");
	}
	
	private void assertObjectNotNull(Object object) {
		if (object != null) {
			return;
		}
		throw new NullPointerException("object cannot be null");
	}
	
	private Map<Serializable, Object> getClassObjects(Class clazz) {
		Map<Serializable, Object> classObjects = classObjectsMap.get(clazz);
		
		if (classObjects == null) {
			classObjects = new HashMap<>();
			classObjectsMap.put(clazz, classObjects);
		}
		return classObjects;
	}
}