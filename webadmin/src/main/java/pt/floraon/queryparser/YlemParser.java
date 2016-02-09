package pt.floraon.queryparser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.StringMatchTypes;
import pt.floraon.queryparser.QueryObject.QueryPiece;
import pt.floraon.results.SimpleTaxonResult;

/**
 * This is the main interpreter for complex queries. It dispatches the query string to all the specific parsers and returns a list of results.
 * @author Miguel Porto
 *
 */
public class YlemParser {
	private String[] preParsers={"GeoPointParser"};		// regex-based parsers, which don't need to find matches in the DB
	private String[] query=new String[1];
	private FloraOn graph;
	public YlemParser(FloraOn graph,String query) {
		this.query[0]=query;//.trim().replaceAll(" +", " ");
		this.graph=graph;
	}
	
	/* Algorithm of the YLEM parser for a word sequence
	 * 
	 *              REGEX PHASE
	 *  Regex matches are removed, and the original word sequence is split into pieces according to the removed regex matches.
	 *  Each piece is treated separately hereafter
	 *  
	 *              MATCH PHASE
	 * The idea is that matches involving a higher number of consecutive words have priority, so they are sought first.
	 * If a match of, say, 3 words is found, then these words will not be searched any further.
	 * 
	 *  word1  word2  word3  word4  word5
	 * [                  1              ] 1: no matches
	 * 
	 * [              2           ] -----  2: no matches
	 *  ----- [              3           ] 3: no matches
	 *  
	 * [          4        ] ------------  4: no matches
	 *  ----- [         5         ] -----  5: no matches
	 *  ------------ [         6         ] 6: no matches
	 *  
	 * [      7     ] -------------------  7: no matches
	 *  ----- [      8     ] ------------  8: MATCH!
	 *  -----  MMMMMMMMMMMM [      9     ] 9: no matches
	 *  
	 * [ 10  ] MMMMMMMMMMMM  ------------  10: MATCH!
	 *  MMMMM  MMMMMMMMMMMM [ 11  ] -----  11: no matches
	 *  MMMMM  MMMMMMMMMMMM  ----- [ 12  ] 12: MATCH!
	 *  MMMMM  MMMMMMMMMMMM  +13++  MMMMM  13: returned as "left unparsed"
	 * 
	 * Each MATCH returns a list of MatchTypes of all matches it did, what kind of match (exact, prefix or partial)
	 * and what type of entity produced the match (taxent, attribute, etc.), sorted by relevance (this sorting happens at the AQL level)
	 * 
	 *              RESULT PHASE
	 * The 1st match of each match list (in this case, 3 match lists were returned) is processed and sent to the server as an optimized AQL query to fetch results.
	 * The intersection of the results of each match list is returned, along with the words which did not produce any match, and with any disambiguation questions. 
	 */
	public List<SimpleTaxonResult> execute() {
		TokenParser tp;
		QueryObject qs=new QueryObject(this.query);
		Class<?> myClass;

		// REGEX PHASE
		// first parse the regex-based patterns (e.g. geographic queries, etc.) and remove these patterns from the string
		try {
			for(String parser : preParsers) {
				myClass = Class.forName("pt.floraon.queryparser."+parser);
				Class<?>[] types = {FloraOn.class, QueryObject.class};
				Constructor<?> constructor = myClass.getConstructor(types);
				
				Object[] parameters = {this.graph, qs};
				tp=(TokenParser)constructor.newInstance(parameters);
				qs=tp.parse();
				if(qs.queryPieces.size()==0) {
					System.out.println("All consumed, stopped at parser "+parser);
					break;
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		// MATCH PHASE
		List<String> noMatch=new ArrayList<String>();
		if(qs.queryPieces.size()>0) {		// is there anything left to parse?
			WordSequenceIterator wsi;
			MatchList mat;
			String tmps;

			for(QueryPiece qp:qs.queryPieces) {		// process each query piece independently
				wsi=qp.iterator();
				while(wsi.hasNext()) {		// iterate of all possible combinations of consecutive words, in each query piece
					tmps=wsi.next();
					try {
						qp.matchLists.add(		// add all the matches of this word combination
							mat=new MatchList(tmps, this.graph.getQueryDriver().queryMatcher(tmps
								, StringMatchTypes.PARTIAL
								, new String[] {
									NodeTypes.taxent.toString()
									,NodeTypes.attribute.toString()
									,NodeTypes.territory.toString()}))
						);
						if(mat.matches.size()>0) wsi.markAsUsed();		// if there are matches, remove these words from further processing (to save time, but ideally they should be all processed)
						System.out.println(Constants.ANSI_YELLOW+"[Matcher] "+Constants.ANSI_RESET+"Query: \""+tmps+"\"");
						if(mat.matches.size()==0) {
							System.out.println(Constants.ANSI_YELLOW+"[Matcher] "+Constants.ANSI_GREEN+"No matches"+Constants.ANSI_RESET);
						} else {
							System.out.println(Constants.ANSI_YELLOW+"[Matcher] "+Constants.ANSI_GREENBOLD+"Found "+mat.matches.size()+Constants.ANSI_RESET+" matches");
							for(Match mt:mat.matches) {
								System.out.println(Constants.ANSI_YELLOW+"        * "+Constants.ANSI_RESET+mt.toString());
							}
						}
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				noMatch.addAll(wsi.getNoMatches());
			}
		}

		// RESULT PHASE
		List<SimpleTaxonResult> tmp;
		Match thisMatch;
		for(QueryPiece qp : qs.queryPieces) {		// process each query piece independently and intersect results
			for(MatchList ml : qp.matchLists) {
				try {
					if(ml.matches.size()>0) {
						thisMatch=ml.matches.get(0);
						System.out.println(Constants.ANSI_YELLOW+"[Fetcher] "+Constants.ANSI_RESET+"Fetch: \""+thisMatch.query+"\""+(thisMatch.rank!=null ? " ("+thisMatch.getRank().toString()+")" : ""));
						// take the most relevant match
						tmp=graph.getQueryDriver().fetchMatchSpecies(ml.matches.get(0),false);
						System.out.println(Constants.ANSI_YELLOW+"[Fetcher] "+Constants.ANSI_GREENBOLD+"Found "+tmp.size()+Constants.ANSI_RESET+" results.");
						if(qs.results==null)
							qs.results=tmp;
						else
							qs.results=SimpleTaxonResult.mergeSimpleTaxonResultLists(qs.results, tmp);
					}
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		if(noMatch.size()>0) {		// is there anything left to parse?
			System.out.println(Constants.ANSI_RED+"Could not understand: "+Arrays.toString(noMatch.toArray())+Constants.ANSI_RESET);
		}

		return qs.results;
	}
}
