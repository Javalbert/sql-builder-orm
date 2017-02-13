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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.javalbert.sqlbuilder.SortType;

/**
 * Immutable class
 * @author Albert
 *
 */
public class Relationship {
	public static final int FIELD_DEQUE = 1;
	public static final int FIELD_LINKED_MAP = 2;
	public static final int FIELD_LINKED_SET = 3;
	public static final int FIELD_LIST = 4;
	public static final int FIELD_MAP = 5;
	public static final int FIELD_SET = 6;
	public static final int FIELD_UNIQUE = 7;
	
	public static final int TYPE_N_TO_ONE = 1;
	public static final int TYPE_ONE_TO_MANY = 2;
	
	private final int batchSize;
	private final String fieldName;
	private final int fieldType;
	private final String inverseOwnerField;
	private final List<JoinColumn> joinColumns;
	private final String mapKeyName;
	private final List<OrderByColumn> orderByColumns;
	@SuppressWarnings("rawtypes")
	private final GraphEntity ownerEntity;
	@SuppressWarnings("rawtypes")
	private final GraphEntity relatedEntity;
	private final int type;
	
	public int getBatchSize() { return batchSize; }
	public String getFieldName() { return fieldName; }
	/**
	 * See <code>Relationship.FIELD</code>* constants
	 * @return
	 */
	public int getFieldType() { return fieldType; }
	public String getInverseOwnerField() { return inverseOwnerField; }
	public List<JoinColumn> getJoinColumns() { return Collections.unmodifiableList(joinColumns); }
	public String getMapKeyName() { return mapKeyName; }
	public List<OrderByColumn> getOrderByColumns() { return Collections.unmodifiableList(orderByColumns); }
	@SuppressWarnings("rawtypes")
	public GraphEntity getOwnerEntity() { return ownerEntity; }
	@SuppressWarnings("rawtypes")
	public GraphEntity getRelatedEntity() { return relatedEntity; }
	public int getType() { return type; }
	
	@SuppressWarnings("unchecked")
	private Relationship(Builder builder) {
		batchSize = builder.batchSize;
		fieldName = builder.fieldName;
		fieldType = builder.fieldType;
		inverseOwnerField = builder.inverseOwnerField;
		joinColumns = new ArrayList<>(builder.joinColumns);
		mapKeyName = builder.mapKeyName;
		orderByColumns = builder.orderByColumns != null 
				? new ArrayList<>(builder.orderByColumns) 
				: Collections.EMPTY_LIST;
		ownerEntity = builder.ownerEntity;
		relatedEntity = builder.relatedEntity;
		type = builder.type;
	}
	
	/**
	 * <code>hashCode()</code> and <code>equals(Object)</code> methods only compute <code>fieldName</code> and <code>ownerEntity</code>
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((ownerEntity == null) ? 0 : ownerEntity.hashCode());
		return result;
	}

	/**
	 * <code>hashCode()</code> and <code>equals(Object)</code> methods only compute <code>fieldName</code> and <code>ownerEntity</code>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Builder other = (Builder) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (ownerEntity == null) {
			if (other.ownerEntity != null)
				return false;
		} else if (!ownerEntity.equals(other.ownerEntity))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Relationship [fieldName=" + fieldName + ", fieldType=" + fieldType + ", inverseOwnerField="
				+ inverseOwnerField + ", joinColumns=" + joinColumns + ", mapKeyName=" + mapKeyName
				+ ", orderByColumns=" + orderByColumns + ", ownerEntity=" + ownerEntity + ", relatedEntity="
				+ relatedEntity + ", type=" + type + "]";
	}
	
	/* BEGIN Inner classes */
	
	public static class Builder {
		private int batchSize = 10;
		private String fieldName;
		private int fieldType;
		private String inverseOwnerField;
		private List<JoinColumn> joinColumns;
		private String mapKeyName;
		private List<OrderByColumn> orderByColumns;
		@SuppressWarnings("rawtypes")
		private GraphEntity ownerEntity;
		@SuppressWarnings("rawtypes")
		private GraphEntity relatedEntity;
		private int type;
		
		protected Builder(GraphEntity<?> ownerEntity) {
			if (ownerEntity == null) {
				throw new NullPointerException("ownerEntity cannot be null");
			}
			this.ownerEntity = ownerEntity;
		}
		
		/* BEGIN Builder */
		
		/**
		 * Only applies to one-to-many relationships by creating a ORDER BY for the many-collection
		 * @param column
		 * @return
		 */
		public Builder ascendingOrder(String column) {
			initOrderByColumns();
			orderByColumns.add(new OrderByColumn(column, SortType.ASC));
			return this;
		}
		
		/**
		 * Read by {@link BatchResolver}
		 * @param batchSize
		 * @return
		 */
		public Builder batchSize(int batchSize) {
			if (batchSize < 1) {
				throw new IllegalArgumentException("batchSize cannot be less than 1");
			}
			this.batchSize = batchSize;
			return this;
		}
		
