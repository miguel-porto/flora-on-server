package pt.floraon.queryparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import pt.floraon.driver.results.SimpleTaxonResult;

public class QueryObject implements Iterable<QueryObject.QueryPiece> {
	protected List<SimpleTaxonResult> results=null;
	protected List<QueryPiece> queryPieces;

	public QueryObject(String[] queryPieces) {
		this.queryPieces=new ArrayList<QueryPiece>();
		for(String q : queryPieces) {
			this.queryPieces.add(new QueryPiece(q));
		}
	}
	
	/**
	 * An iterator for QueryPieces. <b>Note that</b> all iterated elements are consumed unless you call splitQueryPiece or keepQueryPiece.
	 * This means that if you don't call it, you can only iterate once, and lose the elements forever.
	 * @author miguel
	 *
	 */
	protected class QueryPieceIterator implements Iterator<QueryPiece> {
		private int i=0;
		private List<QueryPiece> newList=new ArrayList<QueryPiece>();
		
		/**
		 * Takes the current QueryPiece and replaces it by splitting it according to pattern.
		 * @param piece
		 * @param pattern
		 */
		public void splitQueryPiece(Pattern pattern) {
			for(String s:pattern.split(queryPieces.get(i-1).query)) {
				if(!s.trim().equals("")) newList.add(new QueryPiece(s.trim()));
			}
		}
		
		/*public void keepQueryPiece() {
			newList.add(new QueryPiece(queryPieces.get(i-1).query));
		}*/
		
		@Override
		public boolean hasNext() {
			if(i<queryPieces.size())
				return true;
			else {	// iterator has finished, replace old querypieces with new list
				queryPieces=newList;
				return false;
			}
		}

		@Override
		public QueryPiece next() {
			i++;
			return queryPieces.get(i-1);
		}

		@Override
		public void remove() {
		}
	}
	
	public class QueryPiece implements Iterable<String> {
		protected String query;
		protected List<MatchList> matchLists;
		
		protected QueryPiece(QueryPiece querypiece) {
			this.query=querypiece.query;
		}
		
		protected QueryPiece(String query) {
			this.query=query;
			this.matchLists=new ArrayList<MatchList>();
		}

		@Override
		public String toString() {
			return this.query;
		}

		@Override
		public WordSequenceIterator iterator() {
			return new WordSequenceIterator(query);
		}
	}
	

	@Override
	public QueryPieceIterator iterator() {
		return new QueryPieceIterator();
	}
}
