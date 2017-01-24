package chan.shundat.albert.sqlbuilder.parser

import spock.lang.Specification

class ParseTreeSpec extends Specification {
	def 'Create string literal parse token'() {
		given: "list of tokens that represent the string 'abc''', and a parse tree to parse the tokens"
		List<String> tokens = [ "'", 'abc', "'", "'", "'" ]
		ParseTree tree = new ParseTree(tokens)
		
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
}