package pt.floraon.results;

import java.util.List;

public class NamesAndTerritoriesResult extends SimpleNameResult implements ResultItem {
	@Override
	public String toHTMLTableRow(Object obj) {
		@SuppressWarnings("unchecked")
		List<String> territories=(List<String>) obj;
		int i;
		StringBuilder sb=new StringBuilder();
		sb.append("<tr data-key=\"").append(this._id).append("\"")
			.append(this.current==null ? "" : (this.current ? "" : " class=\"notcurrent\""))
			.append("><td><i>")
			.append(this.leaf==null ? "" : (this.leaf ? "" : "+"))
			.append(this.name)
			.append("</i></td><td>")
			.append(this.author)
			.append("</td><td>");
		
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
