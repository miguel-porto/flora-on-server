package pt.floraon.queryparser;

import java.util.List;

public class MatchList {
	protected String matchString;	// the original string which produced the matches
	protected List<Match> matches;	// the matches, as returned by the AQL query
	
	public MatchList(String query, List<Match> matches) {
		this.matchString=query;
		this.matches=matches;
	}
}
