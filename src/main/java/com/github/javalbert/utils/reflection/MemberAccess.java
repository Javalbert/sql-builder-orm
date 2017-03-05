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
import java.util.Date;

public interface MemberAccess<T> {
	Object get(T instance);
	void set(T instance, Object x);
	
	void setBoolean(T instance, boolean x);
	void setDouble(T instance, double x);
	void setFloat(T instance, float x);
	void setInt(T instance, int x);
	void setLong(T instance, long x);
	
	void setBoxedBoolean(T instance, Boolean x);
	void setBoxedDouble(T instance, Double x);
	void setBoxedFloat(T instance, Float x);
	void setBoxedInt(T instance, Integer x);
	void setBoxedLong(T instance, Long x);
	
	void setBigDecimal(T instance, BigDecimal x);
	void setDate(T instance, Date x);
	void setString(T instance, String x);
}