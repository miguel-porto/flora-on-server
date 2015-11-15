package pt.floraon.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.EntityFactory;

import pt.floraon.entities.ResultItem;
/**
 * Utility class to handle query responses
 * @author miguel
 *
 * @param <T>
 */
public class ResultProcessor<T extends ResultItem> {
	 private final Class<T> type;

	public ResultProcessor(Class<T> type) {
		this.type = type;
	}

	public String toCSVTable(Iterator<T> it) {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
    	while (it.hasNext()) {
    		tmp=it.next();
    		sb.append(tmp.toCSVLine()).append("\n");
    	}
    	return sb.toString();
	}

	public String toHTMLTable(Iterator<T> it) {
		StringBuilder sb=new StringBuilder(); 
		T tmp;
		sb.append("<table>");
    	while (it.hasNext()) {
    		tmp=it.next();
    		sb.append(tmp.toHTMLLine());
    	}
    	sb.append("</table>");
    	return sb.toString();
	}

    public String toJSON(Iterator<T> it) {
    	List<T> out=new ArrayList<T>();
		while(it.hasNext()) {
			out.add(it.next());
		}

    	return EntityFactory.toJsonString(out);
    }
    
	public String toDWC() {
		// TODO export DWC string
		return null;
	}
}
