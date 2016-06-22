package pt.floraon.driver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Objects;

import pt.floraon.entities.GeneralDBEdge;
import pt.floraon.entities.PART_OF;

public final class Constants {
	public static String TAXONOMICGRAPHNAME="taxgraph";

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_CYANBOLD = "\u001B[36;1m";
	public static final String ANSI_GREENBOLD = "\u001B[32;1m";
	
	public enum StringMatchTypes {
		EXACT,PREFIX,PARTIAL		// NOTE: do not change the order here!
	}
	
	public enum TaxonRanks {
		NORANK("non-taxonomic rank",1000),CLASS("Class",50),FAMILY("Family",100),FORM("Form",240),GENUS("Genus",140),KINGDOM("Kingdom",10)
    	,ORDER("Order",80),PHYLUM("Phylum (Division)",30),SECTION("Section",160),SERIES("Series",180),SPECIES("Species",200),SUBCLASS("Subclass",60)
    	,SUBGENUS("Subgenus",150),SUBFAMILY("Subfamily",110),SUBFORM("Subform",250),SUBKINGDOM("Subkingdom",20),SUBORDER("Suborder",90)
    	,SUBPHYLUM("Subphylum (Subdivision)",40),SUBSECTION("Subsection",170),SUBSERIES("Subseries",190),SUBSPECIES("Subspecies",210)
    	,SUBTRIBE("Subtribe",130),SUBVARIETY("Subvariety",230),SUPERORDER("Superorder",70),TRIBE("Tribe",120),VARIETY("Variety",220);
    
		private final String name;
		private final Integer value;
    	TaxonRanks(String name,Integer value) {
			this.name=name;
			this.value=value;
		}
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public Integer getValue() {
    		return this.value;
    	}
    	
    	public static TaxonRanks getRankFromValue(Integer value) {
    		for(TaxonRanks tr:values()) {
    			if(Objects.equals(tr.value, value)) return tr;
    		}
    		return null;
    	}
	}
	
	public enum Facets {	// supertypes of relationships
		TAXONOMY
		,ECOLOGY
		,OCCURRENCE
		,MORPHOLOGY
		,IMAGE
	}

	public enum PhenologicalStates {
		UNKNOWN((short)0),VEGETATIVE((short)1),FLOWER((short)2),DISPERSION((short)3),FLOWER_DISPERSION((short)4);
		
		private final Short code;
		PhenologicalStates (Short code) {
			this.code=code;
		}
		
		public Short getCode() {
			return code;
		}
		
    	public static PhenologicalStates getStateFromCode(Short code) {
    		for(PhenologicalStates tr:values()) {
    			if(Objects.equals(tr.code, code)) return tr;
    		}
    		return null;
    	}
	}

	// Thanks to Estevão Portela-Pereira to all discussions and contributions to the *status!
	public enum OccurrenceStatus {		// this applies to the current status of the taxon in a given territory
		PRESENT							// Taxon is currently present
		,DOUBT_OVER_PRESENCE			// There is doubt over the presence of this taxon due to geographic issues (NOTE: doubt because of taxonomic issues is treated in another field)
		,POSSIBLE_OCCURRENCE			// Taxon might occur in the territory given its distribution and habitat elsewhere, but there is *no* evidence at all of its occurrence
		,ASSUMED_PRESENT				// Taxon has not been observed recently but there are past unequivocal evidences of its occurrence and there are no reasons to suppose that it might have gone extinct.
		,POSSIBLY_EXTINCT				// Taxon is possibly extinct: there are no recent observations, so we assume it may be extinct, but it might still exist, according to expert's opinion
		,EXTINCT						// Taxon is extinct: there are no recent observations, and it is very unlikely that it might still exist, according to expert's opinion
		,ABSENT_BUT_REPORTED_IN_ERROR	// Taxon is absent, but it has been erroneously reported earlier (e.g. because of mis-identifications)
		,ERROR							// Some error occurred
		// The following are deprecated!!
		,RARE,COMMON,OCCURS
		,UNCERTAIN_OCCURRENCE			// Taxon presumably exists, but it is not certain if the taxon occurs or not, because of taxonomic problems
		;
	}
	
