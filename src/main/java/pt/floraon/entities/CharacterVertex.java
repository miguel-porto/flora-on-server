package pt.floraon.entities;

public class CharacterVertex extends GeneralDBNode {
	protected String name,shortName,description;
	
	public CharacterVertex(String name,String shortName,String description) {
		this.name=name.trim();
		this.shortName=shortName;
		this.description=description;
	}
	
	public CharacterVertex(Character at) {
		super(at);
		this.name=at.name;
		this.shortName=at.shortName;
		this.description=at.description;
	}	
	
	public CharacterVertex(CharacterVertex at) {
		super(at);
		this.name=at.name;
		this.shortName=at.shortName;
		this.description=at.description;
	}	
}
