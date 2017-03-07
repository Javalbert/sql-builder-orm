# SqlbuilderORM

## Summary
- Influenced by Hibernate and jOOQ
- No configuration files required
- No lengthy strings or custom query language
- SQL builder API conforming to ANSI SQL
- SQL parser which can parse ANSI SQL strings and internally uses the SQL builder API
- Output SQL strings based on database vendor (ANSI, SQL Server, MySQL)
- Explicit @Column annotation, no implicit mapping to fields avoiding @Transient or transient keyword
- DataSource and Connection objects managed outside by the developer instead of the ORM
- No proxy objects (easy serialization)
- Keep foreign key field on child entities if a relationship is defined
- Object graphs decided at runtime instead of compile time annotations

## TODO
- Adding JUnit tests using Spock
- Conform to ANSI SQL
- Implement MERGE statement
- Implement nested JOINs
- Implement new Java 8 features (Date and Time API, Streams API, lamda functions)
- Publish project to The Central Repository
- Replace usage of reflection API with ReflectASM library
- Write Javadoc (thread-safety)