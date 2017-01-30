package chan.shundat.albert.domain;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Id;
import chan.shundat.albert.orm.IdClass;
import chan.shundat.albert.orm.Table;

@IdClass(BookPagePK.class)
@Table(catalog = "Albert", schema = "dbo", name = "Book_Page")
public class BookPage {
	@Id
	@Column("isbn")
	private String isbn;
	@Id
	@Column("page_number")
	private int pageNumber;
	
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}