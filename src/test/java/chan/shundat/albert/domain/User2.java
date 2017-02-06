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

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Entity;
import chan.shundat.albert.orm.GeneratedValue;
import chan.shundat.albert.orm.Id;
import chan.shundat.albert.orm.Table;
import chan.shundat.albert.orm.Version;

@Entity
@Table(name = "User2")
public class User2 {
	@Id
	@GeneratedValue
	@Column("user_id")
	private int userId;
	@Column("name")
	private String name;
	@Version
	@Column("version")
	private int version;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public User2() {}
	
	public User2(String name) {
		this.name = name;
	}
}