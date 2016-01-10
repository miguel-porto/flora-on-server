package pt.floraon.entities;

import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.FloraOnException;

/**
 * Represents a precisely bounded geographical region, optionally associated with a polygon.
 * Geographical regions can be hierarchically arranged using PART_OF relationships.
 * @author miguel
 *
 */
public class TerritoryVertex extends GeneralDBNode {
	protected String name,shortName,polygon,territoryType,theme;
	protected Boolean showInChecklist; 
	
	public TerritoryVertex(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException {
		if(name==null || name.trim().equals("")) throw new FloraOnException("Territory must have a name");
		if(shortName==null || !shortName.matches("^[a-zA-Z0-9_-]+$")) throw new FloraOnException("Territory must have a short name with only alphanumeric digits");
		this.name=name;
		this.shortName=shortName;
		this.territoryType=type.toString();
		this.theme=theme;
		this.showInChecklist=showInChecklist;
	}

	public TerritoryVertex(String name, String shortName) throws FloraOnException {
		this(name, shortName, TerritoryTypes.COUNTRY, null, true);
	}

	public TerritoryVertex(Territory te) throws FloraOnException {
		this(te.baseNode.name, te.baseNode.shortName, TerritoryTypes.valueOf(te.baseNode.territoryType), te.baseNode.theme, te.baseNode.showInChecklist);
	}
	
	public String getName() {
		return this.name;
	}

	public String getShortName() {
		return this.shortName;
	}
	
	public TerritoryTypes getType() {
		return TerritoryTypes.valueOf(this.territoryType);
	}
	
	public String getTheme() {
		return this.theme;
	}
}
