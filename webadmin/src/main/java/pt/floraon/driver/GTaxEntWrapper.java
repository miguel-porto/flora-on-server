package pt.floraon.driver;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.HAS_QUALITY;
import pt.floraon.entities.SYNONYM;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.TaxEntAndNativeStatusResult;
import pt.floraon.results.ListOfTerritoryStatus.InferredStatus;

public abstract class GTaxEntWrapper extends BaseFloraOnDriver implements ITaxEntWrapper {
	protected INodeKey thisNode;
	private INodeWrapper NWrD;
	public GTaxEntWrapper(FloraOn driver, INodeKey node) throws FloraOnException {
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

	@Override
	public String[] getEndemismDegree() throws FloraOnException {
		TaxEntAndNativeStatusResult listOfStatus = this.getNativeStatusList(null);
		Set<String> tmp = listOfStatus.inferEndemismDegree();
		String[] out = tmp.toArray(new String[tmp.size()]);
		return out;
	}

	@Override
	public Map<String,InferredStatus> getInferredNativeStatus(String territory) throws FloraOnException {
		TaxEntAndNativeStatusResult listOfStatus = this.getNativeStatusList(territory);
		return listOfStatus.inferNativeStatus(territory);		
	}

	@Override
	public Map<String,Set<Territory>> getRestrictedTo(List<String> territory) throws FloraOnException {
		TaxEntAndNativeStatusResult listOfStatus = this.getNativeStatusList(null);
		Set<String> terr = null;

		if(territory != null) {
			terr = new HashSet<String>();
			terr.addAll(territory);
		}
		return listOfStatus.inferRestrictedTo(terr);		
	}
	
	@Override
	public void getSingleTerritoryEndemism() throws FloraOnException {
		TaxEntAndNativeStatusResult listOfStatus = this.getNativeStatusList(null);
		listOfStatus.inferSingleTerritoryEndemismDegree();
	}

}
