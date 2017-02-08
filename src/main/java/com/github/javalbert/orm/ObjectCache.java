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
package com.github.javalbert.orm;

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