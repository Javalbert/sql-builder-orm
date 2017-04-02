# SqlbuilderORM Benchmark

Test environment: Intel Core i7-4790k, Windows 10 x64 + High Performance Power Plan, Java version "1.8.0_121" Java Hotspot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

[Benchmark](src/main/java/com/github/javalbert/SqlbuilderOrmBenchmark.java) which tests getting a single object by ID:

| Benchmark | Mode | Samples | Score | Score error | Units |
|---|---|---:|---:|---:|---|
| testRetrievalHibernateStatelessSession | avgt | 15 | 4455.904 | 39.205 | ns/op |
| testRetrievalJdbc | avgt | 15 | 614.322 | 7.473 | ns/op |
| testRetrievalJooq | avgt | 15 | 7674.354 | 39.303 | ns/op |
| testRetrievalSql2o | avgt | 15 | 70712.486 | 62101.671 | ns/op |
| testRetrievalSqlbuilderOrm | avgt | 15 | 4345.117 | 10.736 | ns/op |

[Non-JMH benchmark](src/main/java/com/github/javalbert/NonJMHBenchmark.java) version of the above, executed 1000 times in a for loop:

| Library | Duration |
|---|---:|
| Hibernate | 235ms |
| JDBC | 18ms |
| jOOQ | 115ms |
| Sql2o | 72ms |
| SqlbORM | 46ms |
