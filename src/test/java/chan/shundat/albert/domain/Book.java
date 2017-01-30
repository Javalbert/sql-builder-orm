package chan.shundat.albert.domain;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Id;
import chan.shundat.albert.orm.Table;

@Table(catalog = "Albert", schema = "dbo", name = "Book")
public class Book {
	@Id
	@Column("isbn")
	private String isbn;
	@Column("title")
	private String title;
	
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}