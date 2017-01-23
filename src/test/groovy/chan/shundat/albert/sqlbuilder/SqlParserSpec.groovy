package chan.shundat.albert.sqlbuilder

import spock.lang.Specification

class SqlParserSpec extends Specification {
	private SqlParser parser
	
	def setup() {
		parser = new SqlParser()
	}
	
	def 'Verify token separation'() {
		String sql = 
"""SELECT a.c1,
	(a.c4 + a.c5) * a.c6,
	a.*
FROM catalog.schema.tableA a
WHERE (a.c2 = 'a' OR a.c2 <> ''' a b ''')
	AND a.c3 > 0
	AND a.c3 < 10"""
		given: 'Complicated SELECT SQL string'
		
		when: "Tokenize the string using regex separator ${SqlParser.REGEX_SPLIT}"
		parser.tokenize(sql)
		
		then: 'Tokens must a list of tokens'
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
		String sql = """DELETE FROM Albert.dbo.Person"""
		given: sql
		
		when: 'Parsed'
		parser.parse(sql)
		
		then: 'SQL is a DELETE statement'
		parser.sqlStatement instanceof Delete
	}
}