package chan.shundat.albert.orm

import chan.shundat.albert.domain.Person
import spock.lang.Specification

class ClassRowMappingSpec extends Specification {
	def 'Verify fully qualified table name for Person class'() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class);
		
		expect: 'right catalog, schema, table name, and fully qualified name'
		mapping.catalog == 'Albert'
		mapping.schema == 'dbo'
		mapping.table == 'Person'
		mapping.tableIdentifier == 'Albert.dbo.Person'
	}
	
	def "Register Person class's version field with field name instead of column annotation's value"() {
		given: 'ClassRowMapping of Person'
		ClassRowMapping mapping = new ClassRowMapping(Person.class);
		
		expect: 'version field is registered'
		mapping.getFieldColumnMappingList().any{ it.column == 'version' }
		mapping.getFieldColumnMappings().containsKey('version')
	}
}