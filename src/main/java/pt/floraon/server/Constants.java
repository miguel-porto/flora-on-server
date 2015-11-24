package pt.floraon.server;

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
	}

	public enum PhenologicalStates {
		UNKNOWN((short)0),VEGETATIVE((short)1),IN_FLOWER((short)2),IN_DISPERSION((short)3),IN_FLOWER_AND_DISPERSION((short)4);
		
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

	public enum NativeStatus {
		WILD((short)0),NATURALIZED((short)1);
		
		private final Short code;
		NativeStatus (Short code) {
			this.code=code;
		}
		
		public Short getCode() {
			return code;
		}
		
    	public static NativeStatus getStateFromCode(Short code) {
    		for(NativeStatus tr:values()) {
    			if(Objects.equals(tr.code, code)) return tr;
    		}
    		return null;
    	}
	}

	public static Map<Facets,AllRelTypes[]> FacetRelTypes=new EnumMap<Facets,AllRelTypes[]>(Facets.class);
	public static Map<AllRelTypes,Facets> RelTypesFacet=new HashMap<AllRelTypes,Facets>();

	public enum AllRelTypes {
		PART_OF(Facets.TAXONOMY,PART_OF.class),
		SYNONYM(Facets.TAXONOMY,pt.floraon.entities.SYNONYM.class),
		HYBRID_OF(Facets.TAXONOMY,pt.floraon.entities.HYBRID_OF.class),
		OBSERVED_IN(Facets.OCCURRENCE,pt.floraon.entities.OBSERVED_IN.class),
		OBSERVED_BY(Facets.OCCURRENCE,pt.floraon.entities.OBSERVED_BY.class),
    	HAS_QUALITY(Facets.MORPHOLOGY,pt.floraon.entities.HAS_QUALITY.class),
    	ATTRIBUTE_OF(Facets.MORPHOLOGY,pt.floraon.entities.ATTRIBUTE_OF.class);
		
		Facets facet;
		Class<? extends GeneralDBEdge> edgeClass;
		
		AllRelTypes(Facets facet,Class<? extends GeneralDBEdge> cls) {
			this.facet=facet;
			this.edgeClass=cls;
		}
		
		public Facets getFacet() {
			return this.facet;
		}
		
		public static AllRelTypes[] getRelTypesOfFacet(Facets facet) {
			List<AllRelTypes> out=new ArrayList<AllRelTypes>();
			for(AllRelTypes art:AllRelTypes.values()) {
				if(art.getFacet().equals(facet)) out.add(art);
			}
			return out.toArray(new AllRelTypes[0]);
		}

		public static AllRelTypes[] getRelTypesOfFacets(Facets[] facets) {
			List<Facets> fac=Arrays.asList(facets);
			List<AllRelTypes> out=new ArrayList<AllRelTypes>();
			for(AllRelTypes art:AllRelTypes.values()) {
				if(fac.contains(art.getFacet())) out.add(art);
			}
			return out.toArray(new AllRelTypes[0]);
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
    	character			// a morphological character (e.g. flower color, leaf shape...)
    }
/*
	public static class Pair<L,R> implements Comparable<Pair<L,R>> {
		  private final L left;
		  private final R right;

		  public Pair(L left, R right) {
		    this.left = left;
		    this.right = right;
		  }

		  public L getLeft() { return left; }
		  public R getRight() { return right; }

		  @Override
		  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

		  @Override
		  public boolean equals(Object o) {
		    if (!(o instanceof Pair)) return false;
			@SuppressWarnings("unchecked")
			Pair<L,R> pairo = (Pair<L,R>) o;
		    return (this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight()));// || (this.left.equals(pairo.getRight()) && this.right.equals(pairo.getLeft()));
		  }

		@Override
		public int compareTo(Pair<L, R> o) {
			return this.getLeft().toString().compareTo(o.getLeft().toString());
		}
	}
*/
    static {
    	for(Entry<Facets,AllRelTypes[]> f:FacetRelTypes.entrySet()) {
    		for(AllRelTypes rt:f.getValue()) {
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
