package pt.floraon.driver.entities;

import org.apache.commons.csv.CSVRecord;

import com.google.gson.JsonObject;

import pt.floraon.arangodriver.FloraOnArangoDriver;
import pt.floraon.driver.Constants.NodeTypes;

public class Image extends GeneralDBNode {
	protected String fileName,guid,comment;
	protected Integer width,height,x1,y1,x2,y2;
	protected Boolean defaultImage;
	
	private Integer idEnt,idOrg,idAut;
	
	public Image(FloraOnArangoDriver graph,CSVRecord record) {
		String tmp;
		guid=record.get(0);
		fileName=record.get(1);
		comment=record.get(6);
		width=Integer.parseInt(record.get(7));
		height=Integer.parseInt(record.get(8));
		x1=Integer.parseInt(record.get(9));
		y1=Integer.parseInt(record.get(10));
		x2=Integer.parseInt(record.get(11));
		y2=Integer.parseInt(record.get(12));
		defaultImage=(tmp=record.get(4))==null ? null : (Integer.parseInt(tmp)==0 ? false : true);
		idEnt=Integer.parseInt(record.get(2));
		idOrg=Integer.parseInt(record.get(3));
		idAut=Integer.parseInt(record.get(5));
	}
	
	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}
	
	@Override
	public NodeTypes getType() {
		return NodeTypes.image;
	}

}
