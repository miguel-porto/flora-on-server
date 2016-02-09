package pt.floraon.entities;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.TerritoryTypes;

/**
 * Represents a precisely bounded geographical region, optionally associated with a polygon.
 * Geographical regions can be hierarchically arranged using PART_OF relationships.
 * @author miguel
 *
 */
public class Territory extends GeneralDBNode {
	protected String name,shortName,polygon,territoryType,theme;
	protected Boolean showInChecklist; 
	
	public Territory(String name, String shortName, TerritoryTypes type, String theme, boolean showInChecklist) throws FloraOnException {
		if(name==null || name.trim().equals("")) throw new FloraOnException("Territory must have a name");
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
	
	public String getName() {
		return this.name;
	}

	public String getShortName() {
		return this.shortName;
	}
	
	public Boolean getShowInChecklist() {
		return this.showInChecklist==null ? false : this.showInChecklist;
	}
	
	public TerritoryTypes getType() {
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
}
