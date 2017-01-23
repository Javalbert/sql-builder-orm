package chan.shundat.albert.orm

import chan.shundat.albert.domain.BookPage
import chan.shundat.albert.domain.BookPagePK
import chan.shundat.albert.domain.Person
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
		
		and: 'Person object with personKey set'
		Person person = new Person()
		person.setPersonKey(123)
		
		when: 'Getting primary key of Person object'
		Object id = mapping.getOrCreateId(person)
		
		then: 'Primary key is correct'
		id == 123
	}
	
	def 'Get primary key of object via map key'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		and: 'Person object with personKey set'
		Person person = new Person()
		person.setPersonKey(123)
		
		when: 'Getting primary key of Person object'
		Object id = mapping.getMapKeyValue(person)
		
		then: 'Primary key is correct'
		id == 123
	}
	
	def 'Get map key value of object via map key'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class)
		
		and: 'Person object with full name set to "Albert Chan"'
		Person person = new Person()
		person.setFullName('Albert Chan')
		
		when: 'Getting full name of Person object via "Full Name" map key'
		String fullName = mapping.getMapKeyValue(person, "Full Name")
		
		then: 'Primary key is correct'
		fullName == 'Albert Chan'
	}
	
	def 'Get Book Page composite PK object'() {
		given: 'ClassRowMapping of BookPage'
		ClassRowMapping mapping = new ClassRowMapping(BookPage.class)
		
		and: 'Book Page object'
		BookPage page = new BookPage();
		page.setIsbn('1234567890')
		page.setPageNumber(321)
		
		expect: 'Class row mapping to know BookPage uses a composite primary key, not scalar'
		mapping.isCompositePrimaryKey()
		!mapping.isScalarPrimaryKey()
		
		when: 'Get the composite PK object BookPagePK'
		BookPagePK pk = mapping.getOrCreateId(page)
		
		then: 'Primary key contains ISBN 1234567890 and page number 321'
		pk.isbn == '1234567890'
		pk.pageNumber == 321
	}
}