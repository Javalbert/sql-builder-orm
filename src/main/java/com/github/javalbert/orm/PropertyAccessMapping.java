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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.javalbert.utils.string.Strings;

public class PropertyAccessMapping extends FieldColumnMapping {
	private static String initMapKeyName(String column, String alias, Method getter, Method setter) {
		MapKey mapKey = getter.getAnnotation(MapKey.class);
		
		if (mapKey == null) {
			mapKey = setter.getAnnotation(MapKey.class);
		}
		String mapKeyName = mapKey != null ? mapKey.value() : null;
		
		return !Strings.isNullOrEmpty(mapKeyName) ? mapKeyName 
				: !Strings.isNullOrEmpty(alias) ? alias 
				: !Strings.isNullOrEmpty(column) ? column 
				: setter.getName().substring(3); // Remove "set" from setter
	}
	
	private final Method getter;
	private final Method setter;

	public Method getGetter() { return getter; }
	public Method getSetter() { return setter; }

	protected PropertyAccessMapping(String column, 
			String alias, 
			Method getter, 
			Method setter, 
			int jdbcType, 
			boolean primaryKey, 
			GeneratedValue generatedValue, 
			boolean version) {
		super(column, alias, jdbcType, initMapKeyName(column, alias, getter, setter), primaryKey, generatedValue, version);
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public Object get(Object instance) {
		try {
			return getter.invoke(instance);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
	
	@Override
	public void set(Object instance, Object value) {
		try {
			setter.invoke(instance, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
	}
}