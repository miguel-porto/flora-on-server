package pt.floraon.entities;

public class AttributeVertex extends GeneralDBNode {
	protected String name,shortName,description;
	
	public AttributeVertex(String name,String shortName,String description) {
		this.name=name.trim();
		this.shortName=shortName;
		this.description=description;
	}
	
	public AttributeVertex(Attribute at) {
		super(at.baseNode);
		this.name=at.baseNode.name;
		this.shortName=at.baseNode.shortName;
		this.description=at.baseNode.description;
	}	
	
	public AttributeVertex(AttributeVertex at) {
		super(at);
		this.name=at.name;
		this.shortName=at.shortName;
		this.description=at.description;
	}	
}
