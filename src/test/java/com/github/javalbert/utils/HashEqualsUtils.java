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
package com.github.javalbert.utils;

import java.math.BigDecimal;

public final class HashEqualsUtils {
	public static boolean equal(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) == 0;
	}
	
	/**
	 * CREDIT: <a href="http://stackoverflow.com/a/14313302">Stack Overflow</a>
	 * @return
	 */
	public static int hash(BigDecimal x) {
		if (x == null) {
			return 0;
		}
		long temp = Double.doubleToLongBits(x.doubleValue());
		return (int) (temp ^ (temp >>> 32));
	}
	
	private HashEqualsUtils() {}
}