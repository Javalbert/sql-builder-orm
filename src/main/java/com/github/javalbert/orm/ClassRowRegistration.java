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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClassRowRegistration {
	public static final int FLAG_ID = 0x1;
	public static final int FLAG_GENERATED_VALUE = 0x2;
	public static final int FLAG_TIMESTAMP = 0x4;
	public static final int FLAG_VERSION = 0x8;
	
	public static abstract class ClassMember {
		public static final int MEMBER_TYPE_FIELD = 1;
		public static final int MEMBER_TYPE_PROPERTY = 2;


		protected final String memberName;
		protected final int memberType;
		
		public String getMemberName() {
			return memberName;
		}
		public int getMemberType() {
			return memberType;
		}
		
		protected ClassMember(String memberName, int memberType) {
			this.memberName = memberName;
			this.memberType = memberType;
		}
	}
	
	public static class ColumnClassMember extends ClassMember {
		protected final String alias;
		protected final String column;
		protected final int flags;
		
		public String getAlias() {
			return alias;
		}
		public String getColumn() {
			return column;
		}
		
		protected ColumnClassMember(
				String memberName,
				int memberType,
				String column,
				String alias,
				int flags) {
			super(memberName, memberType);
			this.alias = alias;
			this.column = column;
			this.flags = flags;
		}
		
		public boolean isAutoIncrement() {
			return isPrimaryKey() && (flags & FLAG_GENERATED_VALUE) != 0;
		}
		public boolean isTimestamp() {
			return (flags & FLAG_TIMESTAMP) != 0;
		}
		public boolean isPrimaryKey() {
			return (flags & FLAG_ID) != 0;
		}
		public boolean isVersion() {
			return (flags & FLAG_VERSION) != 0;
		}
	}
	
	public static class IdClassColumn extends ClassMember {
		protected final String column;

		public String getColumn() {
			return column;
		}
		
		protected IdClassColumn(String memberName, int memberType, String column) {
			super(memberName, memberType);
			this.column = column;
		}
	}
	
	public static class RelatedEntityClassMember extends ClassMember {
		protected final String fieldName;
		
		public String getFieldName() {
			return fieldName;
		}
		
		protected RelatedEntityClassMember(
				String memberName,
				int memberType,
				String fieldName) {
			super(memberName, memberType);
			this.fieldName = fieldName;
		}
	}
	
	// Table name
	//
	private String catalog;
	private String schema;
	private String table;
	
	private final Map<String, ColumnClassMember> columnMemberMap = new HashMap<>();
	private Class<?> idClass;
	private final Map<String, IdClassColumn> idClassColumnMap = new HashMap<>();
	private final Class<?> registeringClass;
	private final Map<String, RelatedEntityClassMember> relatedEntityMemberMap = new HashMap<>();
	
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
	
	/* END Table name */

	public Map<String, ColumnClassMember> getColumnMemberMap() {
		return Collections.unmodifiableMap(columnMemberMap);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getIdClass() {
		return idClass;
	}
	
	public Map<String, IdClassColumn> getIdClassColumnMap() {
		return Collections.unmodifiableMap(idClassColumnMap);
	}
	
	@SuppressWarnings("rawtypes")
	public Class getRegisteringClass() {
		return registeringClass;
	}
	
	public Map<String, RelatedEntityClassMember> getRelatedEntityMemberMap() {
		return Collections.unmodifiableMap(relatedEntityMemberMap);
	}
	
	public ClassRowRegistration(Class<?> registeringClass) {
		this.registeringClass = Objects.requireNonNull(registeringClass, "registeringClass cannot be null");
	}
	
	/* START Fluent API */
	
	/* START Table name */
	
	public ClassRowRegistration catalog(String catalog) {
		this.catalog = catalog;
		return this;
	}
	public ClassRowRegistration schema(String schema) {
		this.schema = schema;
		return this;
	}
	public ClassRowRegistration table(String table) {
		this.table = table;
		return this;
	}
	
	/* END Table name */
	
	public ClassRowRegistration columnInField(
			String field,
			String column,
			String alias,
			int flags) {
		columnMemberMap.put(field, new ColumnClassMember(
				field,
				ClassMember.MEMBER_TYPE_FIELD,
				column,
				alias,
				flags));
		return this;
	}
	
	public ClassRowRegistration columnInProperty(
			String property,
			String column,
			String alias,
			int flags) {
		columnMemberMap.put(property, new ColumnClassMember(
				property,
				ClassMember.MEMBER_TYPE_PROPERTY,
				column,
				alias,
				flags));
		return this;
	}
	
	public ClassRowRegistration idClass(Class<?> idClass) {
		this.idClass = idClass;
		return this;
	}
	
	public ClassRowRegistration idClassColumnInField(String field, String column) {
		idClassColumnMap.put(field, new IdClassColumn(field, ClassMember.MEMBER_TYPE_FIELD, column));
		return this;
	}
	
	public ClassRowRegistration idClassColumnInProperty(String property, String column) {
		idClassColumnMap.put(property, new IdClassColumn(property, ClassMember.MEMBER_TYPE_PROPERTY, column));
		return this;
	}
	
	/**
	 * 
	 * @param field name of the field that stores the related entity
	 * @param fieldName the label specified in {@link Related} annotation
	 * @return
	 */
	public ClassRowRegistration relatedEntityInField(String field, String fieldName) {
		relatedEntityMemberMap.put(field, new RelatedEntityClassMember(field, ClassMember.MEMBER_TYPE_FIELD, fieldName));
		return this;
	}
	
	/**
	 * 
	 * @param property name of the property that stores the related entity
	 * @param fieldName the label specified in {@link Related} annotation
	 * @return
	 */
	public ClassRowRegistration relatedEntityInProperty(String property, String fieldName) {
		relatedEntityMemberMap.put(property, new RelatedEntityClassMember(
				property,
				ClassMember.MEMBER_TYPE_PROPERTY,
				fieldName));
		return this;
	}
	
	/* END Fluent API */
}
