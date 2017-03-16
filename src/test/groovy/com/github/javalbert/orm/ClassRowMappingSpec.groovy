package com.github.javalbert.orm

import com.github.javalbert.orm.ClassRowMapping

import com.github.javalbert.domain.BookPage
import com.github.javalbert.domain.BookPagePK
import com.github.javalbert.domain.Person
import spock.lang.Specification

class ClassRowMappingSpec extends Specification {
	def 'Verify fully qualified table name for a class mapping'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		expect: 'right catalog, schema, table name, and fully qualified name'
		mapping.catalog == 'Albert'
		mapping.schema == 'dbo'
		mapping.table == 'Person'
		mapping.tableIdentifier == 'Albert.dbo.Person'
	}
	
	def 'Get primary key of object'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		and: 'Person object with personKey set to 123'
		Person person = new Person()
		person.setPersonKey(123)
		
		when: 'getting primary key of Person object'
		Object id = mapping.getOrCreateId(person)
		
		then: 'primary key is 123'
		id == 123
	}
	
	def 'Get primary key of object via map key'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		and: 'Person object with personKey set to 123'
		Person person = new Person()
		person.setPersonKey(123)
		
		when: 'getting primary key of Person object via map key'
		Object id = mapping.getMapKeyValue(person)
		
		then: 'primary key is 123'
		id == 123
	}
	
	def 'Get map key value of object via map key'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		and: 'Person object with full name set to "Albert Chan"'
		Person person = new Person()
		person.setFullName('Albert Chan')
		
		when: 'getting full name of Person object via "Full Name" map key'
		String fullName = mapping.getMapKeyValue(person, "Full Name")
		
		then: 'returned full name is "Albert Chan"'
		fullName == 'Albert Chan'
	}
	
	def 'Get composite PK object of an instance of a class that uses composite primary key'() {
		given: 'ClassRowMapping of BookPage'
		ClassRowMapping mapping = new ClassRowMapping(BookPage.class)
		
		and: 'BookPage object with ISBN 1234567890 and page number 321'
		BookPage page = new BookPage();
		page.setIsbn('1234567890')
		page.setPageNumber(321)
		
		expect: 'class row mapping of BookPage to know that BookPage class uses a composite primary key, not scalar'
		mapping.isCompositePrimaryKey()
		mapping.isScalarPrimaryKey() == false
		
		when: 'getting the composite PK object BookPagePK'
		BookPagePK pk = mapping.getOrCreateId(page)
		
		then: 'primary key object contains ISBN 1234567890 and page number 321'
		pk.isbn == '1234567890'
		pk.pageNumber == 321
	}
}