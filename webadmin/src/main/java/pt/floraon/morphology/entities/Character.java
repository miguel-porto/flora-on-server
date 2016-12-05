package pt.floraon.morphology.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.entities.NamedDBNode;

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
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.character;
	}
	
	@Override
	public JsonObject toJson() {
		return super._toJson();
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

}
