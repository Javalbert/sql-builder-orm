package chan.shundat.albert.sqlbuilder.parser

import chan.shundat.albert.sqlbuilder.Delete
import spock.lang.Specification

class SqlParserSpec extends Specification {
	private SqlParser parser
	
	def setup() {
		parser = new SqlParser()
	}
	
	def 'Verify token separation'() {
		given: 'SELECT SQL string'
		String sql =
"""SELECT a.c1,
	(a.c4 + a.c5) * a.c6,
	a.*
FROM catalog.schema.tableA a
WHERE (a.c2 = 'a' OR a.c2 <> ''' a b ''')
	AND a.c3 > 0
	AND a.c3 < 10"""

		// Regex is from SqlParser.REGEX_SPLIT
		when: "Tokenize the string using regex separator \\s|%|'|\\(|\\)|\\*|\\+|,|-|\\.|\\/|<=|<>|<|=|>=|>"
		parser.tokenize(sql)
		
		then: 'tokens must match a list of tokens'
		List tokensToMatch = [
			'SELECT', 'a', '.', 'c1', ',',
			'(', 'a', '.', 'c4', '+', 'a', '.', 'c5', ')', '*', 'a', '.', 'c6', ',',
			'a', '.', '*',
			'FROM', 'catalog', '.', 'schema', '.', 'tableA', 'a',
			'WHERE', '(', 'a', '.', 'c2', '=', "'", 'a', "'", 'OR', 'a', '.', 'c2', '<>', "'", "'", "'", ' ', 'a', ' ', 'b', ' ', "'", "'", "'", ')',
			'AND', 'a', '.', 'c3', '>', '0',
			'AND', 'a', '.', 'c3', '<', '10'
			]
		parser.tokens == tokensToMatch
	}
	
	def 'Parse DELETE statement'() {
		given: 'DELETE SQL string'
		String sql = """DELETE FROM Albert.dbo.Person"""
		
		when: 'Parsed'
		parser.parse(sql)
		
		then: 'SQL is a DELETE statement'
		parser.sqlStatement instanceof Delete
	}
}