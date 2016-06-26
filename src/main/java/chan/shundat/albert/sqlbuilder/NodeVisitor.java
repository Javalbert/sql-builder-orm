package chan.shundat.albert.sqlbuilder;

@SuppressWarnings("rawtypes")
public interface NodeVisitor {
	boolean visit(Node node);
}