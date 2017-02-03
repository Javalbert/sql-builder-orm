# SqlbuilderORM

## Summary
- SQL builder API conforming to ANSI SQL
- SQL parser which can parse ANSI SQL strings and internally uses the SQL builder API
- Output SQL strings based on database vendor (ANSI, SQL Server, MySQL)
- No proxy objects
- Object graphs decided at runtime instead of compile time annotations

## TODO
- Add JMH benchmarks (ReflectASM vs reflection, Hibernate)
- Adding JUnit tests using Spock
- Conform to ANSI SQL
- Implement MERGE statement
- Implement nested JOINs
- Implement new Java 8 features (Date and Time API, lamda functions)
- Publish project to The Central Repository
- Replace usage of reflection API with ReflectASM library