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

import com.github.javalbert.orm.Column;
import com.github.javalbert.orm.Id;
import com.github.javalbert.orm.Table;
import com.github.javalbert.orm.Version;

@Table(catalog = "Albert", schema = "dbo", name = "Person2")
public class Person2 {
	@Version
	@Id
	@Column("person_key")
	private int personKey;
	private String lastName;
	
	public int getPersonKey() {
		return personKey;
	}
	public void setPersonKey(int personKey) {
		this.personKey = personKey;
	}
	@Column("last_name")
	public String getLastName() {
		return lastName;
	}
	@Column("last_name")
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}