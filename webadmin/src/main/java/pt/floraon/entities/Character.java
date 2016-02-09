package pt.floraon.entities;

public class Character extends GeneralDBNode {
	protected String name,shortName,description;
	
	public Character(String name,String shortName,String description) {
		this.name=name.trim();
		this.shortName=shortName;
		this.description=description;
	}
	
	public Character(Character at) {
		super(at);
		this.name=at.name;
		this.shortName=at.shortName;
		this.description=at.description;
	}	
}