	public enum NativeStatus {
		NATIVE((short)0, "is NATIVE to")
		,ASSUMED_NATIVE((short)0, "is ASSUMED to be NATIVE to")
		,DOUBTFULLY_NATIVE((short)1, "is DOUBTFULLY NATIVE to")			// it might be native, but there are also reasons to suspect the opposite
		,NATIVE_REINTRODUCED((short)1, "is NATIVE but REINTRODUCED to")
		,CRYPTOGENIC((short)0, "is CRYPTOGENIC in")
		,DOUBTFULLY_EXOTIC((short)1, "is DOUBTFULLY EXOTIC in")		// it might be introduced, but there are also reasons to suspect the opposite
		,ASSUMED_EXOTIC((short)1, "is ASSUMED to be EXOTIC in")
		,EXOTIC((short)2, "is EXOTIC in")
		,EXOTIC_REINTRODUCED((short)2, "is EXOTIC but REINTRODUCED in")
		//,ENDEMIC((short)3, "is ENDEMIC to")
		,NEAR_ENDEMIC((short)4, "is NEAR ENDEMIC to")					// native and quasi-endemic (say, more than 80% of its native populations in the territory)
		,EXISTING((short)0, "EXISTS in")								// it exists with different status depending on the sub-territory
		,ERROR((short)-1, "ERROR");
		
		private final Short code;
		private final String verbose;
		
		NativeStatus (Short code,String verbose) {
			this.code=code;
			this.verbose=verbose;
		}
		
		public Short getCode() {
			return code;
		}
		
    	public static NativeStatus fromCode(Short code) {
    		for(NativeStatus tr:values()) {
    			if(Objects.equals(tr.code, code)) return tr;
    		}
    		return NativeStatus.ERROR;
    	}

    	public static NativeStatus fromString(String name) {
    		for(NativeStatus tr:values()) {
    			if(Objects.equals(tr.toString(), name)) return tr;
    		}
    		return NativeStatus.ERROR;
    	}
    	
    	public String toVerboseString() {
    		return this.verbose;
    	}

	}

	public enum AbundanceLevel {
		VERY_RARE						// Few populations with few individuals
		,RARE
		,OCCASIONAL
		,COMMON
		,VERY_COMMON
	}

	public enum WorldDistributionCompleteness {		// "Whether or not the plant-area records in the DB represent the complete world native distribution for the plant"
		DISTRIBUTION_COMPLETE
		,DISTRIBUTION_INCOMPLETE
		,NOT_KNOWN
	}
	
	public static Map<Facets,RelTypes[]> FacetRelTypes=new EnumMap<Facets,RelTypes[]>(Facets.class);
	public static Map<RelTypes,Facets> RelTypesFacet=new HashMap<RelTypes,Facets>();
	
	public enum TerritoryTypes {
		COUNTRY
		,GEOGRAPHIC_BOUNDARY
		,BIOGEOGRAPHIC_BOUNDARY
		,PROTECTED_AREA
		,ADMINISTRATIVE_BOUNDARY
		,NOT_SET
	}
	
