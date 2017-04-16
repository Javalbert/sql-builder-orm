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
package com.github.javalbert.utils.string;

import java.util.Objects;

public final class Strings {
	/**
	 * Modeled after {@link Objects#requireNonNull(Object, String)}
	 * @param str
	 * @param message
	 * @return
     * @throws IllegalArgumentException if {@code str} is {@code null} or empty after being trimmed
	 */
	public static String illegalArgOnEmpty(String str, String message) {
		if (isNullOrEmpty(str)) {
			throw new IllegalArgumentException(message);
		}
		return str;
	}
	
	/**
	 * Modeled after {@link Objects#requireNonNull(Object, String)}
	 * @param str
	 * @param message
	 * @return
     * @throws IllegalStateException if {@code str} is {@code null} or empty after being trimmed
	 */
	public static String illegalStateOnEmpty(String str, String message) {
		if (isNullOrEmpty(str)) {
			throw new IllegalStateException(message);
		}
		return str;
	}
	
    /**
     * Will also take into account leading and trailing spaces by trimming the string
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static String safeTrim(String str) {
        return str != null ? str.trim() : str;
    }
	
	private Strings() {}
}