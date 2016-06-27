/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
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