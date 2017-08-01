package pt.floraon.taxonomy.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.entities.NamedDBNode;

/**
 * Represents a precisely bounded geographical region, optionally associated with a polygon.
 * Geographical regions can be hierarchically arranged using PART_OF relationships.
 * @author miguel
 *
 */
public class Territory extends NamedDBNode {
	protected String shortName,polygon,territoryType,theme;
	protected Boolean showInChecklist; 

	public Territory() {super();}

	public Territory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException {
		super(name);
		if(shortName==null || !shortName.matches("^[a-zA-Z0-9_-]+$")) throw new FloraOnException("Territory must have a short name with only alphanumeric digits");
		this.name=name;
		this.shortName=shortName;
		this.territoryType=type.toString();
		this.theme=theme;
		this.showInChecklist=showInChecklist;
	}

	public Territory(String name, String shortName) throws FloraOnException {
		this(name, shortName, TerritoryTypes.COUNTRY, null, true);
	}

	public Territory(Territory te) throws FloraOnException {
		this(te.name, te.shortName, TerritoryTypes.valueOf(te.territoryType), te.theme, te.showInChecklist);
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public Boolean getShowInChecklist() {
		return this.showInChecklist==null ? false : this.showInChecklist;
	}
	
	public TerritoryTypes getTerritoryType() {
		return TerritoryTypes.valueOf(this.territoryType);
	}
	
	public String getTheme() {
		return this.theme;
	}
	
	public void update(String name, String shortName, TerritoryTypes type, String theme, Boolean showInChecklist) throws FloraOnException {
		if(name==null || name.trim().length()==0) throw new TaxonomyException("Territory must have a name");
		if(shortName==null || !shortName.matches("^[a-zA-Z0-9_-]+$")) throw new FloraOnException("Territory must have a short name with only alphanumeric digits");
		if(name!=null) this.name=name;
		if(shortName!=null) this.shortName=shortName;
		if(type!=null) this.territoryType=type.toString();
		if(theme!=null) this.theme=theme;
		if(showInChecklist!=null) this.showInChecklist=showInChecklist;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + ((territoryType == null) ? 0 : territoryType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Territory other = (Territory) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (territoryType == null) {
			if (other.territoryType != null)
				return false;
		} else if (!territoryType.equals(other.territoryType))
			return false;
		return true;
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.territory;
	}

}
