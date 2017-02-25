package pt.floraon.arangodriver;

import static pt.floraon.driver.Constants.*;

import java.util.*;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.UserEntity;

import com.arangodb.model.*;
import jline.internal.Log;
import pt.floraon.authentication.Privileges;
import pt.floraon.driver.*;
import pt.floraon.occurrences.CSVFileProcessor;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.authentication.entities.User;
import pt.floraon.redlistdata.ArangoDBRedListData;
import pt.floraon.authentication.RandomString;

public class FloraOnArangoDriver implements IFloraOn {
	private ArangoDB driver;
	private ArangoDatabase database;
	private INodeWorker NWD;
	private IQuery QD;
	private IListDriver LD;
	private CSVFileProcessor CSV;
	private IRedListData RLD;
	private IAdministration ADMIN;
	private List<Territory> checklistTerritories;
	
	public FloraOnArangoDriver(String dbname, Properties properties) throws FloraOnException {
		String username = properties.getProperty("arango.user");
		String pass = properties.getProperty("arango.password");

		if(username == null || pass == null)
			throw new FloraOnException("You must provide login details for ArangoDB in the floraon.properties file (arango.user and arango.password)");

/*
		VPackDeserializer<RedListEnums.DeclinePopulation> deserializer = new VPackDeserializer<RedListEnums.DeclinePopulation>() {
			@Override
			public RedListEnums.DeclinePopulation deserialize(VPackSlice parent, VPackSlice vpack, VPackDeserializationContext context)
					throws VPackException {
				try {
					return RedListEnums.DeclinePopulation.valueOf(vpack.getAsString());
				} catch (IllegalArgumentException e) {
					Log.warn("Value " + vpack.getAsString() + " not found in enum constant.");
					return null;
				}
			}
		};
*/

		// register deserializers for enums that do not throw exceptions when value is not found
		driver = new ArangoDB.Builder().user(username).password(pass)
				.registerDeserializer(RedListEnums.DeclineDistribution.class, new SafeEnumDeserializer<>(RedListEnums.DeclineDistribution.class))
				.registerDeserializer(RedListEnums.PercentMatureOneSubpop.class, new SafeEnumDeserializer<>(RedListEnums.PercentMatureOneSubpop.class))
				.registerDeserializer(RedListEnums.AssessmentStatus.class, new SafeEnumDeserializer<>(RedListEnums.AssessmentStatus.class))
				.registerDeserializer(RedListEnums.ProposedConservationActions.class, new SafeEnumDeserializer<>(RedListEnums.ProposedConservationActions.class))
				.registerDeserializer(RedListEnums.Uses.class, new SafeEnumDeserializer<>(RedListEnums.Uses.class))
				.registerDeserializer(RedListEnums.PopulationSizeReduction.class, new SafeEnumDeserializer<>(RedListEnums.PopulationSizeReduction.class))
				.registerDeserializer(Privileges.class, new SafeEnumDeserializer<>(Privileges.class))
				.registerDeserializer(RedListEnums.NrMatureIndividuals.class, new SafeEnumDeserializer<>(RedListEnums.NrMatureIndividuals.class))
				.build();

		database = driver.db(dbname);
		NWD = new NodeWorkerDriver(this);
		QD = new QueryDriver(this);
		LD = new ListDriver(this);
		CSV = new CSVFileProcessor(this);
		RLD = new ArangoDBRedListData(this);
		ADMIN = new Administration(this);

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
	public IAdministration getAdministration() {
		return ADMIN;
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
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("oldId"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("rank"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("isSpeciesOrInf"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.taxent.toString()).createHashIndex(Arrays.asList("name"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).createFulltextIndex(Arrays.asList("name"), new FulltextIndexOptions());
		database.collection(NodeTypes.territory.toString()).createHashIndex(Arrays.asList("shortName"), new HashIndexOptions().unique(true).sparse(false));
		database.collection(NodeTypes.user.toString()).createHashIndex(Arrays.asList("userName"), new HashIndexOptions().unique(true).sparse(false));

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
				if(nt == NodeTypes.user) {	// create administrator account
					try {
						User user = new User("admin", "Administrator", new Privileges[] {
								Privileges.MANAGE_REDLIST_USERS});
						user.setUserType(User.UserType.ADMINISTRATOR.toString());
						char[] pass = new RandomString(12).nextString().toCharArray();
						user.setPassword(pass);
						System.out.println("Flora-On admin password: " + new String(pass));
						Log.info("Flora-On admin password: " + new String(pass));
						ADMIN.createUser(user);
					} catch (FloraOnException e1) {
						e1.printStackTrace();
					}
				}
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
