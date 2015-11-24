package pt.floraon.queryparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This takes a string with a word sequence and iterates over all combinations of consecutive words, starting in the whole string itself.
 * @author miguel
 *
 */
public class WordSequenceIterator implements Iterator<String> {
	private String[] query;
	private boolean[] wasConsumed;
	private int nWords;
	private Integer i=null;
	
	public WordSequenceIterator(String query) {
		this.query=query.split(" ");
		this.nWords=this.query.length;
		this.wasConsumed=new boolean[this.nWords];
		Arrays.fill(this.wasConsumed, false);
	}

	private void nextWords() {
		i++;
		if(i+nWords>query.length) {
			i=0;
			nWords--;
		}
	}
	
	public List<String> getNoMatches() {
		List<String> out=new ArrayList<String>();
		for(int j=0;j<wasConsumed.length;j++) {
			if(!wasConsumed[j]) out.add(query[j]);
		}
		return out;
	}
	
	@Override
	public boolean hasNext() {
		if(i==null) {
			i=0;
			return true;
		}
		boolean used;
		do {
			nextWords();
			if(nWords==0) return false;
			used=false;
			for(int j=i;j<i+nWords;j++) {
				if(wasConsumed[j]) {
					used=true;
					break;
				}
			}
		} while(used);
		return true;
	}

	@Override
	public String next() {
//		out=Arrays.copyOfRange(query, i, i+nWords);
		//System.out.println("A: "+i+", "+nWords);
		StringBuilder sb=new StringBuilder();
		for(int j=i;j<i+nWords-1;j++)
			sb.append(query[j]).append(" ");
		sb.append(query[i+nWords-1]);
		return sb.toString();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}
	
	public void markAsUsed() {
		for(int j=i;j<i+nWords;j++) {
			wasConsumed[j]=true;
		}
	}
}
