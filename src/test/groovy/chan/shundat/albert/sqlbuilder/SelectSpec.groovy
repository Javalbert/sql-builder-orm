package chan.shundat.albert.sqlbuilder

import spock.lang.Specification

class SelectSpec extends Specification {
	def 'Create simple Select with 2 nodes'() {
		given: 'SelectList with personKey, and From with Albert.dbo.Person'
		
		SelectList list = new SelectList().
			column('personKey')
		From from = new From().
			tableName('Albert.dbo.Person')
		
		when: 'SelectList and From is added into Select object'
		Select select = new Select().
			list(list).
			from(from)
			
		then: 'Select should have 2 nodes, SelectList and From'
		select.nodes.size() == 2
	}
}