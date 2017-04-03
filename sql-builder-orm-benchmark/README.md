# SqlbuilderORM Benchmark

Test environment: Intel Core i7-4790k, Windows 10 x64 + High Performance Power Plan, Java version "1.8.0_121" Java Hotspot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

[Benchmark](src/main/java/com/github/javalbert/SqlbuilderOrmBenchmark.java) which tests getting a single object by ID:

| Benchmark | Mode | Samples | Score | Score error | Units |
|---|---|---:|---:|---:|---|
| testRetrievalHibernateGetById | avgt | 15 | 4446.329 | 40.730 | ns/op |
| testRetrievalHibernateQueryById | avgt | 15 | 5437.500 | 93.925 | ns/op |
| testRetrievalJdbc | avgt | 15 | 614.788 | 6.625 | ns/op |
| testRetrievalJooq | avgt | 15 | 7775.380 | 40.337 | ns/op |
| testRetrievalSql2o | avgt | 15 | 75717.887 | 63003.542 | ns/op |
| testRetrievalSqlbOrmGetById | avgt | 15 | 2671.766 | 64.161 | ns/op |
| testRetrievalSqlbOrmQueryById | avgt | 15 | 20401.501 | 199.725 | ns/op |

[Non-JMH benchmark](src/main/java/com/github/javalbert/NonJMHBenchmark.java) version of the above, executed 1000 times in a for loop:

| Library | Duration (ms) |
|---|---:|
| Hibernate (get by ID) | 158.25 |
| Hibernate (query by ID) | 588.53 |
| JDBC | 18.90 |
| jOOQ | 116.37 |
| Sql2o | 71.36 |
| SqlbORM (get by ID) | 44.59 |
| SqlbORM (query by ID) | 81.45 |
