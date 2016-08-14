package pt.floraon.entities;

import pt.floraon.driver.DatabaseException;

public class Character extends NamedDBNode {
	protected String shortName,description;
	
	public Character(String name,String shortName,String description) throws DatabaseException {
		super(name);
		this.shortName=shortName;
		this.description=description;
	}
	
	public Character(Character at) throws DatabaseException {
		super(at);
		this.name=at.name;
		this.shortName=at.shortName;
		this.description=at.description;
	}	
}
