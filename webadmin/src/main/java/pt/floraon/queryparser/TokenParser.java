package pt.floraon.queryparser;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;

public abstract class TokenParser implements Comparable<TokenParser> {
	protected FloraOnDriver graph;
	protected QueryObject currentQueryObj;
	protected String classname;
	//protected List<String> afterQuery=new ArrayList<String>();

	protected abstract Integer getOrder();
	public abstract QueryObject parse();
	public TokenParser(FloraOnDriver graph,QueryObject query) {
		this.currentQueryObj=query;
		this.graph=graph;
		this.classname=Constants.ANSI_YELLOW+"["+this.getClass().getSimpleName()+"] "+Constants.ANSI_RESET;
	}
	
	@Override
	public int compareTo(TokenParser o) {
		return this.getOrder().compareTo(o.getOrder());
	}
}
