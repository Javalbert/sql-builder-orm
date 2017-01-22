package chan.shundat.albert.orm

import chan.shundat.albert.domain.Person
import spock.lang.Specification

class ClassRowMappingSpec extends Specification {
	def 'create Person class row mapping'() {
		given: 'a ClassRowMapping of Person class'
		ClassRowMapping mapping = new ClassRowMapping(Person.class);
		
		expect: 'the right catalog, schema, and table name'
		mapping.catalog == 'Albert'
		mapping.schema == 'dbo'
		mapping.table == 'Person'
	}
}