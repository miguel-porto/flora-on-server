package pt.floraon.queryparser;

import java.util.ArrayList;
import java.util.List;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.server.Constants;

public abstract class TokenParser implements Comparable<TokenParser> {
	protected FloraOnGraph graph;
	protected QueryString curquery;
	protected String classname;
	protected List<String> afterQuery=new ArrayList<String>();

	protected abstract Integer getOrder();
	public abstract QueryString parse();
	public TokenParser(FloraOnGraph graph,QueryString query) {
		this.curquery=query;
		this.graph=graph;
		this.classname=Constants.ANSI_YELLOW+"["+this.getClass().getSimpleName()+"] "+Constants.ANSI_RESET;
	}
	
	@Override
	public int compareTo(TokenParser o) {
		return this.getOrder().compareTo(o.getOrder());
	}
}
