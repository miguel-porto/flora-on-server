package pt.floraon.driver;

import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.HAS_QUALITY;
import pt.floraon.entities.SYNONYM;
import pt.floraon.entities.TaxEnt;

public abstract class GTaxEntWrapper extends BaseFloraOnDriver implements ITaxEntWrapper {
	protected INodeKey thisNode;
	private INodeWrapper NWD;
	public GTaxEntWrapper(FloraOn driver, INodeKey node) {
		super(driver);
		this.thisNode=node;
		NWD=(INodeWrapper) driver.wrapNode(node);
	}
	
	@Override
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException {
		return NWD.createRelationshipTo(parent, edge);
	}

	@Override
    public INodeKey createTaxEntChild(String name,String author,TaxonRanks rank,String sensu,String annotation,Boolean current) throws FloraOnException {
    	TaxEnt child=new TaxEnt(name, rank.getValue(), author, sensu, annotation, current, null, null);
    	child.canBeChildOf(driver.getNodeWorkerDriver().getTaxEntById(thisNode));
    	child=driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(child);
    	driver.wrapNode(driver.asNodeKey(child.getID())).setPART_OF(thisNode);
    	return driver.asNodeKey(child.getID());
    }
    
    @Override
	public void setSynonymOf(TaxEnt tev) throws FloraOnException {
		if(thisNode.equals(tev.getID())) throw new TaxonomyException("Cannot add a synonym of itself");
		// FIXME
		if(tev.getCurrent()) {
			driver.getNodeWorkerDriver().updateDocument(thisNode, "current", false);
			//node.update(null, null, null, null, false);
			//driver.graphUpdateVertex(Constants.TAXONOMICGRAPHNAME, NodeTypes.taxent.toString(), node.getID(), node, false);
		}
		driver.wrapNode(thisNode).createRelationshipTo(driver.asNodeKey(tev.getID()), new SYNONYM());
	}
	
	@Override
	public int setHAS_QUALITY(INodeKey parent) throws FloraOnException {
		return createRelationshipTo(parent, new HAS_QUALITY());
	}

}
