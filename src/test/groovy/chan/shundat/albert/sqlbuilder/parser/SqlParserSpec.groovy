package chan.shundat.albert.sqlbuilder.parser

import chan.shundat.albert.sqlbuilder.Delete
import chan.shundat.albert.sqlbuilder.Insert
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.SetValues
import chan.shundat.albert.sqlbuilder.SqlStatement
import chan.shundat.albert.sqlbuilder.Table
import chan.shundat.albert.sqlbuilder.Update
import spock.lang.Specification
import spock.lang.Unroll

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
	
	@Unroll('Parsing a #sql statement should create an instance of #clazz')
	def 'Parse different kinds of SQL statements and retrieve an instance of the appropriate class'() {
		expect: 'the appropriate class to be created for each sql'
		parser.parse(sql)
		SqlStatement stmt = parser.sqlStatement
		stmt.getClass() == clazz
		
		where: 'each type of SQL string corresponds with a class of their namesake'
		sql			||	clazz
		'DELETE'	||	Delete.class
		'INSERT'	||	Insert.class
		'SELECT'	||	Select.class
		'UPDATE'	||	Update.class
	}
	
	def 'Parse DELETE statement'() {
		given: 'SQL string "DELETE FROM Albert.dbo.Person"'
		String sql = "DELETE FROM Albert.dbo.Person"
		
		and: 'Delete object, list of parse tokens, and current index of list starting at zero'
		Delete delete = new Delete()
		List<ParseToken> deleteTokens = parser.sqlToParseTree(sql).nodes
		int deleteNodeIndex = 0
		StringBuilder tableName = new StringBuilder()
		
		when: 'parsing "DELETE"'
		parser.parseDeleteNode(delete, deleteTokens[deleteNodeIndex++], tableName)
		boolean noNodesAddedToDeleteYet = delete.nodes.isEmpty()
		
		and: 'then parsing "FROM"'
		parser.parseDeleteNode(delete, deleteTokens[deleteNodeIndex++], tableName)
		Table table = delete.nodes[0]
		
		then: 'nothing is added to Delete yet'
		noNodesAddedToDeleteYet
		
		and: 'then "Albert.dbo.Person" is added'
		table.name == 'Albert.dbo.Person'
	}
	
	def 'Parse SET nodes of UPDATE statement'() {
		given: "SQL string \"UPDATE Albert.dbo.Person SET name = '@183R7'\""
		String sql = "UPDATE Albert.dbo.Person SET name = '@183R7'"
		
		and: 'child nodes of SET node, and SetValues object'
		List<ParseToken> nodes = parser.sqlToParseTree(sql).nodes[6].nodes
		SetValues values = new SetValues()
		
		when: ''
//		parser.parseSetValues(values, nodes)
		
		then: ''
		
	}
}