		/**
		 * Only applies to one-to-many relationships by creating a ORDER BY for the many-collection
		 * @param column
		 * @return
		 */
		public Builder descendingOrder(String column) {
			initOrderByColumns();
			orderByColumns.add(new OrderByColumn(column, SortType.DESC));
			return this;
		}
		
		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inDeque(String fieldName) {
			this.fieldName = fieldName;
			fieldType = FIELD_DEQUE;
			return this;
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inField(String fieldName) {
			this.fieldName = fieldName;
			fieldType = FIELD_UNIQUE;
			return this;
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inLinkedMap(String fieldName) {
			return inLinkedMap(fieldName, null);
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inLinkedMap(String fieldName, String mapKeyName) {
			this.fieldName = fieldName;
			fieldType = FIELD_LINKED_MAP;
			this.mapKeyName = mapKeyName;
			return this;
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inLinkedSet(String fieldName) {
			this.fieldName = fieldName;
			fieldType = FIELD_LINKED_SET;
			return this;
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inList(String fieldName) {
			this.fieldName = fieldName;
			fieldType = FIELD_LIST;
			return this;
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inMap(String fieldName) {
			return inMap(fieldName, null);
		}

		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inMap(String fieldName, String mapKeyName) {
			this.fieldName = fieldName;
			fieldType = FIELD_MAP;
			this.mapKeyName = mapKeyName;
			return this;
		}
		
		/**
		 * 
		 * @param fieldName corresponds to the value of @Related annotation in a field or property of owner class
		 * @return
		 */
		public Builder inSet(String fieldName) {
			this.fieldName = fieldName;
			fieldType = FIELD_SET;
			return this;
		}
		
		public Builder isRelatedToMany(GraphEntity<?> relatedEntity) {
			this.relatedEntity = relatedEntity;
			type = TYPE_ONE_TO_MANY;
			return this;
		}
		
		public Builder isRelatedToOne(GraphEntity<?> relatedEntity) {
			this.relatedEntity = relatedEntity;
			type = TYPE_N_TO_ONE;
			return this;
		}

		/**
		 * Assigns the value of the field 
		 * (specified by <b>inverseOwnerField</b> parameter) in a related class instance to the owner class instance of this relationship. 
		 * <br>The value of <b>inverseOwnerField</b> corresponds to the value of @Related annotation in a field or property of related class
		 * @param inverseOwnerField the field in the related class to assign
		 * @return
		 */
		public Builder inverseOwnerField(String inverseOwnerField) {
			this.inverseOwnerField = inverseOwnerField;
			return this;
		}

		public Builder joinedBy(String column) {
			return joinedBy(column, column);
		}
		
		public Builder joinedBy(String ownerClassColumn, String relatedClassColumn) {
			initJoinColumns();
			joinColumns.add(new JoinColumn(ownerClassColumn, relatedClassColumn));
			return this;
		}
		
		public Relationship build() {
			if (relatedEntity == null) {
				throw new IllegalStateException("must call one of the Relationship.isRelatedTo*(Class) methods");
			} else if (fieldName == null) {
				throw new IllegalStateException("must call one of the Relationship.in*(String) methods");
			} else if (joinColumns == null || joinColumns.isEmpty()) {
				throw new IllegalStateException("must call Relationship.joinedBy(String[, String]) method(s)");
			} else if (ownerEntity.equals(relatedEntity)) {
				throw new IllegalStateException("ownerEntity cannot be equal to relatedEntity");
			}
			
			Relationship relationship = new Relationship(this);
			ownerEntity.addRelationship(relationship);
			return relationship;
		}
		
		/* END Builder */
		
		/* BEGIN Private methods */
		
		private void initOrderByColumns() {
			if (orderByColumns == null) {
				orderByColumns = new ArrayList<>();
			}
		}
		
		private void initJoinColumns() {
			if (joinColumns == null) {
				joinColumns = new ArrayList<>();
			}
		}
		
		/* END Private methods */
	}
	
	public static class JoinColumn {
		private final String ownerClassColumn;
		private final String relatedClassColumn;
		
		public String getOwnerClassColumn() { return ownerClassColumn; }
		public String getRelatedClassColumn() { return relatedClassColumn; }
		
		public JoinColumn(String ownerClassColumn, String relatedClassColumn) {
			this.ownerClassColumn = ownerClassColumn;
			this.relatedClassColumn = relatedClassColumn;
		}
		
		@Override
		public String toString() {
			return "JoinColumn [ownerClassColumn=" + ownerClassColumn + ", relatedClassColumn=" + relatedClassColumn
					+ "]";
		}
	}
	
	public static class OrderByColumn {
		private final String column;
		private final SortType sortType;
		
		public String getColumn() { return column; }
		public SortType getSortType() { return sortType; }

		public OrderByColumn(String column, SortType sortType) {
			this.column = column;
			this.sortType = sortType;
		}
		
		@Override
		public String toString() {
			return "OrderByColumn [column=" + column + ", sortType=" + sortType + "]";
		}
	}
	
	/* END Inner classes */
}