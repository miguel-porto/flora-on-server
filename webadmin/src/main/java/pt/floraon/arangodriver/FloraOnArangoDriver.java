package pt.floraon.arangodriver;

import static pt.floraon.driver.Constants.*;

import java.io.File;
import java.util.*;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;

import com.arangodb.model.*;
import jline.internal.Log;
import pt.floraon.arangodriver.serializers.*;
import pt.floraon.authentication.Privileges;
import pt.floraon.driver.*;
import pt.floraon.driver.datatypes.Rectangle;
import pt.floraon.driver.entities.GlobalSettings;
import pt.floraon.images.ImageManagementArangoDriver;
import pt.floraon.occurrences.Abundance;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.datatypes.SafeHTMLString;
import pt.floraon.driver.interfaces.*;
import pt.floraon.geometry.Precision;
import pt.floraon.occurrences.CSVFileProcessor;
import pt.floraon.occurrences.arangodb.OccurrenceArangoDriver;
import pt.floraon.occurrences.arangodb.OccurrenceReportArangoDriver;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.redlistdata.threats.*;
import pt.floraon.redlistdata.entities.RedListSettings;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.authentication.entities.User;
import pt.floraon.redlistdata.RedListDataArangoDBDriver;
import pt.floraon.authentication.RandomString;

public class FloraOnArangoDriver implements IFloraOn {
	private ArangoDB driver;
	private ArangoDatabase database;
	private final INodeWorker NWD;
	private final IQuery QD;
	private final IListDriver LD;
	private final CSVFileProcessor CSV;
	private final IRedListDataDriver RLD;
	private final IOccurrenceDriver OCD;
	private final IOccurrenceReportDriver OCRD;
    private final IAdministration ADMIN;
    private final IImageManagement IMG;
    private List<Territory> checklistTerritories;
    private Map<String, RedListSettings> redListSettings;
    private String errorMessage;
    private Properties properties;
    private final File imageFolder, thumbsFolder, originalImageFolder;
	private String contextPath, defaultINaturalistProject;
	private final MultipleChoiceEnumeration<Threat, ThreatCategory> threatEnumeration;
	private final MultipleChoiceEnumeration<ConservationAction, ConservationActionCategory> conservationActionEnumeration;

	/**
	 * Constructs a dummy driver object to hold error messages
	 * @param error
	 */
	public FloraOnArangoDriver(String error) {
		this.errorMessage = error;
        NWD = null;
        QD = null;
        LD = null;
        CSV = null;
        RLD = null;
        OCD = null;
        OCRD = null;
        ADMIN = null;
        IMG = null;
        imageFolder = null;
        thumbsFolder = null;
        originalImageFolder = null;
        contextPath = "";
        threatEnumeration = null;
		conservationActionEnumeration = null;
    }

