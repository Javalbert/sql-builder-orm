package chan.shundat.albert.sqlbuilder.parser;

import java.util.ArrayList;
import java.util.List;

public class ParseToken {
	private final List<ParseToken> nodes = new ArrayList<>();
	private final String token;
	
	public List<ParseToken> getNodes() { return nodes; }
		public String getToken() { return token; }
	
	public ParseToken(String token) {
		this(token, false);
	}
	
	public ParseToken(String token, boolean keyword) {
		this.token = keyword ? token.toUpperCase() : token;
	}
	
	public void addNode(ParseToken node) {
		nodes.add(node);
	}
	
	@Override
	public String toString() {
		return token + (nodes != null && !nodes.isEmpty() 
				? " (" + nodes.get(nodes.size() - 1).getToken() + ")" : "");
	}
}