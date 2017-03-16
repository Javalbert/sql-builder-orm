package com.github.javalbert.orm

import com.github.javalbert.domain.Customer
import com.github.javalbert.orm.ClassRowRegistration
import com.github.javalbert.orm.ClassRowRegistration.ClassMember
import com.github.javalbert.orm.JdbcMapper

import spock.lang.Specification

class ClassRowRegistrationSpec extends Specification {
	private JdbcMapper mapper
	
	def setup() {
		mapper = new JdbcMapper()
	}
	
	def 'Register a single column'() {
		given: 'ClassRowRegistration object that registers "customer_id" column of Customer object'
		ClassRowRegistration registration = new ClassRowRegistration(Customer.class)
				.table('Customer')
				.columnInField('customerId', 'customer_id', null, ClassRowRegistration.FLAG_ID | ClassRowRegistration.FLAG_GENERATED_VALUE);
		
		when: 'registration object for Customer is registered in JdbcMapper'
		mapper.register(registration)
		ClassRowMapping mapping = mapper.getMappings().get(Customer.class)
		
		then: 'column "customer_id" is registered'
		mapping.getFieldColumnMappings().containsKey('customer_id')
		
		and: 'but column "full_name" is not registered'
		mapping.getFieldColumnMappings().containsKey('full_name') == false
	}
}
