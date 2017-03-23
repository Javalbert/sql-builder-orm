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
package com.github.javalbert.utils.reflection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface MemberAccess {
	Object get(Object instance);
	void set(Object instance, Object x);
	
	void setBoolean(Object instance, boolean x);
	void setDouble(Object instance, double x);
	void setFloat(Object instance, float x);
	void setInt(Object instance, int x);
	void setLong(Object instance, long x);
	
	void setBoxedBoolean(Object instance, Boolean x);
	void setBoxedDouble(Object instance, Double x);
	void setBoxedFloat(Object instance, Float x);
	void setBoxedInt(Object instance, Integer x);
	void setBoxedLong(Object instance, Long x);
	
	void setBigDecimal(Object instance, BigDecimal x);
	void setDate(Object instance, Date x);
	void setLocalDate(Object instance, LocalDate x);
	void setLocalDateTime(Object instance, LocalDateTime x);
	void setString(Object instance, String x);
}