	public FloraOnArangoDriver(Properties properties) throws FloraOnException {
		this.properties = properties;
		this.contextPath = properties.getProperty("contextPath", "");
		this.defaultINaturalistProject = properties.getProperty("defaultINaturalistProject", null);
		if(!this.contextPath.equals("") && !this.contextPath.startsWith("/")) this.contextPath = "/" + this.contextPath;
		String username = properties.getProperty("arango.user");
		String pass = properties.getProperty("arango.password");
		String dbname = properties.getProperty("arango.database");

		if(dbname == null)
			throw new FloraOnException("You must provide the database name in the floraon.properties file (arango.database)");

		if(username == null || pass == null)
			throw new FloraOnException("You must provide login details for ArangoDB in the floraon.properties file (arango.user and arango.password)");

		// register deserializers for enums that do not throw exceptions when value is not found
		driver = new ArangoDB.Builder().user(username).password(pass)
				.registerDeserializer(RedListEnums.DeclineDistribution.class, new SafeEnumDeserializer<>(RedListEnums.DeclineDistribution.class))
				.registerDeserializer(RedListEnums.PercentMatureOneSubpop.class, new SafeEnumDeserializer<>(RedListEnums.PercentMatureOneSubpop.class))
				.registerDeserializer(RedListEnums.AssessmentStatus.class, new SafeEnumDeserializer<>(RedListEnums.AssessmentStatus.class))
//				.registerDeserializer(RedListEnums.ProposedConservationActions.class, new SafeEnumDeserializer<>(RedListEnums.ProposedConservationActions.class))
				.registerDeserializer(RedListEnums.Uses.class, new SafeEnumDeserializer<>(RedListEnums.Uses.class))
				.registerDeserializer(RedListEnums.PopulationSizeReduction.class, new SafeEnumDeserializer<>(RedListEnums.PopulationSizeReduction.class))
				.registerDeserializer(Privileges.class, new SafeEnumDeserializer<>(Privileges.class))
				.registerDeserializer(RedListEnums.NrMatureIndividuals.class, new SafeEnumDeserializer<>(RedListEnums.NrMatureIndividuals.class))
				.registerDeserializer(RedListEnums.HasPhoto.class, new SafeEnumDeserializer<>(RedListEnums.HasPhoto.class, RedListEnums.HasPhoto.FALSE))
				.registerDeserializer(Precision.class, new PrecisionDeserializer())
				.registerSerializer(Precision.class, new PrecisionSerializer())
				.registerDeserializer(SafeHTMLString.class, new SafeHTMLStringDeserializer())
				.registerSerializer(SafeHTMLString.class, new SafeHTMLStringSerializer())
				.registerDeserializer(Rectangle.class, new RectangleDeserializer())
				.registerSerializer(Threat.class, new ThreatSerializer())
				.registerDeserializer(Threat.class, new ThreatDeserializer())
				.registerSerializer(ConservationAction.class, new ConservationActionSerializer())
				.registerDeserializer(ConservationAction.class, new ConservationActionDeserializer())
 /*
				.registerSerializer(Rectangle.class, new RectangleSerializer())
*/
				.registerDeserializer(IntegerInterval.class, new NumericIntervalDeserializer())
				.registerSerializer(IntegerInterval.class, new NumericIntervalSerializer())
				.registerDeserializer(Abundance.class, new AbundanceDeserializer())
				.registerSerializer(Abundance.class, new AbundanceSerializer())
				.registerSerializer(Float.class, new FloatNoDataSerializer())	// to handle no data values
				.build();

		database = driver.db(dbname);
		NWD = new NodeWorkerDriver(this);
		QD = new QueryDriver(this);
		LD = new ListDriver(this);
		CSV = new CSVFileProcessor(this);
		RLD = new RedListDataArangoDBDriver(this);
		OCD = new OccurrenceArangoDriver(this);
		OCRD = new OccurrenceReportArangoDriver(this);
		ADMIN = new Administration(this);
		IMG = new ImageManagementArangoDriver(this);

		// fetch the threat enumeration, that may depend on the taxonomic group
		String threatEnumerationClass = this.getProperties().getProperty("threatsEnumeration");
		if(threatEnumerationClass == null) threatEnumerationClass = "ThreatsPlants";
		try {
			threatEnumeration = (MultipleChoiceEnumerationThreats) Class.forName("pt.floraon.redlistdata.threats." + threatEnumerationClass).getDeclaredConstructor().newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new FloraOnException("Could not initialize Threats.");
		}

		try {
			conservationActionEnumeration = (MultipleChoiceEnumerationConservationActions) Class.forName("pt.floraon.redlistdata.threats.ConservationActionPlants").getDeclaredConstructor().newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new FloraOnException("Could not initialize Threats.");
		}

		// check or create image folders
        String folder;
        if((folder = this.getProperties().getProperty("imageFolder")) == null)
            throw new FloraOnException("Image folder is not defined.");

        imageFolder = new File(folder);
        if(!imageFolder.exists()) {
            if(!imageFolder.mkdir())
                throw new FloraOnException("Could not create image folder.");
        }

        thumbsFolder = new File(imageFolder, "thumbs");
        if(!thumbsFolder.exists()) {
            if(!thumbsFolder.mkdir())
                throw new FloraOnException("Could not create thumbs folder.");
        }

        originalImageFolder = new File(imageFolder, "originals");
        if(!originalImageFolder.exists()) {
            if(!originalImageFolder.mkdir())
                throw new FloraOnException("Could not create originals folder.");
        }

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

        // get user options for redlist
		reloadSettings();
	}

