package pt.floraon.entities;

public class SpeciesListVertex extends GeneralDBNode {
	protected Float[] location={null,null};
	protected Integer year,month,day,precision,area;
	protected String comment;
	protected Boolean complete;		// did the observer identify all species he was able to?
	
	public SpeciesListVertex(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision) {
		this.location[0]=latitude;
		this.location[1]=longitude;
		this.year=year;
		this.month=month;
		this.day=day;
		this.precision=precision;
	}
	
	public SpeciesListVertex(Float latitude,Float longitude,Integer year,Integer month,Integer day,Integer precision,Integer area,String comment,Boolean complete) {
		this.location[0]=latitude;
		this.location[1]=longitude;
		this.year=year;
		this.month=month;
		this.day=day;
		this.precision=precision;
		this.area=area;
		this.comment=comment;
		this.complete=complete;
	}
	
	public SpeciesListVertex(SpeciesList sl) {
		super(sl);
		this.location[0]=sl.location[0];
		this.location[1]=sl.location[1];
		this.year=sl.year;
		this.month=sl.month;
		this.day=sl.day;
		this.precision=sl.precision;
		this.area=sl.area;
		this.comment=sl.comment;
		this.complete=sl.complete;
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
		this.comment=sl.comment;
		this.complete=sl.complete;
	}
}
