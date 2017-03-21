package com.github.javalbert.orm

import javax.xml.validation.Schema

import com.github.javalbert.domain.BookPage
import com.github.javalbert.domain.BookPagePK
import com.github.javalbert.domain.Customer
import com.github.javalbert.domain.DataTypeHolder
import com.github.javalbert.domain.Person
import com.github.javalbert.orm.ClassRowRegistration
import com.github.javalbert.orm.ClassRowRegistration.ClassMember
import com.github.javalbert.orm.JdbcMapper

import spock.lang.Specification

class ClassRowRegistrationSpec extends Specification {
	private JdbcMapper mapper
	
	def setup() {
		mapper = new JdbcMapper()
	}
	
	def 'Map a single field that is an auto-increment primary key column'() {
		given: 'ClassRowRegistration object that registers "customer_id" column of Customer object'
		ClassRowRegistration registration = new ClassRowRegistration(Customer.class)
				.table('Customer')
				.columnInField('customerId', 'customer_id', null, ClassRowRegistration.FLAG_ID | ClassRowRegistration.FLAG_GENERATED_VALUE);
		
		when: 'registration object for Customer is registered in JdbcMapper'
		mapper.register(registration)
		ClassRowMapping mapping = mapper.getMappings().get(Customer.class)
		
		then: 'column "customer_id" is registered'
		mapping.getFieldColumnMappings().containsKey('customer_id')
		
		and: 'is an auto-increment primary key'
		mapping.isScalarPrimaryKey()
		mapping.isAutoIncrementId()
		
		and: 'but column "full_name" is not registered'
		mapping.getFieldColumnMappings().containsKey('full_name') == false
	}
	
	def 'Get instance of ID class of entity that uses composite PK'() {
		given: 'BookPage registration with BookPagePK ID class'
		ClassRowRegistration registration = new ClassRowRegistration(BookPage.class)
				.catalog('Albert')
				.schema('dbo')
				.table('BookPage')
				.idClass(BookPagePK.class)
				.idClassColumnInField('isbn', 'isbn')
				.idClassColumnInField('pageNumber', 'page_number')
				.columnInField('isbn', 'isbn', null, ClassRowRegistration.FLAG_ID)
				.columnInField('pageNumber', 'pageNumber', null, ClassRowRegistration.FLAG_ID)
		
		and: 'instance of BookPage'
		BookPage bookPage = new BookPage()
		bookPage.isbn = '1234567890'
		bookPage.pageNumber = 321
		
		when: 'registered and ClassRowMapping is retrieved for BookPage class'
		mapper.register(registration)
		ClassRowMapping mapping = mapper.getMappings().get(BookPage.class)
		
		then: 'ID class of BookPage is BookPagePK'
		mapping.idClass == BookPagePK.class
		
		and: '"isbn" and "page_number" columns in BookPagePK are mapped'
		mapping.idClassMappings[0].column == 'isbn'
		mapping.idClassMappings[1].column == 'page_number'
	}
	
	def 'Map timestamp field'() {
		given: 'registration object for DataTypeHolder'
		ClassRowRegistration reg = new ClassRowRegistration(DataTypeHolder.class)
				.table('DataTypeHolder')
				.columnInField('timestampVal', 'timestamp_val', null, ClassRowRegistration.FLAG_TIMESTAMP)
		
		when: 'registered and timestamp_val FieldColumnMapping is retrieved'
		mapper.register(reg)
		ClassRowMapping classRowMapping = mapper.getMappings().get(DataTypeHolder.class)
		FieldColumnMapping timestampValMapping = classRowMapping.getFieldColumnMappings().get('timestamp_val')
		
		then: 'timestamp_val is mapped as a timestamp data type'
		timestampValMapping.jdbcType == FieldColumnMapping.JDBC_TYPE_TIMESTAMP
	}
	
	def 'Map version field'() {
		given: 'registration object for Person'
		ClassRowRegistration reg = new ClassRowRegistration(Person.class)
				.catalog('Albert')
				.schema('dbo')
				.table('Person')
				.columnInField('version', 'version', null, ClassRowRegistration.FLAG_VERSION)
		
		when: 'registered and "version" field FieldColumnMapping is retrieved'
		mapper.register(reg)
		ClassRowMapping classRowMapping = mapper.getMappings().get(Person.class)
		FieldColumnMapping versionMapping = classRowMapping.getFieldColumnMappings().get('version')
		
		then: '"version" field is mapped as a row versioning column'
		versionMapping.isVersion()
	}
}
