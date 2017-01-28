package chan.shundat.albert.sqlbuilder.parser

import chan.shundat.albert.sqlbuilder.Column
import chan.shundat.albert.sqlbuilder.ColumnList
import chan.shundat.albert.sqlbuilder.ColumnValues
import chan.shundat.albert.sqlbuilder.Delete
import chan.shundat.albert.sqlbuilder.ExpressionBuilding
import chan.shundat.albert.sqlbuilder.Insert
import chan.shundat.albert.sqlbuilder.LiteralNumber
import chan.shundat.albert.sqlbuilder.LiteralString
import chan.shundat.albert.sqlbuilder.Node
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.SetValue
import chan.shundat.albert.sqlbuilder.SetValues
import chan.shundat.albert.sqlbuilder.SqlStatement
import chan.shundat.albert.sqlbuilder.Table
import chan.shundat.albert.sqlbuilder.Token
import chan.shundat.albert.sqlbuilder.Update
import chan.shundat.albert.sqlbuilder.Where
import chan.shundat.albert.sqlbuilder.parser.SqlParser.ExpressionCaseHelper
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
	
	def 'Parse DELETE statement and verify nodes'() {
		given: 'SQL string "DELETE FROM Albert.dbo.Person WHERE personKey = 1"'
		String sql = "DELETE FROM Albert.dbo.Person WHERE personKey = 1"
		
		and: 'Delete object, list of parse tokens, and current index of list starting at zero'
		Delete delete = new Delete()
		List<ParseToken> deleteTokens = parser.sqlToParseTree(sql).nodes
		int deleteNodeIndex = 0
		StringBuilder tableName = new StringBuilder()
		
		when: 'parsing "DELETE"'
		SqlParser.parseDeleteNode(delete, deleteTokens[deleteNodeIndex++], tableName)
		boolean noNodesAddedToDeleteYet = delete.nodes.isEmpty()
		
		and: 'then parsing "FROM"'
		SqlParser.parseDeleteNode(delete, deleteTokens[deleteNodeIndex++], tableName)
		Node table = delete.nodes[0]
		
		and: 'then parsing "WHERE"'
		SqlParser.parseDeleteNode(delete, deleteTokens[deleteNodeIndex++], tableName)
		Node where = delete.nodes[1]
		
		then: 'nothing is added to Delete yet'
		noNodesAddedToDeleteYet
		
		and: 'then Table object with name "Albert.dbo.Person" is added'
		table instanceof Table && table.name == 'Albert.dbo.Person'
		
		and: 'then Where object is added'
		where instanceof Where
	}
	
	def 'Parse SET nodes of UPDATE statement'() {
		given: "SQL string \"UPDATE Albert.dbo.Person SET name = '@183R7', age = 25\""
		String sql = "UPDATE Albert.dbo.Person SET name = '@183R7', age = 25"
		
		and: 'child nodes of SET node, and SetValues object'
		List<ParseToken> nodes = parser.sqlToParseTree(sql).nodes[6].nodes
		int nodeIndex = 0
		SetValues values = new SetValues()
		
		when: "parsing first set value \"name = '@183R7'\""
		nodeIndex = SqlParser.parseSetValue(values, nodes, nodeIndex)
		SetValue setName = values.nodes[0]
		Column nameColumn = setName.nodes[0]
		LiteralString nameLiteral = setName.nodes[1]
		
		and: "then parsing second set value \"age = 25\""
		SqlParser.parseSetValue(values, nodes, nodeIndex)
		SetValue setAge = values.nodes[1]
		Column ageColumn = setAge.nodes[0]
		LiteralNumber ageLiteral = setAge.nodes[1]
		
		then: 'first set value has column name "name" with string value "@183R7"'
		nameColumn.name == 'name'
		nameLiteral.value == '@183R7'
		
		then: 'second set value has column name "age" with number value 25'
		ageColumn.name == 'age'
		ageLiteral.value == 25
	}
	
	def 'Parse UPDATE statement with WHERE clause and verify nodes'() {
		given: "SQL string \"UPDATE Albert.dbo.Person SET name = '@183R7', age = 25 WHERE version = 3\""
		String sql = "UPDATE Albert.dbo.Person SET name = '@183R7', age = 25 WHERE version = 3"
		
		when: 'parsed and Update object retrieved'
		parser.parse(sql)
		Update update = parser.sqlStatement
		
		then: 'Update object has three (3) nodes'
		update.nodes.size() == 3
		
		and: 'first node is Table with name "Albert.dbo.Person"'
		Node table = update.nodes[0]
		table instanceof Table && table.name == 'Albert.dbo.Person'
		
		and: 'second node is SetValues object'
		Node setValues = update.nodes[1]
		setValues instanceof SetValues
		
		and: 'third node is Where object'
		Node where = update.nodes[2]
		where instanceof Where
	}
	
	def 'Parse column list in INSERT statement'() {
		given: "SQL string \"INSERT INTO Albert.dbo.Person (name, age) VALUES ('/\\|_83|27', 25)\""
		String sql = "INSERT INTO Albert.dbo.Person (name, age) VALUES ('/\\|_83|27', 25)"
		
		when: 'parsing the SQL and retrieving the parse token representing columns (name, age)'
		List<ParseToken> insertNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken columnListToken = insertNodes[7]
		
		and: 'then parsing the parse token as a column list'
		ColumnList columnList = new ColumnList()
		SqlParser.parseColumnList(columnList, columnListToken.nodes)
		Column nameColumn = columnList.nodes[0]
		Column ageColumn = columnList.nodes[1]
		
		then: 'name and age is added as nodes'
		nameColumn.name == 'name'
		ageColumn.name == 'age'
	}
	
	def 'Parse values list in INSERT statement'() {
		given: "SQL string \"INSERT INTO Albert.dbo.Person (name, version) VALUES ('/\\|_83|27', DEFAULT)\""
		String sql = "INSERT INTO Albert.dbo.Person (name, version) VALUES ('/\\|_83|27', DEFAULT)"
		
		and: 'parentheses group token after VALUES'
		List<ParseToken> insertNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken parenthesesToken = insertNodes[9]
		ColumnValues columnValues = new ColumnValues()
		int i = 0
		ExpressionCaseHelper helper = new ExpressionCaseHelper(columnValues)
		
		when: 'parsing "/\\|_83|27"'
		SqlParser.parseInsertValue(columnValues, parenthesesToken.nodes[i], i++, helper)
		LiteralString nameLiteral = columnValues.nodes[0]
		
		and: 'then parsing ","'
		SqlParser.parseInsertValue(columnValues, parenthesesToken.nodes[i], i++, helper)
		boolean commaIgnored = columnValues.nodes.size()
		
		and: 'then parsing DEFAULT keyword'
		SqlParser.parseInsertValue(columnValues, parenthesesToken.nodes[i], i++, helper)
		Token defaultKeywordToken = columnValues.nodes[1]
		
		then: 'literal string node is created for value of name'
		nameLiteral.value == '/\\|_83|27'
		
		and: 'then comma is ignored'
		commaIgnored
		
		and: 'then constant token representing DEFAULT keyword is added as a node'
		defaultKeywordToken == ColumnValues.DEFAULT
	}
	
	def 'Parse INSERT statement and verify nodes'() {
		given: "SQL string \"INSERT INTO Albert.dbo.Person (name, version) VALUES ('Albert', 0)\""
		String sql = "INSERT INTO Albert.dbo.Person (name, version) VALUES ('Albert', 0)"
		
		when: 'parsed and Insert object retrieved'
		parser.parse(sql)
		Insert insert = parser.sqlStatement
		
		then: 'first node is the Table with name "Albert.dbo.Person"'
		Node table = insert.nodes[0]
		table instanceof Table
		table.name == 'Albert.dbo.Person'
		
		and: 'second node is the ColumnList (name, version)'
		Node columnList = insert.nodes[1]
		columnList instanceof ColumnList
		
		and: 'third node is the ColumnValues (name, version)'
		Node columnValues = insert.nodes[2]
		columnValues instanceof ColumnValues
	}
	
	def 'Parse INSERT statement with SELECT clause and verify nodes'() {
		given: "SQL string \"INSERT INTO phone_book2 ([name], [phoneNumber]) SELECT [name], [phoneNumber] FROM phone_book\""
		String sql = 
"""INSERT INTO phone_book2 ([name], [phoneNumber])
SELECT [name], [phoneNumber]
FROM phone_book"""
		
		when: 'parsed and Insert object retrieved'
		parser.parse(sql)
		Insert insert = parser.sqlStatement
		
		then: 'third node is the Select object'
		Node select = insert.nodes[2]
		select instanceof Select
	}
	
	def ''() {
		given: ''
		String sql =
""""""
		
		
		when: ''
		
		
		then: ''
		
	}
}