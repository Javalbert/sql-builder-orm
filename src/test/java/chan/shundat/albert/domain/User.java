package chan.shundat.albert.domain;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Entity;
import chan.shundat.albert.orm.GeneratedValue;
import chan.shundat.albert.orm.Id;
import chan.shundat.albert.orm.Table;

@Entity
@Table(name = "User")
public class User {
	@Id
	@GeneratedValue
	@Column("user_id")
	private int userId;
	@Column("name")
	private String name;
	
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
}