package pt.floraon.queryparser;

import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.StringMatchTypes;
import pt.floraon.driver.Constants.TaxonRank;

/**
 * Represents a match in the database. The purpose is to list all possible matches of a given query string in the database, so that they can be ordered
 * in terms of relevance, and queried for results.
 * @author miguel
 *
 */
public class Match {
	public Integer rank;
	private String nodeType;
	private int matchType;
	public String[] matches;
	public String query;
	
	@Override
	public String toString() {
		return "Match: "+nodeType+"; rank: "+getRank()+"; matches: "+(matches.length==1 ? matches[0] : (matches.length==2 ? matches[0]+", "+matches[1] : matches[0]+", "+matches[1]+", ..."));
	}
	
	public StringMatchTypes getMatchType() {
		return StringMatchTypes.values()[this.matchType];
	}
	
	public TaxonRank getRank() {
		if(this.rank==null) return null;
		return TaxonRank.getRankFromValue(this.rank);
	}
	
	public NodeTypes getNodeType() {
		if(this.nodeType==null) return null;
		return NodeTypes.valueOf(this.nodeType);
	}
}
