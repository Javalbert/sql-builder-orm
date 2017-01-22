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
package chan.shundat.albert.domain;

import java.util.Date;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Table;

@Table(catalog = "Albert", schema = "dbo", name = "Person")
public class Person {
	@Column("person_key")
	private int personKey;
	@Column("first_name")
	private String firstName;
	@Column("last_name")
	private String lastName;
	@Column("date_of_birth")
	private Date dateOfBirth;
	@Column
	private int version;
	
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
}