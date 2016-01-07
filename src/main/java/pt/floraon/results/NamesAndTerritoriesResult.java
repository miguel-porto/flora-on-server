package pt.floraon.results;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NamesAndTerritoriesResult extends SimpleNameResult implements ResultItem {
	@Override
	public String toHTMLTableRow(Object obj) {
		@SuppressWarnings("unchecked")
		List<String> territories=(List<String>) obj;
		int i;
		StringBuilder sb=new StringBuilder();
		try {
			sb.append("<tr data-key=\"").append(this._id).append("\"")
				.append(this.current==null ? "" : (this.current ? "" : " class=\"notcurrent\""))
				.append("><td><a href=\"?w=taxondetailspage&id="+URLEncoder.encode(this._id, StandardCharsets.UTF_8.name())+"\"><i>")
				.append(this.leaf==null ? "" : (this.leaf ? "" : "+"))
				.append(this.name)
				.append("</i></a></td><td>")
				.append(this.author)
				.append("</td><td>");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String t : territories) {
			if(this.territories.containsKey(t))
				sb.append("<div class=\"territory ").append(this.territories.get(t).toString()).append("\">").append(t).append("</div>");
			else
				sb.append("<div class=\"territory\">").append(t).append("</div>");
		}
		sb.append("</td></tr>");
		return sb.toString();
	}
}
