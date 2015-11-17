package pt.floraon.queryparser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arangodb.ArangoException;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.entities.SimpleTaxonResult;
import pt.floraon.server.Constants;

public final class GeoPointParser extends TokenParser {
	private Pattern pattern=Pattern.compile("perto: *(-?[0-9.,]+) *(-?[0-9.,]+)",Pattern.CASE_INSENSITIVE);
	
	public GeoPointParser(FloraOnGraph graph, QueryString query) {
		super(graph, query);
	}

	public QueryString parse() {
		List<SimpleTaxonResult> res=null;
		Matcher mat;
		Float lat=null,lng=null;
		System.out.println(this.classname+Constants.ANSI_CYAN+"Entering"+Constants.ANSI_RESET+" with query "+Arrays.toString(this.curquery.query));
		
		for(String piece:this.curquery.query) {
			mat=this.pattern.matcher(piece);
			while(mat.find()) {
				System.out.println(this.classname+"Parsing "+mat.group(0));
				if(mat.groupCount()==2) {
					lat=Float.parseFloat(mat.group(1));
					lng=Float.parseFloat(mat.group(2));
				}		
				if(lat!=null && lng!=null) {
					try {
						res=this.graph.dbSpecificQueries.findListTaxaWithin(lat,lng,15000);
					} catch (ArangoException e) {
						e.printStackTrace();
					}
				}
				System.out.println(this.classname+Constants.ANSI_GREENBOLD+"Found "+res.size()+Constants.ANSI_RESET+" results here");
				
				this.curquery.results=SimpleTaxonResult.mergeResultLists(this.curquery.results,res);
				//System.out.println("# sp:"+this.curquery.results.size());
			}
			
			for(String s:this.pattern.split(piece)) {
				if(!s.trim().equals("")) this.afterQuery.add(s.trim());
			}
		}
		this.curquery.query=this.afterQuery.toArray(new String[0]);
		System.out.println(this.classname+"Left unparsed: "+Arrays.toString(this.curquery.query));
		return this.curquery;
	}

	@Override
	protected Integer getOrder() {
		return 1;
	}
}