	@Override
	public void reloadSettings() {
		try {
			this.redListSettings = this.getRedListData().getRedListSettings(null);
		} catch (FloraOnException e) {
			e.printStackTrace();
		}
//		System.out.println(new Gson().toJson(this.redListSettings));
	}

	@Override
	public RedListSettings getRedListSettings(String territory) {
		RedListSettings out = this.redListSettings.get(territory);
		if (out == null) {
			out = new RedListSettings();
			out.setTerritory(territory);
		}
		return out;
	}

    @Override
    public GlobalSettings getGlobalSettings() {
	    if(database.collection(NodeTypes.global_settings.toString()) == null)
	        database.createCollection(NodeTypes.global_settings.toString());

        Iterator<GlobalSettings> it = database.query(String.format("FOR s IN %s RETURN s", NodeTypes.global_settings.toString()), null
                , null, GlobalSettings.class);
        if(it.hasNext())
            return it.next();
        else {
            DocumentCreateEntity<GlobalSettings> out
                    = database.collection(NodeTypes.global_settings.toString()).insertDocument(new GlobalSettings()
                    , new DocumentCreateOptions().returnNew(true));
            return out.getNew();
        }
    }

    @Override
    public void updateGlobalSettings(GlobalSettings newSettings) {
	    database.collection(NodeTypes.global_settings.toString()).updateDocument(newSettings.getKey(), newSettings);
    }