	// All types of relationships in the graph
	public enum RelTypes {
		PART_OF(Facets.TAXONOMY, PART_OF.class),								// TaxEnt PART_OF TaxEnt
		SYNONYM(Facets.TAXONOMY, pt.floraon.entities.SYNONYM.class),			// TaxEnt SYNONYM TaxEnt
		HYBRID_OF(Facets.TAXONOMY, pt.floraon.entities.HYBRID_OF.class),		// TaxEnt HYBRID_OF TaxEnt
		OBSERVED_IN(Facets.OCCURRENCE, pt.floraon.entities.OBSERVED_IN.class),	// TaxEnt OBSERVED_IN SpeciesList
		//IDENTIFIED_AS(Facets.OCCURRENCE, pt.floraon.entities.IDENTIFIED_AS.class),
		OBSERVED_BY(Facets.OCCURRENCE, pt.floraon.entities.OBSERVED_BY.class),	// SpeciesList OBSERVED_BY Author
    	HAS_QUALITY(Facets.MORPHOLOGY, pt.floraon.entities.HAS_QUALITY.class),	// TaxEnt HAS_QUALITY Attribute
    	ATTRIBUTE_OF(Facets.TAXONOMY, pt.floraon.entities.ATTRIBUTE_OF.class),	// Attribute ATTRIBUTE_OF Character
		EXISTS_IN(Facets.OCCURRENCE, pt.floraon.entities.EXISTS_IN.class),		// TaxEnt EXISTS_IN Territory
		BELONGS_TO(Facets.TAXONOMY, pt.floraon.entities.BELONGS_TO.class);		// Territory BELONGS_TO Territory
    	//IMAGE_OF(Facets.IMAGE,pt.floraon.entities.ATTRIBUTE_OF.class);
		
		Facets facet;
		Class<? extends GeneralDBEdge> edgeClass;
		
		RelTypes(Facets facet,Class<? extends GeneralDBEdge> cls) {
			this.facet=facet;
			this.edgeClass=cls;
		}
		
		public Facets getFacet() {
			return this.facet;
		}

		public static RelTypes[] getRelTypesOfFacet(Facets facet) {
			List<RelTypes> out=new ArrayList<RelTypes>();
			for(RelTypes art:RelTypes.values()) {
				if(art.getFacet().equals(facet)) out.add(art);
			}
			return out.toArray(new RelTypes[0]);
		}

		public static RelTypes[] getRelTypesOfFacets(Facets[] facets) {
			List<Facets> fac=Arrays.asList(facets);
			List<RelTypes> out=new ArrayList<RelTypes>();
			for(RelTypes art:RelTypes.values()) {
				if(fac.contains(art.getFacet())) out.add(art);
			}
			return out.toArray(new RelTypes[0]);
		}

		public GeneralDBEdge getEdge() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Class<?>[] types = {};
			Constructor<?> constructor = this.edgeClass.getConstructor(types);
			Object[] parameters = {};
			return (GeneralDBEdge)constructor.newInstance(parameters);
		}
	}

	public enum NodeTypes {
    	specieslist,		// node representing a species inventory
    	taxent,				// taxonomic node of any rank, or eventually without any formal rank (e.g. Anemone palmata (white-flowered form)
    	//entity,				// an instance of any taxonomic rank and optionally of any organ
    	author,
    	attribute,			// a morphological attribute (e.g. red flower)
    	character,			// a morphological character (e.g. flower color, leaf shape...)
    	image,
    	territory			// a geographic territory (e.g. country)
    }

	static {
    	for(Entry<Facets,RelTypes[]> f:FacetRelTypes.entrySet()) {
    		for(RelTypes rt:f.getValue()) {
    			RelTypesFacet.put(rt,f.getKey());
    		}
    	}
    }
    
    public static String CHECKLISTFIELDS=
    		"{rank:"+TaxonRanks.SPECIES.getValue()+"}"
    		+ ",{rank:"+TaxonRanks.FORM.getValue()+"}"
			+ ",{rank:"+TaxonRanks.SUBSPECIES.getValue()+"}"
			+ ",{rank:"+TaxonRanks.VARIETY.getValue()+"}"
			+ ",{rank:"+TaxonRanks.GENUS.getValue()+"}"
			+ ",{rank:"+TaxonRanks.FAMILY.getValue()+"}"
			+ ",{rank:"+TaxonRanks.ORDER.getValue()+"}";

	public static Map<String,String> infraRanks;
    static {
    	infraRanks = new HashMap<String,String>();
        infraRanks.put("subspecies","subsp.");
        infraRanks.put("form","f.");
        infraRanks.put("variety","var.");
    }
}
