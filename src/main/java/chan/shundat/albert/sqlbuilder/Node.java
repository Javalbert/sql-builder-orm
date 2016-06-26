package chan.shundat.albert.sqlbuilder;

@SuppressWarnings("rawtypes")
public interface Node<T extends Node> {
	public static final int TYPE_BINARY_OPERATOR = 1;
	public static final int TYPE_CASE = 2;
	public static final int TYPE_COLUMN = 3;
	public static final int TYPE_COLUMN_LIST = 4;
	public static final int TYPE_COLUMN_VALUES = 5;
	public static final int TYPE_COMMON_TABLE_EXPRESSION = 6;
	public static final int TYPE_CONDITION = 7;
	public static final int TYPE_DELETE = 8;
	public static final int TYPE_EXPRESSION = 9;
	public static final int TYPE_FETCH = 10;
	public static final int TYPE_FROM = 11;
	public static final int TYPE_FUNCTION = 12;
	public static final int TYPE_GROUP_BY = 13;
	public static final int TYPE_HAVING = 14;
	public static final int TYPE_IN_VALUES = 15;
	public static final int TYPE_INSERT = 16;
	public static final int TYPE_LITERAL_BOOLEAN = 17;
	public static final int TYPE_LITERAL_NULL = 18;
	public static final int TYPE_LITERAL_NUMBER = 19;
	public static final int TYPE_LITERAL_STRING = 20;
	public static final int TYPE_OFFSET = 21;
	public static final int TYPE_ORDER_BY = 22;
	public static final int TYPE_ORDER_BY_SORT = 23;
	public static final int TYPE_PARAM = 24;
	public static final int TYPE_PREDICATE = 25;
	public static final int TYPE_SELECT = 26;
	public static final int TYPE_SELECT_LIST = 27;
	public static final int TYPE_SET_OPERATOR = 28;
	public static final int TYPE_SET_VALUE = 29;
	public static final int TYPE_SET_VALUES = 30;
	public static final int TYPE_TABLE = 31;
	public static final int TYPE_TOKEN = 32;
	public static final int TYPE_UPDATE = 33;
	public static final int TYPE_WHERE = 34;
	public static final int TYPE_WITH = 35;
	
	int getType();

	boolean accept(NodeVisitor visitor);
	/**
	 * Deep copy of the node and recursive child nodes, all of which are then immutable
	 * @return
	 */
	T immutable();
	/**
	 * Deep copy of the node and recursive child nodes, all of which are then mutable
	 * @return
	 */
	T mutable();
}