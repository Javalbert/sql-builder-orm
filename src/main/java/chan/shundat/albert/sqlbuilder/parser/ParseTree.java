package chan.shundat.albert.sqlbuilder.parser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import chan.shundat.albert.sqlbuilder.Keywords;
import chan.shundat.albert.utils.collections.CollectionUtils;

public class ParseTree {
	public static final String TOKEN_PARENTHESES_GROUP = "SQL PARENTHESES GROUP";
	public static final String TOKEN_ROOT = "SQL ROOT";

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
	private static final Set<String> BEFORE_ORDER_BY = CollectionUtils.immutableHashSet(
			Keywords.GROUP_BY,
			Keywords.FROM,
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
	
	private static void throwExpectTokenException(String expectedToken, String tokenBeforeExpectedToken) {
		throw new IllegalArgumentException("Expecting a " + expectedToken + " after " + tokenBeforeExpectedToken);
	}
	
	private static boolean tokenAhead(List<String> tokens, int i, String tokenAhead) {
		int nextIndex = i + 1;
		String token = nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
		return tokenAhead.equals(token);
	}
	
	private ParseToken currentToken;
	private final Stack<Integer> parenthesesStack = new Stack<>();
	private StringBuilder stringLiteral;
	private List<String> tokens;
	private final Stack<ParseToken> tokenStack = new Stack<>();
	
	public ParseTree(List<String> tokens) {
		currentToken = new ParseToken(TOKEN_ROOT);
		this.tokens = Collections.unmodifiableList(tokens);
		tokenStack.push(currentToken);
	}
	
	public ParseToken parseTokens() {
		for (int i = 0; i < tokens.size(); i++) {
			currentToken = tokenStack.peek();

				ParseToken parseToken = null;
			final String token = tokens.get(i);
			
			if (stringLiteral != null) {
				String nextToken = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
				
				if (token.equals("'")) {
					if ("'".equals(nextToken)) {
						stringLiteral.append("'");
						i++;
					} else {
						parseToken = new StringLiteralParseToken(stringLiteral.toString());
						currentToken.addNode(parseToken);
						stringLiteral = null;
					}
				} else {
					stringLiteral.append(token);
				}
				
				continue;
			}
			
			final String tokenUpperCase = token.toUpperCase();
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
					parseToken = new ParseToken(token, true);
					currentToken.addNode(parseToken);
					popStack();
					break;
				case Keywords.GROUP:
					assertAhead(tokens, i, Keywords.BY);
					i++;
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
					if (tokenAhead(tokens, i, Keywords.NOT)) {
						if (tokenAhead(tokens, i + 1, Keywords.NULL)) {
							i += 2;
							parseToken = new ParseToken(Keywords.IS_NOT_NULL, true);
						} else {
							throwExpectTokenException(Keywords.NULL, Keywords.NOT);
						}
					} else if (tokenAhead(tokens, i, Keywords.NULL)) {
						i++;
						parseToken = new ParseToken(Keywords.IS_NULL, true);
					} else {
						throwExpectTokenException(Keywords.NULL, tokenUpperCase);
					}
					currentToken.addNode(parseToken);
					break;
				case Keywords.INNER:
					assertAhead(tokens, i++, Keywords.JOIN);
					// Fall through to JOIN
				case Keywords.JOIN:
					parseToken = new ParseToken(Keywords.INNER_JOIN, true);
					currentToken.addNode(parseToken);
					break;
				case Keywords.NOT:
					if (tokenAhead(tokens, i, Keywords.BETWEEN)) {
						parseToken = new ParseToken(Keywords.NOT_BETWEEN, true);
					} else if (tokenAhead(tokens, i, Keywords.EXISTS)) {
						parseToken = new ParseToken(Keywords.NOT_EXISTS, true);
					} else if (tokenAhead(tokens, i, Keywords.IN)) {
						parseToken = new ParseToken(Keywords.NOT_IN, true);
					} else if (tokenAhead(tokens, i, Keywords.LIKE)) {
						parseToken = new ParseToken(Keywords.NOT_LIKE, true);
					} else {
						throw new IllegalArgumentException("Expecting { " + Keywords.BETWEEN 
								+ " | " + Keywords.EXISTS 
								+ " | " + Keywords.IN 
								+ " | " + Keywords.LIKE 
								+ " } after " + tokenUpperCase);
					}
					i++;
					currentToken.addNode(parseToken);
					break;
				case Keywords.ORDER:
					assertAhead(tokens, i, Keywords.BY);
					i++;
					popBackToBefore(BEFORE_ORDER_BY);
					
					parseToken = new ParseToken(Keywords.ORDER_BY, true);
					addAndPush(parseToken);
					break;
				case Keywords.FULL:
				case Keywords.LEFT:
				case Keywords.RIGHT:
					if (tokenAhead(tokens, i, Keywords.OUTER)) {
						if (tokenAhead(tokens, i + 1, Keywords.JOIN)) {
							i += 2;
						} else {
							throwExpectTokenException(Keywords.JOIN, Keywords.OUTER);
						}
					} else if (tokenAhead(tokens, i, Keywords.JOIN)) {
						i++;
					} else {
						throwExpectTokenException(Keywords.JOIN, tokenUpperCase);
					}

					switch (tokenUpperCase) {
						case Keywords.FULL: parseToken = new ParseToken(Keywords.FULL_OUTER_JOIN, true); break;
						case Keywords.LEFT: parseToken = new ParseToken(Keywords.LEFT_OUTER_JOIN, true); break;
						case Keywords.RIGHT: parseToken = new ParseToken(Keywords.RIGHT_OUTER_JOIN, true); break;
					}
					currentToken.addNode(parseToken);
					break;
				case Keywords.CASE:
				case Keywords.SELECT:
				case Keywords.SET:
					parseToken = new ParseToken(token, true);
					addAndPush(parseToken);
					break;
				case Keywords.EXCEPT:
				case Keywords.INTERSECT:
				case Keywords.UNION:
					popBackToBefore(BEFORE_ORDER_BY);
					
					if (tokenUpperCase.equals(Keywords.UNION) 
							&& tokenAhead(tokens, i, Keywords.ALL)) {
						parseToken = new ParseToken(Keywords.UNION_ALL, true);
						i++;
					} else {
						parseToken = new ParseToken(token, true);
					}
					
					currentToken.addNode(parseToken);
					break;
				case Keywords.FROM:
				case Keywords.WHERE:
					switch (tokenUpperCase) {
						case Keywords.FROM: popBackToBefore(BEFORE_FROM); break;
						case Keywords.WHERE: popBackToBefore(BEFORE_WHERE); break;
					}
					parseToken = new ParseToken(token, true);
					addAndPush(parseToken);
					break;
				default:
					parseToken = new ParseToken(token, Keywords.KEYWORD_SET.contains(token));
					currentToken.addNode(parseToken);
					break;
			}
		}
		
		return returnRoot();
	}

	private void addAndPush(ParseToken token) {
		currentToken.addNode(token);
		tokenStack.push(token);
		
		if (token.getToken().equals(TOKEN_PARENTHESES_GROUP)) {
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
	
	private void popBackToBefore(Set<String> tokens) {
		if (tokenStack.size() == 1) {
			return;
		}
		
		ParseToken poppedToken = null;
		do {
			poppedToken = popStack();
			currentToken = tokenStack.peek();
		} while (!tokens.contains(poppedToken.getToken()) 
				&& !currentToken.getToken().equals(TOKEN_ROOT));
	}
	
	private void popParenthesesStack() {
		int len = !parenthesesStack.isEmpty() ? parenthesesStack.pop() : 1;
		for (int i = 0; i < len; i++) {
			tokenStack.pop();
		}
	}
	
	private ParseToken popStack() {
		ParseToken poppedToken = tokenStack.pop();
		downsizeCurrentParentheses();
		return poppedToken;
	}
	
	private ParseToken returnRoot() {
		while (tokenStack.size() > 1) {
			tokenStack.pop();
		}
		return tokenStack.pop();
	}
}