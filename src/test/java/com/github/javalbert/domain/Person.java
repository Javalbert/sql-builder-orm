/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.domain;

import java.util.Date;

import com.github.javalbert.orm.Alias;
import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.GeneratedValue;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.MapKey;
import com.github.javalbert.orm.Table;
import com.github.javalbert.orm.Version;

@Table(catalog = "Albert", schema = "dbo", name = "Person")
public class Person {
	@Id
	@GeneratedValue
	@Column("person_key")
	private int personKey;
	@Column("first_name")
	private String firstName;
	private String lastName;
	@Column("date_of_birth")
	private Date dateOfBirth;
	@Version
	@Column
	private int version;
	
	// Aliased columns
	//
	@MapKey("Full Name")
	@Alias("full_name")
	private String fullName;
	
	// Non-database fields
	//
	private String jsonString;
	
	public int getPersonKey() {
		return personKey;
	}
	public void setPersonKey(int personKey) {
		this.personKey = personKey;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	@Column("last_name")
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	// Aliased columns
	//
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	// Non-database accessors
	//
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
}