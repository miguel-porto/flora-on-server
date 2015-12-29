package pt.floraon.entities;

/**
 * Represents a precisely bounded geographical region, optionally associated with a polygon.
 * Geographical regions can be hierarchically arranged using PART_OF relationships.
 * @author miguel
 *
 */
public class TerritoryVertex extends GeneralDBNode {
	String name,shortName,polygon;
}
