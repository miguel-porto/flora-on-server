package pt.floraon.arangodriver;

import static pt.floraon.driver.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.UserEntity;

import com.arangodb.model.*;
import pt.floraon.driver.*;
import pt.floraon.entities.Territory;
import pt.floraon.redlisttaxoninfo.ArangoDBRedListData;

public class FloraOnArangoDriver implements IFloraOn {
	private ArangoDB driver;
	private ArangoDatabase database;
	private INodeWorker NWD;
	private IQuery QD;
	private IListDriver LD;
	private CSVFileProcessor CSV;
	private IRedListData RLD;
	private List<Territory> checklistTerritories;
	
	public FloraOnArangoDriver(String dbname, Properties properties) throws FloraOnException {
		String username = properties.getProperty("arango.user");
		String pass = properties.getProperty("arango.password");

		if(username == null || pass == null)
			throw new FloraOnException("Youi must provide login details for ArangoDB in the floraon.properties file (arango.user and arango.password)");

		driver = new ArangoDB.Builder().user(username).password(pass).build();
		database = driver.db(dbname);
/*
		ArangoConfigure configure = new ArangoConfigure();
        configure.init();
        configure.setDefaultDatabase(dbname);
        configure.setUser(username);
        configure.setPassword(pass);
        driver = new ArangoDriver(configure, dbname);
*/
        try {
			Collection<String> dbs=driver.getDatabases();	// TODO: this needs permissions in the _system database...
			if(!dbs.contains(dbname))
				initializeNewDatabase(dbname);
			else {
				checkCollections();
				try {
					database.graph(Constants.TAXONOMICGRAPHNAME).getInfo();
				} catch (ArangoDBException e) {
					createTaxonomicGraph();
				}
			}
		} catch (ArangoDBException e) {
			e.printStackTrace();
			throw new FloraOnException(e.getMessage());
		}
        NWD = new NodeWorkerDriver(this);
        QD = new QueryDriver(this);
        LD = new ListDriver(this);
        CSV = new CSVFileProcessor(this);
		RLD = new ArangoDBRedListData(this);
        updateVariables();
	}

	@Override
	public Object getDatabaseDriver() {
		return driver;
	}

	@Override
	public Object getDatabase() {
		return database;
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
	public IRedListData getRedListData() {
		return RLD;
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
		return id == null ? null : new ArangoKey(id);
	}
	
	private synchronized void updateVariables() throws FloraOnException {
		Iterator<Territory> it=this.LD.getChecklistTerritories().iterator();
		checklistTerritories= new ArrayList<>();
		while(it.hasNext()) {
			checklistTerritories.add(it.next());
		}
	}
	
	/**
	 * Initializes a new database from scratch. Creates collections, graphs, etc.
	 * @param dbname
	 * @throws ArangoDBException
	 */
	private void initializeNewDatabase(String dbname) throws ArangoDBException {
		System.out.println("Initializing a fresh new database");
		/*				UserEntity ue;
		ue=new UserEntity();*/
		UserEntity[] ue=new UserEntity[0];
		driver.createDatabase(dbname);
		database = driver.db(dbname);

		checkCollections();

		createTaxonomicGraph();

		database.collection(NodeTypes.specieslist.toString()).createGeoIndex(Arrays.asList("location"), new GeoIndexOptions().geoJson(false));
		database.collection(NodeTypes.author.toString()).createHashIndex(Arrays.asList("idAut"), new HashIndexOptions().unique(true).sparse(false));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("oldId"), new HashIndexOptions().unique(true).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("rank"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("isSpeciesOrInf"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("name"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createFulltextIndex(Arrays.asList("name"), new FulltextIndexOptions());
		database.collection(NodeTypes.territory.toString()).createHashIndex(Arrays.asList("shortName"), new HashIndexOptions().unique(true).sparse(false));

/*
		driver.createGeoIndex(NodeTypes.specieslist.toString(), false, "location");
		driver.createHashIndex("author", true, "idAut");
		driver.createHashIndex("taxent", true, true, "oldId");
		driver.createHashIndex("taxent", false, true, "rank");
		driver.createHashIndex("taxent", false, false, "isSpeciesOrInf");
		driver.createHashIndex("territory", true, "shortName");
		driver.createHashIndex("taxent", false, true, "name");
		driver.createFulltextIndex("taxent", "name");
*/
	}
	
	private void checkCollections() throws ArangoDBException {
//		Map<String,CollectionEntity> collections=driver.getCollections().getNames();
		
		// create a collection for each nodetype
		for(NodeTypes nt:NodeTypes.values()) {
			try {
				database.collection(nt.toString()).getInfo();
			} catch (ArangoDBException e) {
				System.out.println("Creating collection: "+nt.toString());
				database.createCollection(nt.toString(), new CollectionCreateOptions().type(CollectionType.DOCUMENT));
			}
		}
		
		for(RelTypes nt:RelTypes.values()) {
			try {
				database.collection(nt.toString()).getInfo();
			} catch (ArangoDBException e) {
				System.out.println("Creating collection: "+nt.toString());
				database.createCollection(nt.toString(), new CollectionCreateOptions().type(CollectionType.EDGES));
			}
		}
	}
	
	private void createTaxonomicGraph() throws ArangoDBException {
		Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();

		// taxonomic relations
		EdgeDefinition edgeDefinition = new EdgeDefinition();
		// define the edgeCollection to store the edges
		edgeDefinition.collection(RelTypes.PART_OF.toString());
		// define a set of collections where an edge is going out...
		edgeDefinition.from(NodeTypes.taxent.toString());
		 // repeat this for the collections where an edge is going into
		edgeDefinition.to(NodeTypes.taxent.toString());
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.HYBRID_OF.toString());
		edgeDefinition.from(NodeTypes.taxent.toString());
		edgeDefinition.to(NodeTypes.taxent.toString());
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.SYNONYM.toString());
		edgeDefinition.from(NodeTypes.taxent.toString());
		edgeDefinition.to(NodeTypes.taxent.toString());
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.BELONGS_TO.toString());
		edgeDefinition.from(NodeTypes.territory.toString());
		edgeDefinition.to(NodeTypes.territory.toString());
		edgeDefinitions.add(edgeDefinition);

		// species list subgraph
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.OBSERVED_IN.toString());
		edgeDefinition.from(NodeTypes.taxent.toString());
		edgeDefinition.to(NodeTypes.specieslist.toString());
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.OBSERVED_BY.toString());
		edgeDefinition.from(NodeTypes.specieslist.toString());
		edgeDefinition.to(NodeTypes.author.toString());
		edgeDefinitions.add(edgeDefinition);

		// attributes <- taxent
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.HAS_QUALITY.toString());
		edgeDefinition.from(NodeTypes.taxent.toString());
		edgeDefinition.to(NodeTypes.attribute.toString());
		edgeDefinitions.add(edgeDefinition);

		// characters <- attributes
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.ATTRIBUTE_OF.toString());
		edgeDefinition.from(NodeTypes.attribute.toString());
		edgeDefinition.to(NodeTypes.character.toString());
		edgeDefinitions.add(edgeDefinition);

		// territory <- taxent
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.EXISTS_IN.toString());
		edgeDefinition.from(NodeTypes.taxent.toString());
		edgeDefinition.to(NodeTypes.territory.toString());
		edgeDefinitions.add(edgeDefinition);

		database.createGraph(Constants.TAXONOMICGRAPHNAME, edgeDefinitions, null);
	}

}
