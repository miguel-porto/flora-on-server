package pt.floraon.entities;

import org.apache.commons.csv.CSVRecord;

import com.arangodb.ArangoException;
import com.arangodb.entity.marker.VertexEntity;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.Constants.NodeTypes;

public class Image extends GeneralNodeWrapper {
	public ImageVertex baseNode;
	private Integer idEnt,idOrg,idAut;
// TODO images!
	public Image(FloraOnDriver graph,CSVRecord record) {
		String tmp;
		baseNode.guid=record.get(0);
		baseNode.fileName=record.get(1);
		baseNode.comment=record.get(6);
		baseNode.width=Integer.parseInt(record.get(7));
		baseNode.height=Integer.parseInt(record.get(8));
		baseNode.x1=Integer.parseInt(record.get(9));
		baseNode.y1=Integer.parseInt(record.get(10));
		baseNode.x2=Integer.parseInt(record.get(11));
		baseNode.y2=Integer.parseInt(record.get(12));
		baseNode.defaultImage=(tmp=record.get(4))==null ? null : (Integer.parseInt(tmp)==0 ? false : true);
		idEnt=Integer.parseInt(record.get(2));
		idOrg=Integer.parseInt(record.get(3));
		idAut=Integer.parseInt(record.get(5));
		dirty=true;
		this.graph=graph;
	}
	@Override
	void commit() throws FloraOnException, ArangoException {
		if(!this.dirty) return;
		if(baseNode._id==null) {	// it was created from CSV record
			Author aut=graph.dbNodeWorker.getAuthorById(idAut);
			TaxEnt te=graph.dbNodeWorker.getTaxEntById(idEnt);
			VertexEntity<ImageVertex> vertexEntity=graph.driver.graphCreateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.image.toString(), this.baseNode, false);
		} else {
			// TODO Auto-generated method stub
		}
	}
	
}
