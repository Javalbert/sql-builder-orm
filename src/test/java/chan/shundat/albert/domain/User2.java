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
}