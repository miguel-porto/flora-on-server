package pt.floraon.driver;

import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.HAS_QUALITY;
import pt.floraon.entities.SYNONYM;
import pt.floraon.entities.TaxEnt;

public abstract class GTaxEntWrapper extends BaseFloraOnDriver implements ITaxEntWrapper {
	protected INodeKey thisNode;
	private INodeWrapper NWrD;
	public GTaxEntWrapper(FloraOn driver, INodeKey node) {
		super(driver);
		this.thisNode=node;
		NWrD=(INodeWrapper) driver.wrapNode(node);
	}
	
	@Override
	public int createRelationshipTo(INodeKey parent, GeneralDBEdge edge) throws FloraOnException {
		return NWrD.createRelationshipTo(parent, edge);
	}

	@Override
    public INodeKey createTaxEntChild(String name,String author,TaxonRanks rank,String sensu,String annotation,Boolean current) throws FloraOnException {
    	TaxEnt child=new TaxEnt(name, rank.getValue(), author, sensu, annotation, current, null, null, null);
    	child.canBeChildOf(driver.getNodeWorkerDriver().getTaxEntById(thisNode));
    	child=driver.getNodeWorkerDriver().createTaxEntFromTaxEnt(child);
    	driver.wrapNode(driver.asNodeKey(child.getID())).setPART_OF(thisNode);
    	return driver.asNodeKey(child.getID());
    }
    
    @Override
	public void setSynonymOf(INodeKey tev) throws FloraOnException {
		if(thisNode.getID().equals(tev.getID())) throw new TaxonomyException("Cannot add a synonym of itself");
		// set target node to not current
		driver.getNodeWorkerDriver().updateDocument(thisNode, "current", false);
		// create SYNONYM relationship
		driver.wrapNode(thisNode).createRelationshipTo(tev, new SYNONYM());
	}
	
	@Override
	public int setHAS_QUALITY(INodeKey parent) throws FloraOnException {
		return createRelationshipTo(parent, new HAS_QUALITY());
	}

}
