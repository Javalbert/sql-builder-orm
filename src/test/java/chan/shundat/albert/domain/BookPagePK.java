package chan.shundat.albert.domain;

import java.io.Serializable;

import chan.shundat.albert.orm.Column;
import chan.shundat.albert.orm.Id;

public class BookPagePK implements Serializable {
	private static final long serialVersionUID = 8321127835895887922L;
	
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