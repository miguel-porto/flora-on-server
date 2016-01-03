package pt.floraon.entities;

/**
 * Represents a precisely bounded geographical region, optionally associated with a polygon.
 * Geographical regions can be hierarchically arranged using PART_OF relationships.
 * @author miguel
 *
 */
public class TerritoryVertex extends GeneralDBNode {
	protected String name,shortName,polygon;
	
	public TerritoryVertex(String name, String shortName) {
		this.name=name;
		this.shortName=shortName;
	}
	
	public String getName() {
		return this.name;
	}

	public String getShortName() {
		return this.shortName;
	}
}
