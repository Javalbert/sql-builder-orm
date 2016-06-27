/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.utils.string;

import java.text.DecimalFormatSymbols;

public final class Strings {
	public static boolean areEqual(String str1, String str2) {
		if (str1 == null) {
			return str2 == null;
		}
		return str1.equals(str2);
	}
	
	public static boolean endsWithIgnoreCase(String str, String... endingStrings) {
		boolean endsWith = false;
		
		for (String endingString : endingStrings) {
			boolean endsWithString = true;
			
			if (str.length() >= endingString.length()) {
				for (int strI = str.length() - 1, endingI = endingString.length() - 1; 
						endingI >= 0; 
						strI--, endingI--) {
					char charInStr = Character.toUpperCase(str.charAt(strI));
					char charInEndingString = Character.toUpperCase(endingString.charAt(endingI));
					
					boolean equal = charInStr == charInEndingString;
					if (!equal) {
						/* See source code of String.equalsIgnoreCase() */
						charInStr = Character.toLowerCase(charInStr);
						charInEndingString = Character.toLowerCase(charInEndingString);
						
						equal = charInStr == charInEndingString;
					}
					
					if (!equal) {
						endsWithString = false;
						break;
					}
				}
			} else {
				continue;
			}
			
			if (endsWithString) {
				endsWith = true;
				break;
			}
		}
		
		return endsWith;
	}
	
	/**
	 * This is useful as input to Pattern.compile(regex) where regex contains text that needs to be escaped from regex
	 * @param str
	 * @return
	 */
	public static String escapeRegexText(String str) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			switch (c) {
				case '.':
				case '\\':
				case '+':
				case '*':
				case '?':
				case '^':
				case '$':
				case '[':
				case ']':
				case '(':
				case ')':
				case '|':
				case '/':
					builder.append("\\");
					break;
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
    public static String ifNull(Object obj, String nullReplacement) {
        return ifNull(obj != null
                ? obj.toString()
                : null, nullReplacement);
    }

    public static String ifNull(String str, String nullReplacement) {
        return str != null
                ? str
                : nullReplacement;
    }

    /**
     * Will also take into account leading and trailing spaces by trimming the string
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // CREDIT: http://stackoverflow.com/a/7092110
    public static boolean isNumeric(String str) {
    	if (str == null || str.isEmpty()) {
    		return false;
    	}
    	
        DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
        char localeMinusSign = currentLocaleSymbols.getMinusSign();

        if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign) {
        	return false;
        }

        boolean decimalSeparatorFound = false;
        char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

        for (int i = 1; i < str.length(); i++) {
        	char c = str.charAt(i);
        	
        	if (Character.isDigit(c)) {
        		continue;
        	} else if (c == localeDecimalSeparator && !decimalSeparatorFound) {
        		decimalSeparatorFound = true;
                continue;
            }
        	
            return false;
        }
        return true;
    }
    
    public static String join(String delimiter, Iterable<String> elements) {
    	StringBuilder builder = null;
    	for (String element : elements) {
    		if (builder != null) {
    			builder.append(delimiter + element);
    		} else {
    			builder = new StringBuilder().append(element);
    		}
    	}
    	return builder.toString();
    }

    public static String safeTrim(String str) {
        return str != null ? str.trim() : str;
    }
    
	public static boolean startsWithIgnoreCase(String str, String... startingStrings) {
		boolean startsWith = false;
		
		for (String startingString : startingStrings) {
			boolean startsWithString = true;
			
			if (str.length() >= startingString.length()) {
				for (int i = 0; i < startingString.length(); i++) {
					char charInStr = Character.toUpperCase(str.charAt(i));
					char charInStartingString = Character.toUpperCase(startingString.charAt(i));
					
					boolean equal = charInStr == charInStartingString;
					if (!equal) {
						/* See source code of String.equalsIgnoreCase() */
						charInStr = Character.toLowerCase(charInStr);
						charInStartingString = Character.toLowerCase(charInStartingString);
						
						equal = charInStr == charInStartingString;
					}
					
					if (!equal) {
						startsWithString = false;
						break;
					}
				}
			} else {
				continue;
			}
			
			if (startsWithString) {
				startsWith = true;
				break;
			}
		}
		
		return startsWith;
	}
	
	private Strings() {}
}