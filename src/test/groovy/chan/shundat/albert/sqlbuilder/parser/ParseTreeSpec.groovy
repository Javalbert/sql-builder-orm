package chan.shundat.albert.sqlbuilder.parser

import chan.shundat.albert.sqlbuilder.SqlParser
import spock.lang.Specification

class ParseTreeSpec extends Specification {
	private SqlParser parser
	
	def setup() {
		parser = new SqlParser()
	}
	
	private ParseTree newTree(String sql) {
		return new ParseTree(parser.tokenize(sql))
	}
	
	def 'Create string literal parse token'() {
		given: "list of tokens that represent the string 'abc'''"
		ParseTree tree = newTree("'abc'''")
		
		when: 'parsed token by token'
		tree.parseNextToken()
		boolean stringLiteralCreated = tree.stringLiteral != null
		
		tree.parseNextToken()
		boolean appendedAbc = tree.stringLiteral.toString().endsWith('abc')
		
		int currentTokenIndex = tree.currentTokenIndex
		tree.parseNextToken()
		boolean appendedQuote = tree.stringLiteral.toString().endsWith("'")
		boolean skippedEscapedQuote = currentTokenIndex + 2 == tree.currentTokenIndex
		
		tree.parseNextToken()
		StringLiteralParseToken literalToken = tree.currentParseToken.nodes[0]
		
		then: 'string literal builder is created after detecting the starting quote of the string'
		stringLiteralCreated
		
		and: 'then string literal appended "abc"'
		appendedAbc
		
		and: 'then string literal appended "\'" and skipped the next (escaped) quote'
		appendedQuote
		skippedEscapedQuote
		
		and: 'finally, a parse token is created with value "abc\'", after detecting the ending quote'
		literalToken.value == "abc'"
	}
	
	def 'Test token stacks and child nodes using SELECT and FROM clauses'() {
		given: 'SQL string "SELECT col1 FROM tbl"'
		String sql = 'SELECT col1 FROM tbl'
		ParseTree tree = newTree(sql)
		
		when: 'parsing "SELECT"'
		tree.parseNextToken()
		ParseToken selectToken = tree.rootToken.nodes[0]
		boolean addedSelectToRoot = selectToken.token == 'SELECT'
		boolean addedSelectToTokenStack = tree.tokenStack.peek() == selectToken
		
		and: 'then parsing column "col1"'
		tree.parseNextToken()
		boolean col1IsNodeOfSelect = selectToken.nodes[0].token == 'col1'
		
		and: 'then parsing "FROM"'
		tree.parseNextToken()
		ParseToken fromToken = tree.rootToken.nodes[1]
		boolean addedFromToRoot = fromToken.token == 'FROM'
		boolean selectPoppedFromTokenStack = tree.tokenStack.stream().allMatch{ it.token != 'SELECT' }
		boolean addedFromToTokenStack = tree.tokenStack.peek() == fromToken
		
		and: 'then parsing "tbl"'
		tree.parseNextToken()
		boolean tblIsNodeOfFrom = fromToken.nodes[0].token == 'tbl'
		
		then: 'SELECT is added as a node of SQL ROOT and pushed onto the token stack'
		addedSelectToRoot
		addedSelectToTokenStack
		
		and: 'then col1 is added as a node of SELECT token'
		col1IsNodeOfSelect
		
		and: 'then SELECT is popped from token stack with FROM replacing it, and FROM is added as a node of SQL ROOT'
		selectPoppedFromTokenStack
		addedFromToTokenStack
		addedFromToRoot
		
		and: 'then tbl is added as a node of FROM token'
		tblIsNodeOfFrom
	}
	
	def 'Verify that GROUP should be followed by BY, otherwise throw an error'() {
		given: '"GROUP BY"'
		ParseTree groupByTree = newTree('GROUP BY')
		
		and: '"GROUP" without the "BY"'
		ParseTree groupTree = newTree('GROUP')
		
		when: 'parsing "GROUP BY"'
		groupByTree.parseTokens()
		boolean addedGroupByToTokenStack = groupByTree.rootToken.nodes[0].token == 'GROUP BY'
		
		and: 'parsing "GROUP"'
		groupTree.parseTokens()
		
		then: '"GROUP BY" works, but "GROUP" throws error'
		addedGroupByToTokenStack
		thrown(IllegalArgumentException)
	}
	
	def 'Test the state of parentheses stack'() {
		given: 'SQL string "(SELECT col1 FROM tbl)"'
		ParseTree tree = newTree('(SELECT col1 FROM tbl)')
		
		when: 'parsing "("'
		tree.parseNextToken()
		boolean addedParenthesesGroupToTokenStack = tree.tokenStack.peek().token == ParseTree.TOKEN_PARENTHESES_GROUP
		int parenthesesGroupSizeAfterAddingGroup = tree.parenthesesStack.peek()
		
		and: 'then parsing "SELECT"'
		tree.parseNextToken()
		int parenthesesGroupSizeAfterAddingSelect = tree.parenthesesStack.peek()
		int tokenStackSizeAfterAddingSelect = tree.tokenStack.size()
		
		and: 'then parsing "col1 FROM tbl"'
		tree.parseNextTokensUntil(')')
		int parenthesesGroupSizeAfterAddingFrom = tree.parenthesesStack.peek()
		int tokenStackSizeAfterAddingFrom = tree.tokenStack.size()
		
		and: 'then parsing ")"'
		tree.parseNextToken()
		boolean parenthesesStackIsEmpty = tree.parenthesesStack.isEmpty()
		int tokenStackSizeAfterEndingParenthesis = tree.tokenStack.size()
		
		then: 'parentheses group token is added to token stack, and parentheses group size is 1'
		addedParenthesesGroupToTokenStack
		parenthesesGroupSizeAfterAddingGroup == 1
		
		and: 'then parentheses group size is 2 after adding SELECT, and token stack size is 3'
		parenthesesGroupSizeAfterAddingSelect == 2
		tokenStackSizeAfterAddingSelect == 3
		
		and: 'then parentheses group size is still 2 after adding FROM, and token stack size is still 3'
		parenthesesGroupSizeAfterAddingFrom == 2
		tokenStackSizeAfterAddingFrom == 3
		
		and: 'then parentheses stack is empty, and token stack size is 1 after popping out FROM and parentheses group'
		parenthesesStackIsEmpty
		tokenStackSizeAfterEndingParenthesis == 1
	}
}