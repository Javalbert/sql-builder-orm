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
package com.github.javalbert.orm;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javalbert.sqlbuilder.vendor.Vendor;
import com.github.javalbert.utils.reflection.MemberAccess;

public abstract class ClassRowMapper {
	protected final Class<?> clazz;
	protected final Map<String, FieldColumnMapping> fieldAliasMappings = new HashMap<>();
	protected final List<FieldColumnMapping> fieldColumnMappingList = new ArrayList<>();
	protected final Map<String, FieldColumnMapping> fieldColumnMappings = new HashMap<>();
	protected final Map<String, Field> fieldMap = new HashMap<>();
	protected final List<Field> fields;
	protected Class<?> idClass;
	protected final List<FieldColumnMapping> idClassMappings = new ArrayList<>();
	protected final Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<>();
	protected final List<PropertyDescriptor> propertyDescriptors;
	protected final Map<String, MemberAccess> relatedMemberAccessMap = new HashMap<>();
	protected final Vendor vendor;
	
	// Table name fields
	//
	protected String catalog;
	protected String schema;
	protected String table;
	protected String tableIdentifier;
	
	public Map<String, FieldColumnMapping> getFieldAliasMappings() {
		return Collections.unmodifiableMap(fieldAliasMappings);
	}

	public List<FieldColumnMapping> getFieldColumnMappingList() {
		return Collections.unmodifiableList(fieldColumnMappingList);
	}

	public Map<String, FieldColumnMapping> getFieldColumnMappings() {
		return Collections.unmodifiableMap(fieldColumnMappings);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getIdClass() {
		return idClass;
	}
	
	public List<FieldColumnMapping> getIdClassMappings() {
		return Collections.unmodifiableList(idClassMappings);
	}
	
	public Map<String, MemberAccess> getRelatedMemberAccessMap() {
		return Collections.unmodifiableMap(relatedMemberAccessMap);
	}
	
	/* START Table name */
	
	public String getCatalog() {
		return catalog;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public String getTable() {
		return table;
	}
	
	public String getTableIdentifier() {
		return tableIdentifier;
	}
	
	/* END Table name */
	
	public ClassRowMapper(Class<?> clazz, Vendor vendor) {
		this.clazz = clazz;
		
		fields = Collections.unmodifiableList(Arrays.asList(clazz.getDeclaredFields()));
		fields.forEach(field -> fieldMap.put(field.getName(), field));
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			propertyDescriptors = Collections.unmodifiableList(Arrays.asList(beanInfo.getPropertyDescriptors()));
			propertyDescriptors.forEach(prop -> propertyDescriptorMap.put(prop.getName(), prop));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		
		this.vendor = vendor;
	}
	
	public abstract void map();
}