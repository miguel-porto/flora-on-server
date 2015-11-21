package pt.floraon.queryparser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.results.SimpleTaxonResult;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;
import pt.floraon.server.Constants.StringMatchTypes;

/**
 * A parser which looks for exact matches of the given query string parts
 * @author miguel
 *
 */
public class FullTextPrefixParser extends TokenParser {
	private String[] collectionRestrictions=new String[] {NodeTypes.taxent.toString(),NodeTypes.attribute.toString()};

	public FullTextPrefixParser(FloraOnGraph graph, QueryString query) {
		super(graph, query);
	}
	
	@Override
	public QueryString parse() {
		List<SimpleTaxonResult> res=null;
		this.afterQuery.addAll(Arrays.asList(this.curquery.query));
		System.out.println(this.classname+Constants.ANSI_CYAN+"Entering"+Constants.ANSI_RESET+" with query "+Arrays.toString(this.curquery.query));
		
		for(int whichPiece=0;whichPiece<this.curquery.query.length;whichPiece++) {
			String piece=this.curquery.query[whichPiece].trim().replaceAll(" +", " ");
			System.out.println(this.classname+"Parsing part #"+(whichPiece+1)+" \""+piece+"\", whole string");

			try {
				res=this.graph.dbGeneralQueries.speciesTextQuerySimple(piece,StringMatchTypes.PREFIX,false,collectionRestrictions);	// search all vertex collections
			} catch (ArangoException e) {
				e.printStackTrace();
			}
			
			if(res.size()>0) {	// query has results, intersect with previous results
				System.out.println(this.classname+Constants.ANSI_GREENBOLD+"Found "+res.size()+Constants.ANSI_RESET+" results for \""+piece+"\"");
				this.curquery.results=SimpleTaxonResult.mergeResultLists(this.curquery.results,res);

				//this.afterQuery.remove(whichPiece);
				this.afterQuery.set(whichPiece,"");
			} else System.out.println(this.classname+Constants.ANSI_GREEN+"No results"+Constants.ANSI_RESET+" for \""+piece+"\"");
		}

		for(Iterator<String> it=this.afterQuery.iterator(); it.hasNext();) {
		    if(it.next().equals("")) it.remove();
		}
		this.curquery.query=this.afterQuery.toArray(new String[0]);

		System.out.println(this.classname+"Left unparsed: "+Arrays.toString(this.curquery.query));
		return this.curquery;
	}

	@Override
	protected Integer getOrder() {
		return 2;
	}
}
