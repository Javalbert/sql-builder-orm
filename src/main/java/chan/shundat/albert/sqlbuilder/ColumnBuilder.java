package chan.shundat.albert.sqlbuilder;

public interface ColumnBuilder<T> {
	T column(String name);

	/**
	 * Should call <pre>{@code column(String)}</pre> afterwards
	 * @param alias
	 * @return
	 */
	T tableAlias(String alias);
	
	/**
	 * Should call <pre>{@code column(String)}</pre> afterwards
	 * @param name
	 * @return
	 */
	T tableName(String name);
}