    @Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public boolean hasFailed() {
		return this.errorMessage != null;
	}

	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}

	@Override
	public File getImageFolder() {
		return imageFolder;
	}

    @Override
    public File getThumbsFolder() {
        return thumbsFolder;
    }

    @Override
    public File getOriginalImageFolder() {
        return originalImageFolder;
    }

	@Override
	public String getContextPath() {
		return this.contextPath;
	}

	@Override
	public String getDefaultINaturalistProject() {
		return this.defaultINaturalistProject;
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
	public IRedListDataDriver getRedListData() {
		return RLD;
	}

	@Override
	public IOccurrenceDriver getOccurrenceDriver() {
		return OCD;
	}

	@Override
	public String getDefaultRedListTerritory() {
		return "lu";	// TODO this must be a user setting
	}

	@Override
	public IOccurrenceReportDriver getOccurrenceReportDriver() {
		return OCRD;
	}

	@Override
	public IAdministration getAdministration() {
		return ADMIN;
	}

    @Override
    public IImageManagement getImageManagement() {
        return this.IMG;
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
	}

	/**
	 * This is where the database structure is created.
	 * @throws ArangoDBException
	 */
	private void checkCollections() throws ArangoDBException {
//		Map<String,CollectionEntity> collections=driver.getCollections().getNames();
		
		// create a collection for each nodetype
		for(NodeTypes nt:NodeTypes.values()) {
			if(!database.collection(nt.toString()).exists()) {
				System.out.println("Creating document collection: "+nt.toString());
				database.createCollection(nt.toString(), new CollectionCreateOptions().type(CollectionType.DOCUMENT));
				if(nt == NodeTypes.user) {	// create administrator account if creating collection of users
					try {
						User user = new User("admin", "Administrator", new Privileges[] {
								Privileges.MANAGE_REDLIST_USERS, Privileges.CREATE_REDLIST_DATASETS, Privileges.VIEW_FULL_SHEET,
								Privileges.EDIT_FULL_CHECKLIST, Privileges.MODIFY_TAXA, Privileges.MODIFY_TAXA_TERRITORIES});
						user.setUserType(User.UserType.ADMINISTRATOR.toString());
						char[] pass = new RandomString(12).nextString().toCharArray();
						user.setPassword(pass);
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
				System.out.println("Creating edge collection: "+nt.toString());
				database.createCollection(nt.toString(), new CollectionCreateOptions().type(CollectionType.EDGES));
			}
		}

		// create indexes

//		database.collection(NodeTypes.specieslist.toString()).createGeoIndex(Arrays.asList("location"), new GeoIndexOptions().geoJson(false));
		database.collection(NodeTypes.inventory.toString()).ensureGeoIndex(Arrays.asList("latitude", "longitude"), new GeoIndexOptions().geoJson(false));
		database.collection(NodeTypes.inventory.toString()).ensureSkiplistIndex(Arrays.asList("latitude", "longitude"), new SkiplistIndexOptions().sparse(false));
//		database.collection(NodeTypes.author.toString()).createHashIndex(Arrays.asList("idAut"), new HashIndexOptions().unique(true).sparse(false));
		database.collection(NodeTypes.taxent.toString()).ensureHashIndex(Collections.singleton("oldId"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).ensureHashIndex(Collections.singleton("rank"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).ensureHashIndex(Collections.singleton("isSpeciesOrInf"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.taxent.toString()).ensureHashIndex(Collections.singleton("name"), new HashIndexOptions().unique(false).sparse(true));
		database.collection(NodeTypes.taxent.toString()).ensureFulltextIndex(Collections.singleton("name"), new FulltextIndexOptions());
		database.collection(NodeTypes.territory.toString()).ensureHashIndex(Collections.singleton("shortName"), new HashIndexOptions().unique(true).sparse(false));
		database.collection(NodeTypes.user.toString()).ensureHashIndex(Collections.singleton("userName"), new HashIndexOptions().unique(true).sparse(true));
		database.collection(NodeTypes.toponym.toString()).ensureFulltextIndex(Collections.singleton("locality"), new FulltextIndexOptions().minLength(1));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("maintainer"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureSkiplistIndex(Arrays.asList("year", "month", "day"), new SkiplistIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("unmatchedOccurrences[*].confidence"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("unmatchedOccurrences[*].confidence"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("observers[*]"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("collectors[*]"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("dets[*]"), new HashIndexOptions().unique(false).sparse(false));
		database.collection(NodeTypes.inventory.toString()).ensurePersistentIndex(Collections.singleton("unmatchedOccurrences[*].uuid"), new PersistentIndexOptions().unique(true).sparse(true));	// sparse because there may be inventories without occurrences
//		database.collection(NodeTypes.inventory.toString()).ensureHashIndex(Collections.singleton("unmatchedOccurrences[*].uuid"), new HashIndexOptions().unique(true).sparse(true));	// sparse because there may be inventories without occurrences
		database.collection(NodeTypes.image.toString()).ensureHashIndex(Collections.singleton("uuid"), new HashIndexOptions().unique(true).sparse(false));
//		database.collection(NodeTypes.inventory.toString()).ensureSkiplistIndex(Arrays.asList("latitude", "longitude"), new SkiplistIndexOptions().unique(false).sparse(false));
//		database.collection(NodeTypes.inventory.toString()).ensureSkiplistIndex(Collections.singleton("month"), new SkiplistIndexOptions().unique(false).sparse(false));
//		database.collection(NodeTypes.inventory.toString()).ensureSkiplistIndex(Collections.singleton("day"), new SkiplistIndexOptions().unique(false).sparse(false));
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
		edgeDefinition.to(NodeTypes.inventory.toString());
		edgeDefinitions.add(edgeDefinition);

		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.OBSERVED_BY.toString());
		edgeDefinition.from(NodeTypes.inventory.toString());
		edgeDefinition.to(NodeTypes.user.toString());
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

		// habitat <- habitat
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.TYPE_OF.toString());
		edgeDefinition.from(NodeTypes.habitat.toString());
		edgeDefinition.to(NodeTypes.habitat.toString());
		edgeDefinitions.add(edgeDefinition);

		// habitat <-> habitat
		edgeDefinition = new EdgeDefinition();
		edgeDefinition.collection(RelTypes.SAME_AS.toString());
		edgeDefinition.from(NodeTypes.habitat.toString());
		edgeDefinition.to(NodeTypes.habitat.toString());
		edgeDefinitions.add(edgeDefinition);

		database.createGraph(Constants.TAXONOMICGRAPHNAME, edgeDefinitions, null);
	}

	@Override
	public MultipleChoiceEnumeration<Threat, ThreatCategory> getThreatEnum() {
		return this.threatEnumeration;
	}

	@Override
	public MultipleChoiceEnumeration<ConservationAction, ConservationActionCategory> getConservationActionEnum() {
		return this.conservationActionEnumeration;
	}

}
