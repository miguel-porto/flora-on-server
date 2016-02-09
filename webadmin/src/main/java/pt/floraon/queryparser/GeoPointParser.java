package pt.floraon.queryparser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.queryparser.QueryObject.QueryPiece;
import pt.floraon.queryparser.QueryObject.QueryPieceIterator;
import pt.floraon.results.SimpleTaxonResult;

/**
 * Implements a parser for geographic queries based on a single point (latitude longitude) given in the form:
 * perto: xxx yyy
 * @author miguel
 *
 */
public final class GeoPointParser extends TokenParser {
	private final int RADIUS=15000;
	private Pattern pattern=Pattern.compile("perto: *(-?[0-9.,]+) *(-?[0-9.,]+)",Pattern.CASE_INSENSITIVE);
	
	public GeoPointParser(FloraOn graph, QueryObject query) {
		super(graph, query);
	}

	public QueryObject parse() {
		List<SimpleTaxonResult> res=null;
		Matcher mat;
		Float lat=null,lng=null;
		System.out.println(this.classname+Constants.ANSI_CYAN+"Entering"+Constants.ANSI_RESET+" with query "+Arrays.toString(this.currentQueryObj.queryPieces.toArray()));
		
		QueryPieceIterator itqp=this.currentQueryObj.iterator();
		QueryPiece piece;
		
		while(itqp.hasNext()) {		// iterate over all pieces
			piece=itqp.next();
			mat=this.pattern.matcher(piece.query);
			while(mat.find()) {
				System.out.println(this.classname+"Parsing "+mat.group(0));
				if(mat.groupCount()==2) {
					lat=Float.parseFloat(mat.group(1));
					lng=Float.parseFloat(mat.group(2));
				}		
				if(lat!=null && lng!=null) {
					try {
						res=graph.getQueryDriver().findListTaxaWithin(lat,lng,RADIUS);
					} catch (FloraOnException e) {
						e.printStackTrace();
					}
				}
				System.out.println(this.classname+Constants.ANSI_GREENBOLD+"Found "+res.size()+Constants.ANSI_RESET+" results here");
				
				this.currentQueryObj.results=SimpleTaxonResult.mergeSimpleTaxonResultLists(this.currentQueryObj.results,res);
			}
			
			// split this piece
			itqp.splitQueryPiece(pattern);
		}
		System.out.println(this.classname+"Left unparsed: "+Arrays.toString(this.currentQueryObj.queryPieces.toArray()));
		return this.currentQueryObj;
	}

	@Override
	protected Integer getOrder() {
		return 1;
	}
}
