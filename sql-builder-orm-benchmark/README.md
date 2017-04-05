# SqlbuilderORM Benchmark

Test environment: Intel Core i7-4790k, Windows 10 x64 + High Performance Power Plan, Java version "1.8.0_121" Java Hotspot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

[Benchmark](src/main/java/com/github/javalbert/EntityByIdBenchmark.java) which tests getting a single object by ID:

| Benchmark | Mode | Samples | Score | Score error | Units |
|---|---|---:|---:|---:|---|
| testRetrievalHibernateGetById | avgt | 15 | 4372.836 | 37.874 | ns/op |
| testRetrievalHibernateQueryById | avgt | 15 | 5368.254 | 64.305 | ns/op |
| testRetrievalJdbc | avgt | 15 | 610.772 | 3.052 | ns/op |
| testRetrievalJooq | avgt | 15 | 7682.461 | 62.970 | ns/op |
| testRetrievalSql2o | avgt | 15 | 92056.040 | 66644.627 | ns/op |
| testRetrievalSqlbOrmGetById | avgt | 15 | 2700.575 | 67.184 | ns/op |
| testRetrievalSqlbOrmQueryById | avgt | 15 | 19417.833 | 188.876 | ns/op |

[Non-JMH benchmark](src/main/java/com/github/javalbert/EntityByIdNonJMH.java) version of the above, executed 1000 times in a for loop:

| Library | Duration (ms) |
|---|---:|
| Hibernate (get by ID) | 169.03 |
| Hibernate (query by ID) | 607.77 |
| JDBC | 20.10 |
| jOOQ | 120.70 |
| Sql2o | 72.03 |
| SqlbORM (get by ID) | 43.84 |
| SqlbORM (query by ID) | 81.79 |
