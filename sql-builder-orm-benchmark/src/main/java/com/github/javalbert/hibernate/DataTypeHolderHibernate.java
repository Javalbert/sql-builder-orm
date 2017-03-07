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
package com.github.javalbert.hibernate;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <a href="http://www.h2database.com/html/datatypes.html">H2DB data types</a>
 * @author Albert
 *
 */
@Entity
@Table(name = "DataTypeHolder")
public class DataTypeHolderHibernate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "int_val")
	private int intVal;
	@Column(name = "boolean_val")
	private boolean booleanVal;
	@Column(name = "bigint_val")
	private long bigintVal;
	@Column(name = "decimal_val")
	private BigDecimal decimalVal;
	@Column(name = "double_val")
	private double doubleVal;
	@Column(name = "real_val")
	private float realVal;
	@Column(name = "date_val")
	private Date dateVal;
	@Column(name = "timestamp_val")
	private Date timestampVal;
	@Column(name = "varchar_val")
	private String varcharVal;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIntVal() {
		return intVal;
	}
	public void setIntVal(int intVal) {
		this.intVal = intVal;
	}
	public boolean isBooleanVal() {
		return booleanVal;
	}
	public void setBooleanVal(boolean booleanVal) {
		this.booleanVal = booleanVal;
	}
	public long getBigintVal() {
		return bigintVal;
	}
	public void setBigintVal(long bigintVal) {
		this.bigintVal = bigintVal;
	}
	public BigDecimal getDecimalVal() {
		return decimalVal;
	}
	public void setDecimalVal(BigDecimal decimalVal) {
		this.decimalVal = decimalVal;
	}
	public double getDoubleVal() {
		return doubleVal;
	}
	public void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}
	public float getRealVal() {
		return realVal;
	}
	public void setRealVal(float realVal) {
		this.realVal = realVal;
	}
	public Date getDateVal() {
		return dateVal;
	}
	public void setDateVal(Date dateVal) {
		this.dateVal = dateVal;
	}
	public Date getTimestampVal() {
		return timestampVal;
	}
	public void setTimestampVal(Date timestampVal) {
		this.timestampVal = timestampVal;
	}
	public String getVarcharVal() {
		return varcharVal;
	}
	public void setVarcharVal(String varcharVal) {
		this.varcharVal = varcharVal;
	}
	
	public DataTypeHolderHibernate() {}
	
	public DataTypeHolderHibernate(
			int id,
			int intVal,
			boolean booleanVal,
			long bigintVal,
			BigDecimal decimalVal,
			double doubleVal,
			float realVal,
			Date dateVal,
			Date timestampVal,
			String varcharVal) {
		this.id = id;
		this.intVal = intVal;
		this.booleanVal = booleanVal;
		this.bigintVal = bigintVal;
		this.decimalVal = decimalVal;
		this.doubleVal = doubleVal;
		this.realVal = realVal;
		this.dateVal = dateVal;
		this.timestampVal = timestampVal;
		this.varcharVal = varcharVal;
	}
}