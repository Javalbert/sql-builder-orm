package com.github.javalbert.orm

import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime

import com.github.javalbert.domain.Java8DateHolder
import com.github.javalbert.h2.H2
import com.github.javalbert.sqlbuilder.Predicate
import com.github.javalbert.sqlbuilder.Select
import com.github.javalbert.sqlbuilder.SelectList
import com.github.javalbert.sqlbuilder.Where
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
	
	def 'Execute SELECT statement with LocalDate parameter list'() {
		given: 'two Java8DateHolder records'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Java8DateHolder(LocalDate.of(2017, 2, 4), null))
			mapper.save(conn, new Java8DateHolder(LocalDate.of(2017, 3, 11), null))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Select object with "dates" parameter'
		Select select = mapper.selectFrom(Java8DateHolder.class)
		.where(new Where().predicate(new Predicate().column('local_date').in().param('dates')
		)
		)
		
		when: 'SELECT statement is executed'
		List<Java8DateHolder> dateHolders = null
		try {
			conn = H2.getConnection()
			dateHolders = mapper.createQuery(select)
					.setLocalDates('dates', [ LocalDate.of(2017, 2, 4), LocalDate.of(2017, 3, 11) ])
					.toList(conn, Java8DateHolder.class)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the two objects were retrieved'
		dateHolders.size() == 2
	}
	
	def 'Execute SELECT statement with LocalDateTime parameter list'() {
		given: 'two Java8DateHolder records'
		Connection conn = null
		try {
			conn = H2.getConnection()
			mapper.save(conn, new Java8DateHolder(null, LocalDateTime.of(2017, 3, 22, 8, 37)))
			mapper.save(conn, new Java8DateHolder(null, LocalDateTime.of(2017, 3, 22, 17, 29)))
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		and: 'Select object with "dateTimes" parameter'
		Select select = mapper.selectFrom(Java8DateHolder.class)
		.where(new Where().predicate(new Predicate().column('local_date_time').in().param('dateTimes')
		)
		)
		
		when: 'SELECT statement is executed'
		List<Java8DateHolder> dateHolders = null
		try {
			conn = H2.getConnection()
			dateHolders = mapper.createQuery(select)
					.setLocalDateTimes('dateTimes', [ LocalDateTime.of(2017, 3, 22, 8, 37), LocalDateTime.of(2017, 3, 22, 17, 29) ])
					.toList(conn, Java8DateHolder.class)
		} finally {
			JdbcUtils.closeQuietly(conn)
		}
		
		then: 'the two objects were retrieved'
		dateHolders.size() == 2
	}
}
