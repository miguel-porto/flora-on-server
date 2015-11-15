package pt.floraon.queryparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.entities.SimpleTaxonResult;
import pt.floraon.server.Constants;
import pt.floraon.server.Constants.NodeTypes;
import pt.floraon.server.Constants.StringMatchTypes;

/**
 * A parser which looks for partial matches at the beginning of each word or word sequence.
 * It runs iteratively, starting with multiple-word sequences.
 * @author miguel
 *
 */
public class TaxonPrefixParser extends TokenParser {
	private Integer iteration=0;
	private String[] collectionRestrictions=new String[] {NodeTypes.taxent.toString()};
	
	public TaxonPrefixParser(FloraOnGraph graph, QueryString query) {
		super(graph, query);
	}
	
	@Override
	public QueryString parse() {
		List<SimpleTaxonResult> res=null;
		this.afterQuery.addAll(Arrays.asList(this.curquery.query));
		String[] splitpieces;
		List<String> piecesToParse;
		Integer groupWordsIn;
		Boolean anythingLeftToParse=false,anythingParsed=false;
		
		System.out.println(this.classname+Constants.ANSI_CYAN+"Entering"+Constants.ANSI_RESET+" with query "+Arrays.toString(this.curquery.query)+", iteration #"+(this.iteration+1));
		
		for(int whichPiece=0;whichPiece<this.curquery.query.length;whichPiece++) {
			String piece=this.curquery.query[whichPiece].trim().replaceAll(" +", " ");
			piecesToParse=new ArrayList<String>();
			
			StringBuilder sb;
			splitpieces=piece.split(" ");
			groupWordsIn=splitpieces.length>1 ? splitpieces.length-this.iteration-1 : 1;	// NOTE: here we start with groups less than the total number, because we assume that before this parser, a full length parser already acted.
			if(groupWordsIn<=0) continue;
			anythingLeftToParse=groupWordsIn>1;
			
			System.out.println(this.classname+"Parsing part #"+(whichPiece+1)+" \""+piece+"\", grouping words in "+groupWordsIn);
			for(int i=0;i<splitpieces.length-groupWordsIn+1;i++) {
				sb=new StringBuilder();
				for(int j=i;j<i+groupWordsIn;j++)
					sb.append(splitpieces[j]+" ");
				piecesToParse.add(sb.toString().trim());
			}
			System.out.println(this.classname+"Pieces: "+Arrays.toString(piecesToParse.toArray()));

// rosales rosaceae perto:39 -8.5 rubus flagellaris carnudo
			for(String smallpiece:piecesToParse) {
				smallpiece=smallpiece.trim();
				try {
					res=this.graph.speciesTextQuerySimple(smallpiece,StringMatchTypes.PREFIX,false,collectionRestrictions);
					/*	this alternative below is actaully slower...
					if(this.curquery.results==null)
						res=this.graph.speciesTextQuerySimple(smallpiece,false,false);
					else
						res=this.graph.inverseSpeciesTextQuery(this.curquery.results,smallpiece,false);*/
				} catch (ArangoException e) {
					e.printStackTrace();
				}
				
				if(res.size()>0) {	// query has results, intersect with previous results
					System.out.println(this.classname+Constants.ANSI_GREENBOLD+"Found "+res.size()+Constants.ANSI_RESET+" results for \""+smallpiece+"\"");
					this.curquery.results=SimpleTaxonResult.mergeResultLists(this.curquery.results,res);

					String rep=piece.replace(smallpiece, "").trim().replaceAll(" +", " ");
					//System.out.println("\tReplace: "+smallpiece+" in "+piece);
// TODO if there's a match, no need to parse the overlapping pieces that come next! first come...
					this.afterQuery.set(whichPiece,rep);
					piece=rep;
					anythingParsed=true;
/*
					if(rep.equals(""))
						this.afterQuery.remove(whichPiece);
					else {
						this.afterQuery.set(whichPiece,rep);
						piece=rep;
					}*/
				} else System.out.println(this.classname+Constants.ANSI_GREEN+"No results"+Constants.ANSI_RESET+" for \""+smallpiece+"\"");
			}
		}
		
		for(Iterator<String> it=this.afterQuery.iterator(); it.hasNext();) {
		    if(it.next().equals("")) it.remove();
		}
		this.curquery.query=this.afterQuery.toArray(new String[0]);
		
		System.out.println(this.classname+"Left unparsed: "+Arrays.toString(this.curquery.query));
		
		// no more query pieces to parse, stop chain
		if(this.curquery.query.length==0) return this.curquery;

		if(!anythingLeftToParse) return this.curquery;
		TaxonPrefixParser ptpp=new TaxonPrefixParser(this.graph,this.curquery);
		ptpp.setIteration(anythingParsed ? 0 : this.iteration+1);
		return ptpp.parse();
	}

	@Override
	protected Integer getOrder() {
		return 2;
	}
	
	public void setIteration(Integer iter) {
		this.iteration=iter;
	}
}
