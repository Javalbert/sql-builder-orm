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