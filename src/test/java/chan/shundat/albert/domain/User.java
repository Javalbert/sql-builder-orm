package chan.shundat.albert.domain;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Entity;
import chan.shundat.albert.orm.Id;
import chan.shundat.albert.orm.Table;
import chan.shundat.albert.orm.Version;

@Entity
@Table(name = "User")
public class User {
	@Id
	@Column("user_id")
	private int userId;
	@Column("name")
	private String name;
	@Column("active")
	private Boolean active;
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
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public User() {
		this(0, null);
	}
	
	public User(int userId, String name) {
		this(userId, name, null);
	}
	
	public User(int userId, String name, Boolean active) {
		this.userId = userId;
		this.name = name;
		this.active = active;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + userId;
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
		User other = (User) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (userId != other.userId)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
}