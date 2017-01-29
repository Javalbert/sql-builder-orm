package chan.shundat.albert.sqlbuilder.parser

import chan.shundat.albert.sqlbuilder.Column
import chan.shundat.albert.sqlbuilder.ColumnList
import chan.shundat.albert.sqlbuilder.ColumnValues
import chan.shundat.albert.sqlbuilder.CommonTableExpression
import chan.shundat.albert.sqlbuilder.Condition
import chan.shundat.albert.sqlbuilder.Delete
import chan.shundat.albert.sqlbuilder.Fetch
import chan.shundat.albert.sqlbuilder.From
import chan.shundat.albert.sqlbuilder.GroupBy
import chan.shundat.albert.sqlbuilder.Insert
import chan.shundat.albert.sqlbuilder.Join
import chan.shundat.albert.sqlbuilder.LiteralNumber
import chan.shundat.albert.sqlbuilder.LiteralString
import chan.shundat.albert.sqlbuilder.Node
import chan.shundat.albert.sqlbuilder.Offset
import chan.shundat.albert.sqlbuilder.OrderBy
import chan.shundat.albert.sqlbuilder.Prefix
import chan.shundat.albert.sqlbuilder.Select
import chan.shundat.albert.sqlbuilder.SelectList
import chan.shundat.albert.sqlbuilder.SetValue
import chan.shundat.albert.sqlbuilder.SetValues
import chan.shundat.albert.sqlbuilder.SortType
import chan.shundat.albert.sqlbuilder.SqlStatement
import chan.shundat.albert.sqlbuilder.Table
import chan.shundat.albert.sqlbuilder.Token
import chan.shundat.albert.sqlbuilder.Update
import chan.shundat.albert.sqlbuilder.Where
import chan.shundat.albert.sqlbuilder.With
import chan.shundat.albert.sqlbuilder.parser.SqlParser.ExpressionCaseHelper
import chan.shundat.albert.sqlbuilder.parser.SqlParser.FromHelper
import chan.shundat.albert.sqlbuilder.parser.SqlParser.WithHelper
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
		table instanceof Table
		table.name == 'Albert.dbo.Person'
		
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
		table instanceof Table
		table.name == 'Albert.dbo.Person'
		
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
	
	def 'Parse select list in SELECT statement and verify Column objects'() {
		given: "SQL string \"SELECT t.col1 AS Col1, t.col2 AS 'Second Column' FROM tbl t\""
		String sql = "SELECT t.col1 AS Col1, t.col2 AS 'Second Column' FROM tbl t"
		
		and: 'select list parse token and SelectList object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken selectListNode = selectNodes[0]
		SelectList selectList = new SelectList()
		int i = 0
		ExpressionCaseHelper helper = new ExpressionCaseHelper(selectList)
		helper.setAlwaysAppendColumn(true) // Important
		
		when: 'parsing "t.col1"'
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // t
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // .
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // col1
		String pendingCol1 = helper.pendingString.toString()
		
		and: 'then parsing AS'
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1
		Node col1 = selectList.nodes[0]
		
		and: "then parsing \"t.col2 AS 'Second Column'\""
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // ,
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // t
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // .
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1 // col2
		// Move forward to parsing the alias string literal from t.col2
		i = SqlParser.parseSelectListNode(selectList, selectListNode.nodes, i, helper) + 1
		Node col2 = selectList.nodes[1]
		
		then: 'set pending column "t.col1"'
		pendingCol1 == 't.col1'
		
		and: 'then "col1" Column object with table alias "t" and column alias "Col1" is added into SelectList'
		col1 instanceof Column
		col1.prefix == Prefix.TABLE_ALIAS
		col1.prefixValue == 't'
		col1.name == 'col1'
		col1.alias == 'Col1'
		
		and: 'then col2 has a column alias of "Second Column"'
		col2.name == 'col2'
		col2.alias == 'Second Column'
	}
	
	def 'Parse FROM clause and verify nodes'() {
		given: 'SQL string "SELECT * FROM Pets AS p INNER JOIN PetTypes t ON p.PetTypeID = t.PetTypeID"'
		String sql = "SELECT * FROM Pets AS p INNER JOIN PetTypes t ON p.PetTypeID = t.PetTypeID"
		
		and: 'FROM object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken fromNode = selectNodes[1]
		From from = new From()
		int i = 0
		FromHelper helper = new FromHelper(from)
		
		expect: 'parse token represents FROM clause'
		fromNode.token == 'FROM'
		
		when: 'parsing "Pets" table'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1
		String pendingPets = helper.tableName.toString()
		
		and: 'then parsing "AS p"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1
		Node petsTable = from.nodes[0]
		
		and: 'then parsing "INNER JOIN"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1
		Node innerJoin = from.nodes[1]
		
		and: 'then parsing "PetTypes t"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // PetTypes
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // t
		Node petTypesTable = from.nodes[2]
		
		and: 'then parsing "ON"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1
		Node joinPredicate = from.nodes[3]
		
		then: 'table "Pets" is pending to be added'
		pendingPets == 'Pets'
		
		and: 'then first node Table object is added into From object with name "Pets" and alias "p"'
		petsTable instanceof Table
		petsTable.alias == 'p'
		petsTable.name == 'Pets'
		
		and: 'then seoncd node INNER JOIN constant token is added'
		innerJoin == Join.INNER_JOIN
		
		and: 'then third node Table "PetTypes" alias "t" is added'
		petTypesTable instanceof Table
		petTypesTable.alias == 't'
		petTypesTable.name == 'PetTypes'
		
		and: 'then forth node Condition object representing ON join predicate is added'
		joinPredicate instanceof Condition
	}
	
	def 'Parse FROM clause with inline view'() {
		given: 'SELECT * FROM People LEFT OUTER JOIN (SELECT p.* FROM Pets p INNER JOIN PetTypes pt ON p.PetTypeID = pt.PetTypeID) Pets'
		String sql = "SELECT * FROM People LEFT OUTER JOIN (SELECT p.* FROM Pets p INNER JOIN PetTypes pt ON p.PetTypeID = pt.PetTypeID) Pets"
		
		when: 'parsed'
		parser.parse(sql)
		Select select = parser.sqlStatement
		From from = select.nodes[1]
		Node inlineView = from.nodes[2]
		
		then: 'Inline view inside From object is a Select object with alias "Pets"'
		inlineView instanceof Select
		inlineView.alias == 'Pets'
	}
	
	def 'Parse ORDER BY and verify nodes'() {
		given: "SQL string \"SELECT d.android_version_number, COUNT(*) AS 'Device Count' FROM MobileDevice d GROUP BY d.android_version_number ORDER BY d.android_version_number, 'Device Count' DESC\""
		String sql = "SELECT d.android_version_number, COUNT(*) AS 'Device Count' FROM MobileDevice d GROUP BY d.android_version_number ORDER BY d.android_version_number, 'Device Count' DESC"
		
		and: 'parse token of ORDER BY clause and OrderBy object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken orderByNode = selectNodes[3]
		OrderBy orderBy = new OrderBy()
		int i = 0
		StringBuilder columnBuilder = new StringBuilder()
		
		when: 'parsing "d.android_version_number"'
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // d
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // .
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // android_version_number
		String pendingAndroidVersionNumber = columnBuilder.toString() // Must get column string here, on next iteration columnBuilder.setLength(0)
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // ,
		Node androidVersionNumberColumn = orderBy.nodes[0]
		
		and: "then parsing column alias \"'Device Count'\""
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1
		Node deviceCountColumn = orderBy.nodes[1]
		
		and: 'then parsing DESC keyword'
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1
		Node descToken = orderBy.nodes[2]
		
		then: 'pending column "d.android_version_number" is added as Column object with name "android_version_number" and table alias "d"'
		pendingAndroidVersionNumber == 'd.android_version_number'
		androidVersionNumberColumn instanceof Column
		androidVersionNumberColumn.prefixValue == 'd'
		androidVersionNumberColumn.name == 'android_version_number'
		
		and: 'then Column with column alias "Device Count" is added'
		deviceCountColumn instanceof Column
		deviceCountColumn.alias == 'Device Count'
		
		and: 'then constant token DESC is added'
		descToken == SortType.DESC
	}
	
	def 'Parse ORDER BY with OFFSET FETCH'() {
		given: "SQL string \"SELECT * FROM tbl ORDER BY col1 OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY\""
		String sql = "SELECT * FROM tbl ORDER BY col1 OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY"
		
		and: 'parse token of ORDER BY clause and OrderBy object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken orderByNode = selectNodes[2]
		OrderBy orderBy = new OrderBy()
		int i = 0
		StringBuilder columnBuilder = new StringBuilder()
		
		when: 'parsing "OFFSET"'
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // col1
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // OFFSET
		Node offsetNode = orderBy.nodes[1]
		
		and: 'then parsing "FETCH FIRST 20 ROWS ONLY"'
		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1
		Node fetchNode = orderBy.nodes[2]
		
		then: 'Offset object with skip count of 60 is added'
		offsetNode instanceof Offset
		offsetNode.skipCount == 60
		
		and: 'then Fetch object with fetch count of 20 is added'
		fetchNode instanceof Fetch
		fetchNode.fetchCount == 20
	}
	
	def 'Parse GROUP BY and verify nodes'() {
		given: "SQL string \"SELECT * FROM tbl GROUP BY col1, col2\""
		String sql = "SELECT * FROM tbl GROUP BY col1, col2"
		
		and: 'GROUP BY parse token and GroupBy object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		ParseToken groupByNode = selectNodes[2]
		GroupBy groupBy = new GroupBy()
		int i = 0
		StringBuilder columnBuilder = new StringBuilder()
		
		when: 'parsing "col1"'
		SqlParser.parseGroupByNode(groupBy, groupByNode.nodes[i++], columnBuilder)
		String pendingCol1 = columnBuilder.toString()
		
		and: 'then parsing ","'
		SqlParser.parseGroupByNode(groupBy, groupByNode.nodes[i++], columnBuilder)
		Node col1Node = groupBy.nodes[0]
		
		and: 'then parsing "col2"'
		SqlParser.parseGroupByNode(groupBy, groupByNode.nodes[i++], columnBuilder)
		SqlParser.parsePendingColumn(columnBuilder, groupBy); // Must call this to add last column
		Node col2Node = groupBy.nodes[1]
		
		then: 'column "col1" is pending to be added as a Column'
		pendingCol1 == 'col1'
		
		and: 'then "col1" Column is added'
		col1Node instanceof Column
		col1Node.name == 'col1'
		
		and: 'then "col2" Column is added'
		col2Node instanceof Column
		col2Node.name == 'col2'
	}
	
	def 'Parse WITH clause and verify nodes'() {
		given: "SQL string \"WITH tbl1 AS (SELECT 1 col1, 2 col2), tbl2 (first_name, last_name) AS (SELECT 'albert' FirstName, 'chan') SELECT * FROM tbl1, tbl2\""
		String sql = "WITH tbl1 AS (SELECT 1 col1, 2 col2), tbl2 (first_name, last_name) AS (SELECT 'albert' FirstName, 'chan') SELECT * FROM tbl1, tbl2"
		
		and: 'With object'
		List<ParseToken> selectNodes = new ParseTree(parser.tokenize(sql)).parseTokens().nodes
		With with = new With()
		WithHelper helper = new WithHelper(with, selectNodes, 0)
		
		when: 'parsing "WITH" and "tbl1"'
		helper.parseNextToken() // WITH
		helper.parseNextToken() // tbl1
		Node tbl1Cte = with.nodes[0]
		
		and: 'then parsing "AS (SELECT 1 col1, 2 col2)"'
		helper.parseNextToken()
		Select tbl1CteQuery = tbl1Cte.select
		
		and: 'then parsing ", tbl2 (first_name, last_name)"'
		helper.parseNextToken() // ,
		helper.parseNextToken() // tbl2
		helper.parseNextToken() // (
		Node tbl2Cte = with.nodes[1]
		List<String> tbl2Columns = tbl2Cte.columns
		
		and: 'then parsing the "SELECT" of the query (not CTE)'
		helper.parseNextToken() // AS
		boolean wasNotTheEnd = !helper.terminalClauseFound
		int indexBeforeSelect = helper.i - 1 // - 1 because the helper increments 1 more for parentheses group node
		helper.parseNextToken()
		boolean foundSelect = helper.terminalClauseFound
		int indexAfterSelect = helper.i
		
		then: 'CommonTableExpression object with name "tbl1" is added into With object'
		tbl1Cte instanceof CommonTableExpression
		tbl1Cte.name == 'tbl1'
		
		and: 'then tbl1 CTE has its SELECT statement set'
		tbl1CteQuery instanceof Select
		
		and: 'then tbl2 CTE specified columns "first_name" and "last_name"'
		tbl2Columns == [ 'first_name', 'last_name' ]
		
		and: 'then SELECT clause of the query is found, '
		wasNotTheEnd
		foundSelect
		
		and: "internal index was not incremented from \"(SELECT 'albert' FirstName, 'chan')\" to the \"SELECT\" because another for loop will use the value and increment it"
		indexAfterSelect == indexBeforeSelect
	}
}