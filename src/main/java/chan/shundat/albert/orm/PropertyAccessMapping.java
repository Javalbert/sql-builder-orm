/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.orm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chan.shundat.albert.utils.string.Strings;

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