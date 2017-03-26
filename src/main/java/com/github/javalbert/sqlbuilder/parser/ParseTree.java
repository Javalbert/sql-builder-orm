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
package com.github.javalbert.sqlbuilder.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.javalbert.sqlbuilder.Keywords;
import com.github.javalbert.utils.collections.CollectionUtils;

public class ParseTree {
	public static final String TOKEN_PARENTHESES_GROUP = "SQL PARENTHESES GROUP";
	public static final String TOKEN_ROOT = "SQL ROOT";

	private static final Set<String> BEFORE_FETCH = CollectionUtils.immutableHashSet(
			Keywords.GROUP_BY,
			Keywords.FROM,
			Keywords.HAVING,
			Keywords.ORDER_BY,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_FROM = CollectionUtils.immutableHashSet(
			Keywords.SELECT
			);
	private static final Set<String> BEFORE_GROUP_BY = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_HAVING = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.GROUP_BY,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_OFFSET = BEFORE_FETCH;
	private static final Set<String> BEFORE_ORDER_BY = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.GROUP_BY,
			Keywords.HAVING,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_WHERE = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.SELECT
			);
	
	private static void assertAhead(List<String> tokens, int i, String tokenAhead) {
		int nextIndex = i + 1;
		String token = nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
		
		if (token == null || !token.toUpperCase().equals(tokenAhead)) {
			throwExpectTokenException(tokenAhead, tokens.get(i));
		}
	}
	
	private static String getTokenAhead(List<String> tokens, int i) {
		int nextIndex = i + 1;
		return nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
	}
	
	private static void throwExpectTokenException(String expectedToken, String tokenBeforeExpectedToken) {
		throw new IllegalArgumentException("Expecting a " + expectedToken + " after " + tokenBeforeExpectedToken);
	}
	
	private ParseToken currentParseToken;
	private String currentToken;
	private int currentTokenIndex = -1;
	private final Deque<Integer> parenthesesStack = new ArrayDeque<>();
	private final ParseToken rootToken;
	private StringBuilder stringLiteral;
	private List<String> tokens;
	private final Deque<ParseToken> tokenStack = new ArrayDeque<>();
	
	public ParseTree(List<String> tokens) {
		rootToken = new ParseToken(TOKEN_ROOT);
		currentParseToken = rootToken;
		this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
		tokenStack.push(currentParseToken);
	}
	
	public ParseToken parseTokens() {
		while (parseNextToken());
		return rootToken;
	}
	
	boolean parseNextToken() {
		if (!hasNextToken()) {
			return false;
		}
		nextToken();
		parseCurrentToken();
		return true;
	}
	
	void parseNextTokensUntil(String token) {
		while (parseNextToken()) {
			if (!hasNextToken() 
					|| token.equals(tokens.get(currentTokenIndex + 1))) {
				break;
			}
		}
	}
	
	private boolean hasNextToken() {
		return currentTokenIndex + 1 < tokens.size();
	}
	
	private void addAndPush(ParseToken token) {
		currentParseToken.addNode(token);
		tokenStack.push(token);
		
		if (token.getToken().equals(TOKEN_PARENTHESES_GROUP)) {
			// 1 is the TOKEN_PARENTHESES_GROUP token itself
			parenthesesStack.push(1);
		} else if (!parenthesesStack.isEmpty()) {
			int size = parenthesesStack.pop();
			parenthesesStack.push(size + 1);
		}
	}
	
	private void downsizeCurrentParentheses() {
		if (!parenthesesStack.isEmpty()) {
			int size = parenthesesStack.pop();
			size--;
			
			if (size > 0) {
				parenthesesStack.push(size);
			}
		}
	}
	
	private boolean handleStringLiteral() {
		if (stringLiteral == null) {
			return false;
		}
		String nextToken = hasNextToken() ? tokens.get(currentTokenIndex + 1) : null;
		
		if (!currentToken.equals("'")) {
			stringLiteral.append(currentToken);
		} else if ("'".equals(nextToken)) {
			stringLiteral.append("'");
			currentTokenIndex++;
		} else {
			ParseToken parseToken = new StringLiteralParseToken(stringLiteral.toString());
			currentParseToken.addNode(parseToken);
			stringLiteral = null;
		}
		
		return true;
	}
	
