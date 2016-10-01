package pt.floraon.entities;

import com.google.gson.JsonObject;

import pt.floraon.driver.Constants.NodeTypes;

public class SpeciesList extends GeneralDBNode {
	protected Float[] location={null,null};
	protected Integer year,month,day,precision,area;
	protected String pubNotes,privNotes,habitat;
	protected Boolean complete;		// did the observer identify all species he was able to?
	
	public Float[] getLocation() {
		return this.location;
	}
	public Integer getYear() {
		return this.year;
	}
	public Integer getMonth() {
		return this.month;
	}
	public Integer getDay() {
		return this.day;
	}
	public Integer getPrecision() {
		return this.precision;
	}
	public Integer getArea() {
		return this.area;
	}
	public String getPubNotes() {
		return this.pubNotes;
	}
	public String getPrivNotes() {
		return this.privNotes;
	}
	public String getHabitat() {
		return this.habitat;
	}
	public Boolean getComplete() {
		return this.complete;
	}
	
	public SpeciesList(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision,Integer area,String pubNotes,Boolean complete,String privNotes,String habitat) {
		this.location[0]=latitude;
		this.location[1]=longitude;
		this.year=year;
		this.month=month;
		this.day=day;
		this.precision=precision;
		this.area=area;
		this.pubNotes=pubNotes;
		this.complete=complete;
		this.privNotes=privNotes;
		this.habitat=habitat;
	}
	
	public SpeciesList(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision) {
		this(latitude, longitude, year, month, day, precision, null, null, null, null, null);
	}
	
	public SpeciesList(SpeciesList sl) {
		super(sl);
		this.location[0]=sl.location[0];
		this.location[1]=sl.location[1];
		this.year=sl.year;
		this.month=sl.month;
		this.day=sl.day;
		this.precision=sl.precision;
		this.area=sl.area;
		this.pubNotes=sl.pubNotes;
		this.complete=sl.complete;
	}
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.specieslist;
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
