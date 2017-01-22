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
	@Column("name")
	private String name;
	@Column("date_of_birth")
	private Date dateOfBirth;
	@Column("version")
	private int version;
	
	public int getPersonKey() {
		return personKey;
	}
	public void setPersonKey(int personKey) {
		this.personKey = personKey;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + personKey;
		result = prime * result + version;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (dateOfBirth == null) {
			if (other.dateOfBirth != null)
				return false;
		} else if (!dateOfBirth.equals(other.dateOfBirth))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (personKey != other.personKey)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Person [personKey=" + personKey + ", name=" + name + ", dateOfBirth=" + dateOfBirth + ", version="
				+ version + "]";
	}
}