	private void nextToken() {
		currentTokenIndex++;

		currentParseToken = tokenStack.peek();
		currentToken = tokens.get(currentTokenIndex);
	}
	
	private void parseCurrentToken() {
		if (handleStringLiteral()) {
			return;
		}
		
		ParseToken parseToken = null;
		String tokenAhead;
		
		final String tokenUpperCase = currentToken.toUpperCase();
		switch (tokenUpperCase) {
			case "":
				break;
			case "'":
				if (stringLiteral == null) {
					stringLiteral = new StringBuilder();
				}
				break;
			case "(":
				parseToken = new ParseToken(TOKEN_PARENTHESES_GROUP);
				addAndPush(parseToken);
				break;
			case ")":
				popParenthesesStack();
				break;
			case Keywords.END:
				parseToken = new ParseToken(currentToken, true);
				currentParseToken.addNode(parseToken);
				popStack();
				break;
			case Keywords.FETCH:
				popBackToBefore(BEFORE_FETCH);
				parseToken = new ParseToken(Keywords.FETCH, true);
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.GROUP:
				assertAhead(tokens, currentTokenIndex, Keywords.BY);
				currentTokenIndex++;
				popBackToBefore(BEFORE_GROUP_BY);
				
				parseToken = new ParseToken(Keywords.GROUP_BY, true);
				addAndPush(parseToken);
				break;
			case Keywords.HAVING:
				popBackToBefore(BEFORE_HAVING);
				parseToken = new ParseToken(Keywords.HAVING, true);
				addAndPush(parseToken);
				break;
			case Keywords.IS:
				tokenAhead = getTokenAhead(tokens, currentTokenIndex);
				
				if (Keywords.NOT.equals(tokenAhead)) {
					String nullToken = getTokenAhead(tokens, currentTokenIndex + 1);
					if (Keywords.NULL.equals(nullToken)) {
						currentTokenIndex += 2;
						parseToken = new ParseToken(Keywords.IS_NOT_NULL, true);
					} else {
						throwExpectTokenException(Keywords.NULL, Keywords.NOT);
					}
				} else if (Keywords.NULL.equals(tokenAhead)) {
					currentTokenIndex++;
					parseToken = new ParseToken(Keywords.IS_NULL, true);
				} else {
					throwExpectTokenException(Keywords.NULL, tokenUpperCase);
				}
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.INNER:
				assertAhead(tokens, currentTokenIndex++, Keywords.JOIN);
				// Fall through to JOIN
			case Keywords.JOIN:
				parseToken = new ParseToken(Keywords.INNER_JOIN, true);
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.NOT:
				tokenAhead = getTokenAhead(tokens, currentTokenIndex);
				
				if (Keywords.MATCHED.equals(tokenAhead)) {
					parseToken = new ParseToken(Keywords.NOT, true);
				} else {
					if (Keywords.BETWEEN.equals(tokenAhead)) {
						parseToken = new ParseToken(Keywords.NOT_BETWEEN, true);
					} else if (Keywords.EXISTS.equals(tokenAhead)) {
						parseToken = new ParseToken(Keywords.NOT_EXISTS, true);
					} else if (Keywords.IN.equals(tokenAhead)) {
						parseToken = new ParseToken(Keywords.NOT_IN, true);
					} else if (Keywords.LIKE.equals(tokenAhead)) {
						parseToken = new ParseToken(Keywords.NOT_LIKE, true);
					} else {
						throw new IllegalArgumentException("Expecting { "
								+ Keywords.BETWEEN
								+ " | " + Keywords.EXISTS
								+ " | " + Keywords.IN
								+ " | " + Keywords.LIKE
								+ " | " + Keywords.MATCHED
								+ " } after " + tokenUpperCase);
					}
					currentTokenIndex++;
				}
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.OFFSET:
				popBackToBefore(BEFORE_OFFSET);
				parseToken = new ParseToken(Keywords.OFFSET, true);
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.ORDER:
				assertAhead(tokens, currentTokenIndex, Keywords.BY);
				currentTokenIndex++;
				popBackToBefore(BEFORE_ORDER_BY);
				
				parseToken = new ParseToken(Keywords.ORDER_BY, true);
				addAndPush(parseToken);
				break;
			case Keywords.FULL:
			case Keywords.LEFT:
			case Keywords.RIGHT:
				tokenAhead = getTokenAhead(tokens, currentTokenIndex);
				
				if (Keywords.OUTER.equals(tokenAhead)) {
					String joinToken = getTokenAhead(tokens, currentTokenIndex + 1);
					if (Keywords.JOIN.equals(joinToken)) {
						currentTokenIndex += 2;
					} else {
						throwExpectTokenException(Keywords.JOIN, Keywords.OUTER);
					}
				} else if (Keywords.JOIN.equals(tokenAhead)) {
					currentTokenIndex++;
				} else {
					throwExpectTokenException(Keywords.JOIN, tokenUpperCase);
				}

				switch (tokenUpperCase) {
					case Keywords.FULL: parseToken = new ParseToken(Keywords.FULL_OUTER_JOIN, true); break;
					case Keywords.LEFT: parseToken = new ParseToken(Keywords.LEFT_OUTER_JOIN, true); break;
					case Keywords.RIGHT: parseToken = new ParseToken(Keywords.RIGHT_OUTER_JOIN, true); break;
				}
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.CASE:
			case Keywords.SELECT:
			case Keywords.SET:
				parseToken = new ParseToken(currentToken, true);
				addAndPush(parseToken);
				break;
			case Keywords.EXCEPT:
			case Keywords.INTERSECT:
			case Keywords.UNION:
				popBackToBefore(BEFORE_ORDER_BY);
				
				if (tokenUpperCase.equals(Keywords.UNION) 
						&& Keywords.ALL.equals(getTokenAhead(tokens, currentTokenIndex))) {
					parseToken = new ParseToken(Keywords.UNION_ALL, true);
					currentTokenIndex++;
				} else {
					parseToken = new ParseToken(currentToken, true);
				}
				
				currentParseToken.addNode(parseToken);
				break;
			case Keywords.FROM:
			case Keywords.WHERE:
				switch (tokenUpperCase) {
					case Keywords.FROM: popBackToBefore(BEFORE_FROM); break;
					case Keywords.WHERE: popBackToBefore(BEFORE_WHERE); break;
				}
				parseToken = new ParseToken(currentToken, true);
				addAndPush(parseToken);
				break;
			default:
				if (StringUtils.isNumeric(currentToken)) {
					parseToken = new NumberLiteralParseToken(currentToken);
					currentParseToken.addNode(parseToken);
				} else {
					parseToken = new ParseToken(currentToken, Keywords.KEYWORD_SET.contains(currentToken));
					currentParseToken.addNode(parseToken);
				}
				break;
		}
	}
	
	private void popBackToBefore(Set<String> tokens) {
		if (tokenStack.size() == 1) {
			return;
		}
		
		ParseToken poppedToken = null;
		do {
			poppedToken = popStack();
			currentParseToken = tokenStack.peek();
		} while (!tokens.contains(poppedToken.getToken()) 
				&& !currentParseToken.getToken().equals(TOKEN_ROOT));
	}
	
	private void popParenthesesStack() {
		int len = !parenthesesStack.isEmpty() ? parenthesesStack.pop() : 1;
		for (int i = 0; i < len; i++) {
			// Pop every token that was pushed into tokenStack that was inside parentheses
			// including the TOKEN_PARENTHESES_GROUP token
			tokenStack.pop();
		}
	}
	
	private ParseToken popStack() {
		ParseToken poppedToken = tokenStack.pop();
		downsizeCurrentParentheses();
		return poppedToken;
	}
}