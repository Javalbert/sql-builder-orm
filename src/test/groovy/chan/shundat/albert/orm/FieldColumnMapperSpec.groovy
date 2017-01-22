package chan.shundat.albert.orm

import chan.shundat.albert.domain.Person
import chan.shundat.albert.domain.Person2

import java.lang.reflect.Field
import java.lang.reflect.Method
import spock.lang.Specification

class FieldColumnMapperSpec extends Specification {
	private FieldColumnMapper mapper
	private FieldColumnMapper mapper2
	
	def setup() {
		mapper = new FieldColumnMapper(Person.class)
		mapper2 = new FieldColumnMapper(Person2.class)
	}
	
	def 'Return column name of Person first name field'() {
		given: 'First name field of Person'
		Field field = mapper.getField('firstName')
		Column columnAnnotation = field.getAnnotation(Column.class)
		
		expect: 'Column annotation to have value of "first_name"'
		columnAnnotation.value() == 'first_name'
		
		and: 'Return column name of "version" from class field'
		mapper.getColumnName(field) == 'first_name'
	}
	
	def "Return column name of field where column annotation's value is blank"() {
		given: 'Version field of Person class and its Column annotation'
		Field field = mapper.getField('version')
		Column columnAnnotation = field.getAnnotation(Column.class)
		
		expect: 'Column annotation to have blank value'
		columnAnnotation.value() == ''
		
		and: 'Return column name of "version" from class field'
		mapper.getColumnName(field) == 'version'
	}
	
	def 'Map first name field of Person class as a column'() {
		given: 'First name field of Person'
		Field field = mapper.getField('firstName')
		
		when: 'Creating and adding mapping of firstName field'
		FieldColumnMapping mapping = mapper.mapFieldToColumn(field)
		mapper.addMapping(mapping)
		
		then: 'Mapping is added'
		!mapper.getFieldColumnMappingList().isEmpty()
		!mapper.getFieldColumnMappings().isEmpty()
	}
	
	def 'Verify that field without Column or Alias annotation is not mapped'() {
		given: 'jsonString field of Person class and its Column and Alias annotations'
		Field field = mapper.getField('jsonString')
		Column columnAnnotation = field.getAnnotation(Column.class)
		Alias aliasAnnotation = field.getAnnotation(Alias.class)
		
		expect: 'Field "jsonString" exists on Person class'
		field != null
		
		and: 'Column and alias annotations for the field are null'
		columnAnnotation == null
		aliasAnnotation == null
		
		when: 'Creating and then adding the mapping'
		FieldColumnMapping mapping = mapper.mapFieldToColumn(field)
		mapper.addMapping(mapping)
		
		then: 'Mapping is not actually added because it is null'
		mapping == null
		mapper.getFieldAliasMappings().isEmpty()
		mapper.getFieldColumnMappingList().isEmpty()
		mapper.getFieldColumnMappings().isEmpty()
	}
	
	def 'Throw error when a field is both a primary key and a version column'() {
		given: 'A field that is both a primary key and a version column'
		Field field = mapper2.getField('personKey')
		
		when: 'map field to column'
		mapper2.mapFieldToColumn(field)
		
		then: 'throw error'
		thrown(IllegalArgumentException)
	}
	
	def 'Add field column mapping by looking at its accessor method instead of field'() {
		given: 'Method that gets last name'
		Method getLastName = mapper.getMethod('getLastName')
		
		when: 'Creating and adding the last name mapping via getter method'
		FieldColumnMapping mapping = mapper.mapPropertyToColumn(getLastName)
		mapper.addMapping(mapping)
		
		then: 'Mapping of last name is added'
		!mapper.getFieldColumnMappingList().isEmpty()
		!mapper.getFieldColumnMappings().isEmpty()
	}
	
	def 'Throw error when a property has @Column annotation defined in both getter and setter'() {
		given: 'Getter and setter of last name in Person2 class'
		Method getLastName = mapper2.getMethod('getLastName')
		Method setLastName = mapper2.getMethod('setLastName')
		
		and: 'FieldColumnMappings for them'
		FieldColumnMapping mappingViaGetter = mapper2.mapPropertyToColumn(getLastName)
		FieldColumnMapping mappingViaSetter = mapper2.mapPropertyToColumn(setLastName)
		
		when: 'When adding the mappings'
		mapper2.addMapping(mappingViaGetter)
		mapper2.addMapping(mappingViaSetter)
		
		then: 'throw error'
		thrown(IllegalArgumentException)
	}
}