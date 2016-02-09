package pt.floraon.arangodriver;

import static pt.floraon.driver.Constants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.UserEntity;

import pt.floraon.driver.IAttributeWrapper;
import pt.floraon.driver.CSVFileProcessor;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.GAttributeWrapper;
import pt.floraon.driver.FloraOn;
import pt.floraon.driver.IListDriver;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.INodeWorker;
import pt.floraon.driver.INodeWrapper;
import pt.floraon.driver.IQuery;
import pt.floraon.driver.ISpeciesListWrapper;
import pt.floraon.driver.ITaxEntWrapper;
import pt.floraon.entities.Territory;

public class FloraOnArangoDriver implements FloraOn {
	private ArangoDriver driver;
	private INodeWorker NWD;
	private IQuery QD;
	private IListDriver LD;
	private CSVFileProcessor CSV;
	private List<Territory> checklistTerritories;
	
	@Override
	public Object getArangoDriver() {
		return driver;
	}
	
	@Override
	public INodeWorker getNodeWorkerDriver() {
		return NWD;
	}
	
	@Override
	public IQuery getQueryDriver() {
		return QD;
	}

	@Override
	public IListDriver getListDriver() {
		return LD;
	}
	
	@Override
	public CSVFileProcessor getCSVFileProcessor() {
		return CSV;
	}
	
	@Override
	public List<Territory> getChecklistTerritories() {
		return this.checklistTerritories;
	}
	
	@Override
	public INodeWrapper wrapNode(INodeKey node) {
		return new NodeWrapperDriver(this, node);
	}

	@Override
	public ITaxEntWrapper wrapTaxEnt(INodeKey node) {
		return new TaxEntWrapperDriver(this, node);
	}
	
	@Override
	public ISpeciesListWrapper wrapSpeciesList(INodeKey node) {
		return new SpeciesListWrapperDriver(this, node);
	}
	
	@Override
	public IAttributeWrapper wrapAttribute(INodeKey node) {
		return new GAttributeWrapper(this, node);
	}
	
	@Override
	public INodeKey asNodeKey(String id) throws FloraOnException {
		return id==null ? null : new ArangoKey(id);
	}
	
	public FloraOnArangoDriver(String dbname) throws FloraOnException {
        ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase(dbname);
        driver = new ArangoDriver(configure);

        try {
			StringsResultEntity dbs=driver.getDatabases();
			if(!dbs.getResult().contains(dbname))
				initializeNewGraph(dbname);
			else
				driver.setDefaultDatabase(dbname);        
		} catch (ArangoException e) {
			System.err.println("ERROR initializing the graph: "+e.getMessage());
			e.printStackTrace();
			throw new FloraOnException(e.getMessage());
		}
        
        NWD=new NodeWorkerDriver(this);
        QD=new QueryDriver(this);
        LD=new ListDriver(this);
        CSV=new CSVFileProcessor(this);
        updateVariables();

        //TEWrF=new TaxEntWrapperFactory();
        //NWrF=new NodeWrapperFactory();
		//sc.setAttribute("NodeWrapperFactory", NWrF);
		//sc.setAttribute("TaxEntWrapperFactory", TEWrF);
	}
	/*
	public FloraOnDriver(String dbname) throws ArangoException {
        ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase("flora");
        this.dbGeneralQueries=new FloraOnDriver.GeneralQueries();
        this.dbNodeWorker=new FloraOnDriver.NodeWorker();
        this.dbDataUploader=new FloraOnDriver.DataUploader();
        this.dbSpecificQueries=new FloraOnDriver.SpecificQueries();
        
        driver = new ArangoDriver(configure);
/*
        driver.createAqlFunction("flora::hybridVisitor", "function (config, result, vertex, path, connections) {"
    		+ "result.push({vertex});"
    		+ "}");*/
        /*
        driver.createAqlFunction("flora::testCode", "function (config, vertex, path) {"
    		+ "if(!vertex.name) return ['exclude','prune'];"
    		+ "}");//
                
        try {
			StringsResultEntity dbs=driver.getDatabases();
			if(!dbs.getResult().contains(dbname))
				initializeNewGraph(dbname);
			else
				driver.setDefaultDatabase(dbname);        
		} catch (ArangoException e) {
			System.err.println("ERROR initializing the graph: "+e.getMessage());
			e.printStackTrace();
			throw new ArangoException(e.getMessage());
		}
        
        updateVariables();
	}*/
	
	public synchronized void updateVariables() throws FloraOnException {
		Iterator<Territory> it=this.LD.getChecklistTerritories();
		checklistTerritories=new ArrayList<Territory>();
		while(it.hasNext()) {
			checklistTerritories.add(it.next());
		}
	}
	
	/**
	 * Initializes a new database from scratch. Creates collections, graphs, etc.
	 * @param dbname
	 * @throws ArangoException
	 */
	private void initializeNewGraph(String dbname) throws ArangoException {
		System.out.println("Initializing a fresh new database");
		/*				UserEntity ue;
		ue=new UserEntity();*/
		UserEntity[] ue=new UserEntity[0];
		driver.createDatabase(dbname, ue);
		driver.setDefaultDatabase(dbname);
		
		// create a collection for each nodetype
		for(NodeTypes nt:NodeTypes.values()) {
			driver.createCollection(nt.toString());
		}
		
		CollectionOptions co=new CollectionOptions();
		co.setType(CollectionType.EDGE);
		for(RelTypes nt:RelTypes.values()) {
			driver.createCollection(nt.toString(),co);
		}

		createTaxonomicGraph();
		//createNativeStatusGraph();
		
		driver.createGeoIndex(NodeTypes.specieslist.toString(), false, "location");
		driver.createHashIndex("author", true, "idAut");
		driver.createHashIndex("taxent", true, true, "oldId");
		driver.createHashIndex("taxent", false, true, "rank");
		driver.createHashIndex("taxent", false, false, "isSpeciesOrInf");
		driver.createHashIndex("territory", true, "shortName");
		driver.createFulltextIndex("taxent", "name");
	}
	
	private void createTaxonomicGraph() throws ArangoException {
		List<EdgeDefinitionEntity> edgeDefinitions = new ArrayList<EdgeDefinitionEntity>();

		// taxonomic relations
		EdgeDefinitionEntity edgeDefinition = new EdgeDefinitionEntity();
		// define the edgeCollection to store the edges
		edgeDefinition.setCollection(RelTypes.PART_OF.toString());
		// define a set of collections where an edge is going out...
		List<String> from = new ArrayList<String>();
		// and add one or more collections
		from.add(NodeTypes.taxent.toString());
		from.add(NodeTypes.territory.toString());
		edgeDefinition.setFrom(from);
		 // repeat this for the collections where an edge is going into  
		List<String> to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		from.add(NodeTypes.territory.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.HYBRID_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.SYNONYM.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// species list subgraph
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.OBSERVED_IN.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.specieslist.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.OBSERVED_BY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.specieslist.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.author.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// attributes <- taxent
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.HAS_QUALITY.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.attribute.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// characters <- attributes
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.ATTRIBUTE_OF.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.attribute.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.character.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		// territory <- taxent
		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.EXISTS_IN.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.taxent.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.territory.toString());
		edgeDefinition.setTo(to);
		edgeDefinitions.add(edgeDefinition);

		driver.createGraph(Constants.TAXONOMICGRAPHNAME, edgeDefinitions, null, true);
	}

}
