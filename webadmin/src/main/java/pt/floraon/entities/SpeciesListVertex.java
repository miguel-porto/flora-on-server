package pt.floraon.entities;

public class SpeciesListVertex extends GeneralDBNode {
	protected Float[] location={null,null};
	protected Integer year,month,day,precision,area;
	protected String pubNotes,privNotes,habitat;
	protected Boolean complete;		// did the observer identify all species he was able to?
	
	public SpeciesListVertex(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision) {
		this.location[0]=latitude;
		this.location[1]=longitude;
		this.year=year;
		this.month=month;
		this.day=day;
		this.precision=precision;
	}
	
	public SpeciesListVertex(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision,Integer area,String pubNotes,Boolean complete,String privNotes,String habitat) {
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
	
	public SpeciesListVertex(SpeciesList sl) {
		super(sl.baseNode);
		this.location[0]=sl.baseNode.location[0];
		this.location[1]=sl.baseNode.location[1];
		this.year=sl.baseNode.year;
		this.month=sl.baseNode.month;
		this.day=sl.baseNode.day;
		this.precision=sl.baseNode.precision;
		this.area=sl.baseNode.area;
		this.pubNotes=sl.baseNode.pubNotes;
		this.complete=sl.baseNode.complete;
	}

	public SpeciesListVertex(SpeciesListVertex sl) {
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
}
