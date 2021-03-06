package com.github.javalbert.sqlbuilder.parser

import com.github.javalbert.sqlbuilder.AggregateFunction
import com.github.javalbert.sqlbuilder.ArithmeticOperator
import com.github.javalbert.sqlbuilder.BinaryOperator
import com.github.javalbert.sqlbuilder.Case
import com.github.javalbert.sqlbuilder.Column
import com.github.javalbert.sqlbuilder.ColumnList
import com.github.javalbert.sqlbuilder.ColumnValues
import com.github.javalbert.sqlbuilder.CommonTableExpression
import com.github.javalbert.sqlbuilder.Condition
import com.github.javalbert.sqlbuilder.Delete
import com.github.javalbert.sqlbuilder.Expression
import com.github.javalbert.sqlbuilder.Fetch
import com.github.javalbert.sqlbuilder.From
import com.github.javalbert.sqlbuilder.GroupBy
import com.github.javalbert.sqlbuilder.InValues
import com.github.javalbert.sqlbuilder.Insert
import com.github.javalbert.sqlbuilder.Join
import com.github.javalbert.sqlbuilder.LiteralNumber
import com.github.javalbert.sqlbuilder.LiteralString
import com.github.javalbert.sqlbuilder.LogicalOperator
import com.github.javalbert.sqlbuilder.Merge
import com.github.javalbert.sqlbuilder.Node
import com.github.javalbert.sqlbuilder.Offset
import com.github.javalbert.sqlbuilder.OrderBy
import com.github.javalbert.sqlbuilder.Param
import com.github.javalbert.sqlbuilder.Predicate
import com.github.javalbert.sqlbuilder.PredicateOperator
import com.github.javalbert.sqlbuilder.Prefix
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.SetValue
import com.github.javalbert.sqlbuilder.SetValues
import com.github.javalbert.sqlbuilder.SortType
import com.github.javalbert.sqlbuilder.SqlStatement
import com.github.javalbert.sqlbuilder.Table
import com.github.javalbert.sqlbuilder.Token
import com.github.javalbert.sqlbuilder.Update
import com.github.javalbert.sqlbuilder.Where
import com.github.javalbert.sqlbuilder.With
import com.github.javalbert.sqlbuilder.parser.SqlParser.ExpressionCaseHelper
import com.github.javalbert.sqlbuilder.parser.SqlParser.FromHelper
import com.github.javalbert.sqlbuilder.parser.SqlParser.MergeHelper
import com.github.javalbert.sqlbuilder.parser.SqlParser.SelectTreeHelper
import com.github.javalbert.sqlbuilder.parser.SqlParser.WithHelper

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
		List<ParseToken> insertNodes = getParseTokens(sql)
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
		List<ParseToken> insertNodes = getParseTokens(sql)
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
		List<ParseToken> selectNodes = getParseTokens(sql)
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
		List<ParseToken> selectNodes = getParseTokens(sql)
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
		List<ParseToken> selectNodes = getParseTokens(sql)
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
	
	// Appears that in ANSI SQL, OFFSET FETCH does not require ORDER BY to precede it
	// http://stackoverflow.com/a/24046664
	// http://dba.stackexchange.com/a/30455
