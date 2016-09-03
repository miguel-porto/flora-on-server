package pt.floraon.arangodriver;

import static pt.floraon.driver.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionEntity;
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
	
	public FloraOnArangoDriver(String dbname, String basedir) throws FloraOnException {
		File account=new File(basedir+"/arangodb_login.txt");
		if(!account.canRead()) throw new FloraOnException("Cannot connect to ArangoDB server without a user account in a file named arangodb_login.txt located in the folder "+basedir);
		BufferedReader fr;
		String username, pass;
		try {
			fr = new BufferedReader(new FileReader(account));
			username=fr.readLine();
			pass=fr.readLine();
			fr.close();
		} catch (IOException e1) {
			throw new FloraOnException("Cannot connect to ArangoDB server without a user account in a file named arangodb_login.txt locate in the root folder of the webapps.");
		}

		ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase(dbname);
        configure.setUser(username);
        configure.setPassword(pass);
        driver = new ArangoDriver(configure, dbname);

        try {
			StringsResultEntity dbs=driver.getDatabases(username, pass);
			if(!dbs.getResult().contains(dbname))
				initializeNewGraph(dbname);
			else {
				checkCollections(dbname);
				if(!driver.getGraphList().contains(Constants.TAXONOMICGRAPHNAME))
					createTaxonomicGraph();
			}
		} catch (ArangoException e) {
			e.printStackTrace();
			throw new FloraOnException(e.getMessage());
		}

        NWD=new NodeWorkerDriver(this);
        QD=new QueryDriver(this);
        LD=new ListDriver(this);
        CSV=new CSVFileProcessor(this);
        updateVariables();
	}

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
	public INodeWrapper wrapNode(INodeKey node) throws FloraOnException {
		return new NodeWrapperDriver(this, node);
	}

	@Override
	public ITaxEntWrapper wrapTaxEnt(INodeKey node) throws FloraOnException {
		return new TaxEntWrapperDriver(this, node);
	}
	
	@Override
	public ISpeciesListWrapper wrapSpeciesList(INodeKey node) throws FloraOnException {
		return new SpeciesListWrapperDriver(this, node);
	}
	
	@Override
	public IAttributeWrapper wrapAttribute(INodeKey node) throws FloraOnException {
		return new GAttributeWrapper(this, node);
	}
	
	@Override
	public INodeKey asNodeKey(String id) throws FloraOnException {
		return id==null ? null : new ArangoKey(id);
	}
	
	public synchronized void updateVariables() throws FloraOnException {
		Iterator<Territory> it=this.LD.getChecklistTerritories().iterator();
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
		
		checkCollections(dbname);

		createTaxonomicGraph();
		
		driver.createGeoIndex(NodeTypes.specieslist.toString(), false, "location");
		driver.createHashIndex("author", true, "idAut");
		driver.createHashIndex("taxent", true, true, "oldId");
		driver.createHashIndex("taxent", false, true, "rank");
		driver.createHashIndex("taxent", false, false, "isSpeciesOrInf");
		driver.createHashIndex("territory", true, "shortName");
		driver.createHashIndex("taxent", false, true, "name");
		driver.createFulltextIndex("taxent", "name");
	}
	
	private void checkCollections(String dbname) throws ArangoException {
		Map<String,CollectionEntity> collections=driver.getCollections().getNames();
		
		// create a collection for each nodetype
		for(NodeTypes nt:NodeTypes.values()) {
			//if(driver.getCollection(nt.toString()).getCount() == 0) {
			if(!collections.containsKey(nt.toString())) {
				System.out.println("Creating collection: "+nt.toString());
				driver.createCollection(nt.toString());
			}
		}
		
		CollectionOptions co=new CollectionOptions();
		co.setType(CollectionType.EDGE);
		for(RelTypes nt:RelTypes.values()) {
			//if(driver.getCollection(nt.toString()).getCount() == 0) {
			if(!collections.containsKey(nt.toString())) {
				System.out.println("Creating collection: "+nt.toString());
				driver.createCollection(nt.toString(),co);
			}
		}
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
		edgeDefinition.setFrom(from);
		 // repeat this for the collections where an edge is going into  
		List<String> to = new ArrayList<String>();
		to.add(NodeTypes.taxent.toString());
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

		edgeDefinition = new EdgeDefinitionEntity();
		edgeDefinition.setCollection(RelTypes.BELONGS_TO.toString());
		from = new ArrayList<String>();
		from.add(NodeTypes.territory.toString());
		edgeDefinition.setFrom(from);
		to = new ArrayList<String>();
		to.add(NodeTypes.territory.toString());
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
