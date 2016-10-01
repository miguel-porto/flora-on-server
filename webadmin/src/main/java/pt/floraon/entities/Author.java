package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.NodeTypes;

public class Author extends GeneralDBNode {
	protected Integer idAut;
	protected String name;
	protected String email;
	protected String acronym;
	protected Integer level;
	protected String username;
	
	
	public Author(Integer idAut,String name,String email,String acronym,String username,Integer level) {
		this.idAut=idAut;
		this.name=name;
		this.email=email;
		this.acronym=acronym;
		this.username=username;
		this.level=level;
	}
	
	public Author(Author aut) {
		super(aut);
		this.idAut=aut.idAut;
		this.name=aut.name;
		this.email=aut.email;
		this.acronym=aut.acronym;
		this.username=aut.username;
		this.level=aut.level;
	}
	
	public Integer getIdAut() {
		return this.idAut;
	}
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.author;
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
