# SqlbuilderORM Benchmark

Test environment: Intel Core i7-4790k, Windows 10 x64 + High Performance Power Plan, Java version "1.8.0_121" Java Hotspot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

[Benchmark](src/main/java/com/github/javalbert/SqlbuilderOrmBenchmark.java) which tests getting a single object by ID:

| Benchmark | Mode | Samples | Score | Score error | Units |
|---|---|---:|---:|---:|---|
| testRetrievalHibernateStatelessSession | avgt | 15 | 4521.153 | 42.683 | ns/op |
| testRetrievalJdbc | avgt | 15 | 616.361 | 5.621 | ns/op |
| testRetrievalSql2o | avgt | 15 | 81948.040 | 66513.775 | ns/op |
| testRetrievalSqlbuilderOrm | avgt | 15 | 4360.992 | 64.079 | ns/op |

[Non-JMH benchmark](src/main/java/com/github/javalbert/NonJMHBenchmark.java) version of the above, executed 1000 times in a for loop:

| Library | Duration |
|---|---:|
| Hibernate | 237ms |
| JDBC | 19ms |
| Sql2o | 74ms |
| SqlbORM | 55ms |
