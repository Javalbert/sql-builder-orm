# SqlbuilderORM

## Summary
- SQL centric
	- No need to learn framework specific query language
	- SQL building
	- SQL parsing
	- ANSI SQL over proprietary SQL extensions
- No reliance on database schema (potentially inconsistent/buggy JDBC driver behavior)
- Data mapping row to class
	- Requires explicit @Column annotations (no need for transient keyword)
	- Classes don't need to represent a database table and they can use @Alias annotation for aliases in result sets
- No Connection object intialization/handling (your choice of application framework and/or connection pool handles Connections)
- SELECT query results can be returned as Lists, Maps, or Sets
	- JSON support (uses GSON library)
- Uses Reflection API
	- No proxy classes
- Object graphs decided at runtime (not compile time)
- No code generation

## TODO

- Testing
	- JUnit
- Javadoc