//	def 'Parse ORDER BY with OFFSET FETCH'() {
//		given: "SQL string \"SELECT * FROM tbl ORDER BY col1 OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY\""
//		String sql = "SELECT * FROM tbl ORDER BY col1 OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY"
//		
//		and: 'parse token of ORDER BY clause and OrderBy object'
//		List<ParseToken> selectNodes = getParseTokens(sql)
//		ParseToken orderByNode = selectNodes[2]
//		OrderBy orderBy = new OrderBy()
//		int i = 0
//		StringBuilder columnBuilder = new StringBuilder()
//		
//		when: 'parsing "OFFSET"'
//		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // col1
//		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1 // OFFSET
//		Node offsetNode = orderBy.nodes[1]
//		
//		and: 'then parsing "FETCH FIRST 20 ROWS ONLY"'
//		i = SqlParser.parseOrderByNode(orderBy, orderByNode.nodes, i, columnBuilder) + 1
//		Node fetchNode = orderBy.nodes[2]
//		
//		then: 'Offset object with skip count of 60 is added'
//		offsetNode instanceof Offset
//		offsetNode.skipCount == 60
//		
//		and: 'then Fetch object with fetch count of 20 is added'
//		fetchNode instanceof Fetch
//		fetchNode.fetchCount == 20
//	}
	
	def 'Parse SELECT with OFFSET FETCH'() {
		given: "SQL string \"SELECT * FROM tbl OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY\""
		String sql = "SELECT * FROM tbl OFFSET 60 ROWS FETCH FIRST 20 ROWS ONLY"
		List<ParseToken> selectNodes = getParseTokens(sql)
		Select select = new Select()
		SelectTreeHelper helper = new SelectTreeHelper(select, selectNodes, 0, selectNodes.size())
		
		when: 'parsing "SELECT * FROM tbl"'
		helper.parseCurrentToken() // SelectList
		helper.incrementTokenIndex()
		helper.parseCurrentToken() // From
		helper.incrementTokenIndex()
		Node selectList = select.nodes[0]
		Node from = select.nodes[1]
		
		and: 'then parsing "OFFSET 60 ROWS"'
		helper.parseCurrentToken()
		helper.incrementTokenIndex()
		Node offset = select.nodes[2]
		
		and: 'then parsing "FETCH FIRST 20 ROWS ONLY"'
		helper.parseCurrentToken()
		helper.incrementTokenIndex()
		Node fetch = select.nodes[3]
		
		then: 'SelectList and FROM clause was parsed'
		selectList instanceof SelectList
		from instanceof From
		
		and: 'then OFFSET clause with skip count of 60 was parsed'
		offset instanceof Offset
		offset.skipCount == 60
		
		and: 'then FETCH clause with fetch count of 20 was parsed'
		fetch instanceof Fetch
		fetch.fetchCount == 20
	}
	
	def 'Parse GROUP BY and verify nodes'() {
		given: "SQL string \"SELECT * FROM tbl GROUP BY col1, col2\""
		String sql = "SELECT * FROM tbl GROUP BY col1, col2"
		
		and: 'GROUP BY parse token and GroupBy object'
		List<ParseToken> selectNodes = getParseTokens(sql)
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
		List<ParseToken> selectNodes = getParseTokens(sql)
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
	
	def 'Parse expressions with string concatenation and arithmetic operations in SELECT statement'() {
		given: "SQL string \"SELECT num1str || ' + ' || num2str AS str_concat, num1 + num2 AS addition, (num1 + num2) * num3 - 1 AS more_complicated\""
		String sql = "SELECT num1str || ' + ' || num2str AS str_concat, num1 + num2 AS addition, (num1 + num2) * num3 - 1 AS more_complicated"
		
		when: 'parsed'
		parser.parse(sql)
		SelectList selectList = parser.sqlStatement.nodes[0]
		
		then: 'first column is an Expression object with alias "str_concat"'
		Node strConcatExpr = selectList.nodes[0]
		strConcatExpr instanceof Expression
		strConcatExpr.alias == 'str_concat'
		
		and: 'first column consists of nodes: num1str, string concat binary operator, literal string " + ", string concat binary operator, and num2str'
		Token num1StrToken = strConcatExpr.nodes[0]
		num1StrToken.token == 'num1str'
		BinaryOperator concat = strConcatExpr.nodes[1]
		concat == BinaryOperator.CONCAT
		LiteralString literal = strConcatExpr.nodes[2]
		literal.value == ' + '
		Token num2StrToken = strConcatExpr.nodes[4]
		num2StrToken.token == 'num2str'
		
		and: 'second expression has arithmetic operator add'
		Expression additionExpr = selectList.nodes[1]
		Node arithmeticOperatorAddNode = additionExpr.nodes[1]
		arithmeticOperatorAddNode == ArithmeticOperator.PLUS
		
		and: 'thrid expression consists: nested expression with addition operator node, multiplication operator, num3, subtract operator, literal number 1'
		Expression moreComplicatedExpr = selectList.nodes[2]
		Node nestedAddition = moreComplicatedExpr.nodes[0]
		nestedAddition.nodes[1] == ArithmeticOperator.PLUS
		moreComplicatedExpr.nodes[1] == ArithmeticOperator.MULTIPLY
		moreComplicatedExpr.nodes[2].token == 'num3'
		moreComplicatedExpr.nodes[3] == ArithmeticOperator.MINUS
		Node literalNumberOne = moreComplicatedExpr.nodes[4]
		literalNumberOne instanceof LiteralNumber
		literalNumberOne.value == 1
	}
	
	def 'Parse functions in SELECT statement'() {
		given: "SQL string \"SELECT TRIM(col1) || ' Summary', MIN(col2), MAX(col2), AVG(col2), SUM(col2), COUNT(col2) GROUP BY col1, col2\""
		String sql = "SELECT TRIM(col1) || ' Summary', MIN(col2), MAX(col2), AVG(col2), SUM(col2), COUNT(col2) GROUP BY col1, col2"
		
		when: 'parsed'
		parser.parse(sql)
		SelectList selectList = parser.sqlStatement.nodes[0]
		
		then: 'second column is AggregateFunction object representing MIN function on "col2" column'
		Node minNode = selectList.nodes[1]
		minNode instanceof AggregateFunction
		minNode.name == 'MIN'
		minNode.nodes[0].token == 'col2'
		
		and: 'then third column is MAX function'
		AggregateFunction maxNode = selectList.nodes[2]
		maxNode.name == 'MAX'
		
		and: 'then the rest are AVG, SUM, and COUNT'
		selectList.nodes[3].name == 'AVG'
		selectList.nodes[4].name == 'SUM'
		selectList.nodes[5].name == 'COUNT'
	}
	
	def 'Parse WHERE clause of SELECT statement and verify Condition object nodes'() {
		given: "SQL string \"SELECT * FROM tbl WHERE num1 >= 2 AND some_date > :someDate AND (col1 IS NULL OR col1 LIKE 'abc%')\""
		String sql = "SELECT * FROM tbl WHERE num1 >= 2 AND some_date > :someDate AND (col1 IS NULL OR col1 LIKE 'abc%')"
		
		and: 'Condition object and WHERE parse token'
		List<ParseToken> selectNodes = getParseTokens(sql)
		ParseToken whereNode = selectNodes[2]
		Condition condition = new Condition()
		int i = 0
		int nodeListSize = whereNode.nodes.size()
		
		when: 'parsing "num1 >= 2"'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		and: 'then parsing "AND" logical operator'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		and: 'then parsing "some_date > :someDate"'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		and: "then parsing \"AND (col1 IS NULL OR col1 LIKE 'abc%')\""
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1 // AND
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		then: 'Predicate object representing num1 >= 2 is added into Condition object'
		Node num1GteqTwo = condition.nodes[0]
		num1GteqTwo instanceof Predicate
		num1GteqTwo.nodes[0].token == 'num1'
		num1GteqTwo.nodes[1] == PredicateOperator.GT_EQ
		num1GteqTwo.nodes[2].value == 2
		
		and: 'then logical operator AND constant is added'
		Node logicalAndNode = condition.nodes[1]
		logicalAndNode == LogicalOperator.AND
		
		and: 'then Param object representing parameter :someDate is added'
		Predicate someDateGtParam = condition.nodes[2]
		Node someDateParam = someDateGtParam.nodes[2]
		someDateParam instanceof Param
		someDateParam.name == 'someDate'
		
		and: "then nested Condition (col1 IS NULL OR col1 LIKE 'abc%') is added"
		Node nestedCondition = condition.nodes[4]
		nestedCondition instanceof Condition
	}
	
	def 'Parse WHERE clause with IN keyword'() {
		given: "SQL string \"SELECT * FROM tbl WHERE col1 IN (1, 2) AND col2 IN (SELECT * FROM tbl2) AND col3 IN (:someCollection)\""
		String sql = "SELECT * FROM tbl WHERE col1 IN (1, 2) AND col2 IN (SELECT * FROM tbl2) AND col3 IN (:someCollection)"
		
		and: 'Condition object and WHERE parse token'
		List<ParseToken> selectNodes = getParseTokens(sql)
		ParseToken whereNode = selectNodes[2]
		Condition condition = new Condition()
		int i = 0
		int nodeListSize = whereNode.nodes.size()
		
		when: 'parsing "col1 IN (1, 2)"'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		and: 'then parsing "AND col2 IN (SELECT * FROM tbl2)"'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1 // AND
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		and: 'then parsing "AND col3 IN (:someCollection)"'
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1 // AND
		i = SqlParser.parseConditionNode(condition, whereNode.nodes, i, nodeListSize) + 1
		
		then: 'Predicate object for "col1 IN (1, 2)" uses predicate operator IN and contains InValues object with values 1 and 2'
		Predicate inOneOrTwo = condition.nodes[0]
		inOneOrTwo.nodes[1] == PredicateOperator.IN
		Node valuesOneOrTwo = inOneOrTwo.nodes[2]
		valuesOneOrTwo instanceof InValues
		valuesOneOrTwo.nodes[0].value == 1
		valuesOneOrTwo.nodes[1].value == 2
		
		and: 'then IN Predicate contains Select object'
		Predicate inSelect = condition.nodes[2]
		inSelect.nodes[2] instanceof Select
		
		and: 'then IN Predicate contains Param :someCollection'
		Predicate inSomeCollection = condition.nodes[4]
		inSomeCollection.nodes[2] instanceof Param
		inSomeCollection.nodes[2].name == 'someCollection'
	}
	
	def 'Parse CASE expressions in SELECT statement and verify nodes'() {
		given: "SQL string \"SELECT CASE num1 WHEN 1 THEN 'One' ELSE 'Zero' END AS 'Number Word' FROM tbl\""
		String sql = "SELECT CASE num1 WHEN 1 THEN 'One' ELSE 'Zero' END AS 'Number Word' FROM tbl"
		
		and: 'Case object, SelectList object, and CASE parse token'
		List<ParseToken> selectNodes = getParseTokens(sql)
		ParseToken caseNode = selectNodes[0].nodes[0] // SELECT > CASE
		Case sqlCase = new Case()
		SelectList selectList = new SelectList()
		selectList.sqlCase(sqlCase)
		int i = 0
		ExpressionCaseHelper helper = new ExpressionCaseHelper(sqlCase);
		
		when: 'parsing "num1"'
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1
		String pendingInputExpr = helper.pendingString.toString()
		
		and: 'then parsing WHEN keyword'
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1
		
		and: 'then parsing 1'
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1
		
		and: "then parsing \"THEN 'One'\""
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1 // THEN
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1 // 'One'
		
		and: "then parsing \"ELSE 'Zero' END\""
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1 // ELSE
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1 // 'Zero'
		i = SqlParser.parseCaseNode(sqlCase, caseNode.nodes, i, helper) + 1 // END
		boolean endOfCase = i >= caseNode.nodes.size()
		
		and: 'then apply alias to Case via SelectList object'
		selectList.as('Number Word')
		
		then: 'pending input expression "num1"'
		pendingInputExpr == 'num1'
		
		and: 'then num1 is added and WHEN constant token is added into Case object'
		sqlCase.nodes[0].token == 'num1'
		sqlCase.nodes[1] == Case.WHEN
		
		and: 'then 1 number literal is added'
		sqlCase.nodes[2].value == 1
		
		and: 'then THEN constant token and string literal "One" added'
		sqlCase.nodes[3] == Case.THEN
		sqlCase.nodes[4].value == 'One'
		
		and: 'then ELSE and "Zero" added, and reached end of CASE expression'
		sqlCase.nodes[5] == Case.ELSE
		sqlCase.nodes[6].value == 'Zero'
		endOfCase
		
		and: "then Case object's alias is \"Number Word\""
		sqlCase.alias == 'Number Word'
	}
	
	def 'Parse MERGE INSERT statement USING a table as table reference and verify nodes'() {
		given: 'SQL string "MERGE INTO Albert.dbo.DeceasedPerson dcd USING Albert.dbo.Person AS prs ON dcd.person_key = prs.person_key WHEN NOT MATCHED THEN INSERT (name_of_deceased) VALUES (prs.first_name || prs.last_name)"'
		String sql = 'MERGE INTO Albert.dbo.DeceasedPerson dcd USING Albert.dbo.Person AS prs ON dcd.person_key = prs.person_key WHEN NOT MATCHED THEN INSERT (name_of_deceased) VALUES (prs.first_name || prs.last_name)'
		
		and: 'Merge object'
		List<ParseToken> mergeNodes = getParseTokens(sql)
		Merge merge = new Merge()
		int i = 0
		MergeHelper helper = new MergeHelper(merge)
		
		when: 'parsing "MERGE INTO Albert.dbo.DeceasedPerson dcd"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // MERGE
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // INTO
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // Albert
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // .
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // dbo
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // .
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // DeceasedPerson
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // dcd
		Node targetTableNode = merge.nodes[0]
		
		and: 'then parsing "USING Albert.dbo.Person AS prs"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // USING
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // Albert
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // .
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // dbo
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // .
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // Person
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // AS
		Node tableReferenceNode = merge.nodes[1]
		
		and: 'then parsing "ON dcd.person_key = prs.person_key"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // ON
		Node searchCondition = merge.nodes[2]
		
		and: 'then parsing "WHEN NOT MATCHED THEN"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // WHEN NOT MATCHED
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // THEN
		Node whenMatchedNode = merge.nodes[3]
		Node thenNode = merge.nodes[4]
		// ^^^ Reason why there are two nodes here instead of a single node
		// is because of the ability to specify a search condition for the WHEN clause
		// and is between MATCHED and THEN e.g. WHEN MATCHED AND <search condition> THEN
		
		and: 'then parsing "INSERT (name_of_deceased) VALUES (prs.first_name || prs.last_name)"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // INSERT (name_of_deceased) VALUES (prs.first_name || prs.last_name)
		Node insertNode = merge.nodes[5]
		
		then: 'target table "Albert.dbo.DeceasedPerson" with alias "dcd" was added'
		targetTableNode instanceof Table
		targetTableNode.name == 'Albert.dbo.DeceasedPerson'
		targetTableNode.alias == 'dcd'
		
		and: 'then table reference "Albert.dbo.Person" with alias "prs" was added'
		tableReferenceNode instanceof Table
		tableReferenceNode.name == 'Albert.dbo.Person'
		tableReferenceNode.alias == 'prs'
		
		and: 'then search condition of joining DeceasedPersons table with Persons table by person_key column was added'
		searchCondition instanceof Condition
		
		and: 'then WHEN NOT MATCHED THEN was added'
		whenMatchedNode == Merge.WHEN_NOT_MATCHED
		thenNode == Merge.THEN
		
		and: 'then INSERT statement was added'
		insertNode instanceof Insert
	}
	
	def 'Parse MERGE with a SELECT statement as the table reference and with a secondary search condition and verify nodes'() {
		given: "SQL string \"MERGE INTO Person2 AS p2 USING (SELECT person_key, last_name FROM Person) p ON p2.person_key = p.person_key WHEN MATCHED AND p.last_name = 'Chan' THEN DELETE\""
		String sql = "MERGE INTO Person2 AS p2 USING (SELECT person_key, last_name FROM Person) p ON p2.person_key = p.person_key WHEN MATCHED AND p.last_name = 'Chan' THEN DELETE"
		
		and: 'Merge object'
		List<ParseToken> mergeNodes = getParseTokens(sql)
		Merge merge = new Merge()
		int i = 0
		MergeHelper helper = new MergeHelper(merge)
		
		when: 'parsing "... USING (SELECT person_key, last_name FROM Albert.dbo.Person) p ON p2.person_key = p.person_key"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // MERGE
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // INTO
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // Person2
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // AS p2
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // USING
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // (SELECT person_key, last_name FROM Albert.dbo.Person)
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // p
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // ON p2.person_key = p.person_key
		Node selectNode = merge.nodes[1]
		
		and: "then parsing \"WHEN MATCHED AND p.last_name = 'Chan'\""
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // WHEN MATCHED
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // AND
		Node whenClauseSearchCondition = merge.nodes[4]
		
		and: 'then parsing "THEN DELETE"'
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // THEN
		i = SqlParser.parseMergeNode(merge, mergeNodes, i, helper) + 1 // DELETE
		Node deleteNode = merge.nodes[6]
		
		then: 'SELECT statement as the table reference with alias "p" was added'
		selectNode instanceof Select
		selectNode.alias == 'p'
		
		and: 'then WHEN clause search condition was added'
		whenClauseSearchCondition instanceof Condition
		
		and: 'then DELETE constant token was added'
		deleteNode == Merge.DELETE
	}
	
	def 'Parse FROM clause with nested JOIN syntax'() {
		given: 'SQL string "SELECT trip.id, drvr.name FROM Trips trip LEFT OUTER JOIN Driver drvr INNER JOIN Device dev ON drvr.device_id = dev.device_id ON trip.driver_id = drvr.driver_id"'
		String sql = 'SELECT trip.id, drvr.name FROM Trips trip LEFT OUTER JOIN Driver drvr INNER JOIN Device dev ON drvr.device_id = dev.device_id ON trip.driver_id = drvr.driver_id'
		
		and: 'FROM object'
		List<ParseToken> selectNodes = getParseTokens(sql)
		ParseToken fromNode = selectNodes[1]
		From from = new From()
		int i = 0
		FromHelper helper = new FromHelper(from)
		
		when: 'parsing "Trips trip LEFT OUTER JOIN Driver drvr"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // Trips
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // trip
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // LEFT OUTER JOIN
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // Driver
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // drvr
		Node tripsTable = from.nodes[0]
		Node leftOuterJoin = from.nodes[1]
		Node driverTable = from.nodes[2]
		
		and: 'then parsing nested join "INNER JOIN Device dev ON drvr.device_id = dev.device_id"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // INNER JOIN
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // Device
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // dev
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // ON drvr.device_id = dev.device_id
		Node nestedJoinClause = from.nodes[3]
		Node deviceTable = from.nodes[4]
		Node nestedJoinCondition = from.nodes[5]
		
		and: 'then parsing "ON trip.driver_id = drvr.driver_id"'
		i = SqlParser.parseFromNode(from, fromNode.nodes, i, helper) + 1 // ON trip.driver_id = drvr.driver_id
		Node outerJoinCondition = from.nodes[5]
		
		then: 'Trips table, LEFT OUTER JOIN, and Driver table is added'
		tripsTable instanceof Table
		leftOuterJoin == Join.LEFT_OUTER_JOIN
		driverTable instanceof Table
		
		and: 'then nested INNER JOIN between Driver and Device tables is added'
		nestedJoinClause == Join.INNER_JOIN
		deviceTable instanceof Table
		nestedJoinCondition instanceof Condition
		
		and: 'then outer join condition between Trips and Driver tables is added'
		outerJoinCondition instanceof Condition
	}
	
	def 'Parse FROM clause with nested JOINs surrounded by parentheses'() {
		// SQL retrieved in last example from http://sqlity.net/en/1435/a-join-a-day-nested-joins/
		given: 'SQL string "FROM Product prod JOIN (OrderHeader oh JOIN OrderDetail od ON oh.OrderID = od.OrderID) ON od.ProdID = prod.ProdID JOIN (Customer cus JOIN Person pers ON cus.PersonID = pers.BusinessEntityID) ON oh.CustID = cus.CustID"'
		String sql = 'FROM Product prod JOIN (OrderHeader oh JOIN OrderDetail od ON oh.OrderID = od.OrderID) ON od.ProdID = prod.ProdID JOIN (Customer cus JOIN Person pers ON cus.PersonID = pers.BusinessEntityID) ON oh.CustID = cus.CustID'
		
		and: 'FROM object'
		List<ParseToken> nodes = getParseTokens(sql)[0].nodes
		From from = new From()
		int i = 0
		FromHelper helper = new FromHelper(from)
		
		when: 'parsing Product and the first nested JOIN group tables: "FROM Product prod JOIN (OrderHeader oh JOIN OrderDetail od ON oh.OrderID = od.OrderID)"'
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // Product
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // prod
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // JOIN
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // (OrderHeader oh JOIN OrderDetail od ON oh.OrderID = od.OrderID)
		Node leftParenthesisNode = from.nodes[2]
		Node orderHeader = from.nodes[3]
		Node nestedJoinClause = from.nodes[4]
		Node orderDetail = from.nodes[5]
		Node nestedJoinCondition = from.nodes[6]
		Node rightParenthesisNode = from.nodes[7]
		
		and: 'then parsing join condition "ON od.ProdID = prod.ProdID JOIN" between Product and the first nested JOIN group tables'
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // ON od.ProdID = prod.ProdID
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // JOIN
		Node outerJoinCondition = from.nodes[8]
		Node joinBetweenNestedJoinGroups = from.nodes[9]
		
		and: 'then parsing the second nested JOIN group "(Customer cus JOIN Person pers ON cus.PersonID = pers.BusinessEntityID)"'
		i = SqlParser.parseFromNode(from, nodes, i, helper) + 1 // (Customer cus JOIN Person pers ON cus.PersonID = pers.BusinessEntityID)
		Node secondLeftParenthesis = from.nodes[10]
		Node secondRightParenthesis = from.nodes[15]
		
		then: 'the first nested JOIN group contains the Order tables, their join condition, and surrounded by parentheses'
		leftParenthesisNode == From.LEFT_PARENTHESIS
		orderHeader instanceof Table
		nestedJoinClause == Join.INNER_JOIN
		orderDetail instanceof Table
		nestedJoinCondition instanceof Condition
		rightParenthesisNode == From.RIGHT_PARENTHESIS
		
		and: 'then first nested JOIN group is joined with Product, and together will join with the result of the second nested JOIN group'
		outerJoinCondition instanceof Condition
		joinBetweenNestedJoinGroups == Join.INNER_JOIN
		
		and: 'then second nested JOIN group is surrounded by a set of parentheses'
		secondLeftParenthesis == From.LEFT_PARENTHESIS
		secondRightParenthesis == From.RIGHT_PARENTHESIS
	}
	
	private List<ParseToken> getParseTokens(String sql) {
		return new ParseTree(parser.tokenize(sql)).parseTokens().nodes;
	}
}