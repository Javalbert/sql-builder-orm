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
package chan.shundat.albert.sqlbuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlStringUtils {
	public static String createLiteralToken(String str) {
		return createLiteralToken(str, true);
	}
	
	public static String createLiteralToken(String str, boolean quotesAreOptional) {
		if (!quotesAreOptional 
				|| containsSpacesOrQuotes(str) 
				|| Keywords.KEYWORD_SET.contains(str.toUpperCase())) {
			str = "'" + str.replace("'", "''") + "'";
		}
		return str;
	}
	
	private static boolean containsSpacesOrQuotes(String str) {
		Pattern pattern = Pattern.compile(".*\\s.*|'");
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}
	
	private SqlStringUtils() {}
}