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
package com.github.javalbert.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Entity;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.IsTimestamp;
import com.github.javalbert.orm.Table;
import com.github.javalbert.utils.HashEqualsUtils;

/**
 * <a href="http://www.h2database.com/html/datatypes.html">H2 data types</a>
 * @author Albert
 *
 */
@Entity
@Table(name = "DataTypeHolder")
public class DataTypeHolder {
	@Id
	@Column("id")
	private int id;
	@Column("int_val")
	private int intVal;
	@Column("boolean_val")
	private boolean booleanVal;
	@Column("bigint_val")
	private long bigintVal;
	@Column("decimal_val")
	private BigDecimal decimalVal;
	@Column("double_val")
	private double doubleVal;
	@Column("real_val")
	private float realVal;
	@Column("date_val")
	private Date dateVal;
	@IsTimestamp
	@Column("timestamp_val")
	private Date timestampVal;
	@Column("varchar_val")
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
	
	public DataTypeHolder() {}
	
	public DataTypeHolder(
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (bigintVal ^ (bigintVal >>> 32));
		result = prime * result + (booleanVal ? 1231 : 1237);
		result = prime * result + ((dateVal == null) ? 0 : dateVal.hashCode());
		result = prime * result + HashEqualsUtils.hash(decimalVal);
		long temp;
		temp = Double.doubleToLongBits(doubleVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + id;
		result = prime * result + intVal;
		result = prime * result + Float.floatToIntBits(realVal);
		result = prime * result + ((timestampVal == null) ? 0 : timestampVal.hashCode());
		result = prime * result + ((varcharVal == null) ? 0 : varcharVal.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataTypeHolder other = (DataTypeHolder) obj;
		if (bigintVal != other.bigintVal)
			return false;
		if (booleanVal != other.booleanVal)
			return false;
		if (dateVal == null) {
			if (other.dateVal != null)
				return false;
		} else if (!dateVal.equals(other.dateVal))
			return false;
		if (decimalVal == null) {
			if (other.decimalVal != null)
				return false;
		} else if (!HashEqualsUtils.equal(decimalVal, other.decimalVal))
			return false;
		if (Double.doubleToLongBits(doubleVal) != Double.doubleToLongBits(other.doubleVal))
			return false;
		if (id != other.id)
			return false;
		if (intVal != other.intVal)
			return false;
		if (Float.floatToIntBits(realVal) != Float.floatToIntBits(other.realVal))
			return false;
		if (timestampVal == null) {
			if (other.timestampVal != null)
				return false;
		} else if (!timestampVal.equals(other.timestampVal))
			return false;
		if (varcharVal == null) {
			if (other.varcharVal != null)
				return false;
		} else if (!varcharVal.equals(other.varcharVal))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return Integer.toString(id);
	}
}