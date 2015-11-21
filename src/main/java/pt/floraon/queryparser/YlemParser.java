package pt.floraon.queryparser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.results.SimpleTaxonResult;
import pt.floraon.server.Constants;

public class YlemParser {
	private String[] parsers={"GeoPointParser","FullTextPrefixParser","AttributesFullMatchParser","TaxonPrefixParser","TaxonPartialParser"};
	private String[] query=new String[1];
	private FloraOnGraph graph;
	public YlemParser(FloraOnGraph graph,String query) {
		this.query[0]=query;//.trim().replaceAll(" +", " ");
		this.graph=graph;
	}
	
	public List<SimpleTaxonResult> execute() {
		TokenParser tp;
		QueryString qs=new QueryString();
		qs.query=this.query;
		qs.results=null;
		
		Class<?> myClass;
		try {
			for(String parser : parsers) {
				myClass = Class.forName("pt.floraon.queryparser."+parser);
				Class<?>[] types = {FloraOnGraph.class, QueryString.class};
				Constructor<?> constructor = myClass.getConstructor(types);
				
				Object[] parameters = {this.graph, qs};
				tp=(TokenParser)constructor.newInstance(parameters);
				qs=tp.parse();
				if(qs.query.length==0) {
					System.out.println("All consumed, stopped at parser "+parser);
					break;
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		if(qs.query.length>0) {
			System.out.println(Constants.ANSI_RED+"Could not understand: "+Arrays.toString(qs.query)+Constants.ANSI_RESET);
		}
		return qs.results;
	}
}
