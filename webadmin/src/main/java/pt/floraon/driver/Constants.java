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
	
	public enum TaxonRank {
		NORANK("non-taxonomic rank",1000),CLASS("Class",50),FAMILY("Family",100),FORM("Form",240),GENUS("Genus",140),KINGDOM("Kingdom",10)
    	,ORDER("Order",80),PHYLUM("Phylum (Division)",30),SECTION("Section",160),SERIES("Series",180),SPECIES("Species",200),SUBCLASS("Subclass",60)
    	,SUBGENUS("Subgenus",150),SUBFAMILY("Subfamily",110),SUBFORM("Subform",250),SUBKINGDOM("Subkingdom",20),SUBORDER("Suborder",90)
    	,SUBPHYLUM("Subphylum (Subdivision)",40),SUBSECTION("Subsection",170),SUBSERIES("Subseries",190),SUBSPECIES("Subspecies",210)
    	,SUBTRIBE("Subtribe",130),SUBVARIETY("Subvariety",230),SUPERORDER("Superorder",70),TRIBE("Tribe",120),VARIETY("Variety",220);
    
		private final String name;
		private final Integer value;
    	TaxonRank(String name,Integer value) {
			this.name=name;
			this.value=value;
		}
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public Integer getValue() {
    		return this.value;
    	}
    	
    	public static TaxonRank getRankFromValue(Integer value) {
    		for(TaxonRank tr:values()) {
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

	public enum OccurrenceStatus {
		OCCURS,COMMON,RARE,POSSIBLY_EXTINCT,EXTINCT;
	}
	
	public enum NativeStatus {
		WILD((short)0)
		,NATIVE((short)0)
		,EXISTING((short)0)
		,UNCERTAIN((short)1)
		,NATURALIZED((short)2)
		,EXOTIC((short)2)
		,ENDEMIC((short)3)
		,ERROR((short)-1);
		
		private final Short code;
		NativeStatus (Short code) {
			this.code=code;
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
	
	public enum RelTypes {
		PART_OF(Facets.TAXONOMY, PART_OF.class),
		SYNONYM(Facets.TAXONOMY, pt.floraon.entities.SYNONYM.class),
		HYBRID_OF(Facets.TAXONOMY, pt.floraon.entities.HYBRID_OF.class),
		OBSERVED_IN(Facets.OCCURRENCE, pt.floraon.entities.OBSERVED_IN.class),
		OBSERVED_BY(Facets.OCCURRENCE, pt.floraon.entities.OBSERVED_BY.class),
    	HAS_QUALITY(Facets.MORPHOLOGY, pt.floraon.entities.HAS_QUALITY.class),
    	ATTRIBUTE_OF(Facets.TAXONOMY, pt.floraon.entities.ATTRIBUTE_OF.class),
		EXISTS_IN(Facets.OCCURRENCE, pt.floraon.entities.EXISTS_IN.class);
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
    		"{rank:"+TaxonRank.SPECIES.getValue()+"}"
    		+ ",{rank:"+TaxonRank.FORM.getValue()+"}"
			+ ",{rank:"+TaxonRank.SUBSPECIES.getValue()+"}"
			+ ",{rank:"+TaxonRank.VARIETY.getValue()+"}"
			+ ",{rank:"+TaxonRank.GENUS.getValue()+"}"
			+ ",{rank:"+TaxonRank.FAMILY.getValue()+"}"
			+ ",{rank:"+TaxonRank.ORDER.getValue()+"}";

	public static Map<String,String> infraRanks;
    static {
    	infraRanks = new HashMap<String,String>();
        infraRanks.put("subspecies","subsp.");
        infraRanks.put("form","f.");
        infraRanks.put("variety","var.");
    }
}
