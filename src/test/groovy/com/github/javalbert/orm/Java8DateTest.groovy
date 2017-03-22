package com.github.javalbert.orm

import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime

import com.github.javalbert.domain.Java8DateHolder
import com.github.javalbert.h2.H2
import com.github.javalbert.utils.jdbc.JdbcUtils

import spock.lang.Specification

class Java8DateTest extends Specification {
	private JdbcMapper mapper
	
	def setup() {
		mapper = new JdbcMapper()
		mapper.register(new ClassRowRegistration(Java8DateHolder.class)
			.table('Java8DateHolder')
			.columnInProperty('id', 'id', null, ClassRowRegistration.FLAG_ID | ClassRowRegistration.FLAG_GENERATED_VALUE)
			.columnInProperty('localDate', 'local_date', null, 0)
			.columnInProperty('localDateTime', 'local_date_time', null, 0))
		H2.createTables()
		H2.deleteRecords()
	}
	
	def 'Save and get an entity with Java 8 dates'() {
		given: 'instance of Java8DateHolder'
		Java8DateHolder holder = new Java8DateHolder()
		holder.localDate = LocalDate.of(2017, 3, 21)
		holder.localDateTime = LocalDateTime.of(2017, 3, 17, 11, 50)
		
		when: 'saved'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, holder)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'then retrieved back'
		Java8DateHolder holderFromDb = null
		try {
			conn = H2.getConnection()
			holderFromDb = mapper.get(conn, Java8DateHolder.class, 1L)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'dates and times are correct'
		holderFromDb.localDate == LocalDate.of(2017, 3, 21)
		holderFromDb.localDateTime == LocalDateTime.of(2017, 3, 17, 11, 50)
	}
}
