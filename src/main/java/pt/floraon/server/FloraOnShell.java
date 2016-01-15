package pt.floraon.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.arangodb.ArangoException;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.QueryException;
import pt.floraon.driver.TaxonomyException;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.NodeTypes;
import pt.floraon.driver.Constants.PhenologicalStates;
import pt.floraon.entities.Author;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.queryparser.YlemParser;
import pt.floraon.results.ResultProcessor;
import pt.floraon.results.SimpleTaxonResult;

public class FloraOnShell {
    public static void main( String[] args ) throws ParseException, IOException {
    	FloraOnDriver graph;
    	try {
			graph=new FloraOnDriver("flora");
		} catch (ArangoException e2) {
			e2.printStackTrace();
			return;
		}
    	ServerResponse server=new ServerResponse(graph, null);
    	
    	System.out.println(Constants.ANSI_GREENBOLD+"\nWelcome to the Flora-On console!\nThis is the query interpreter. Enter a query directly or issue a server command."+Constants.ANSI_RESET+"\nServer commands start with \\\nType \\q to quit, \\sampledata to load some sample data and get it working.");
    	try {
			System.out.println(Constants.ANSI_WHITE+graph.dbSpecificQueries.getNumberOfNodesInCollection(NodeTypes.taxent)+" taxon nodes; "+graph.dbSpecificQueries.getNumberOfNodesInCollection(NodeTypes.attribute)+" attribute nodes; "+graph.dbSpecificQueries.getNumberOfNodesInCollection(NodeTypes.specieslist)+" species inventories."+Constants.ANSI_RESET+"\n");
		} catch (ArangoException e1) {
			System.out.println("Some fatal error reading database. Aborting.");
			System.out.println(e1.getMessage());
			System.exit(1);
		}
    	
    	Iterator<SimpleTaxonResult> it;
    	ResultProcessor<SimpleTaxonResult> rp;
    	String[] commands=new String[]{
			"\\upload/taxonomy?file="
			,"\\upload/attributes?file="
			,"\\upload/occurrences?file="
			,"\\upload/authors?file="
			,"\\checklist"
			,"\\sampledata"
		};
    	
        try {
            ConsoleReader console = new ConsoleReader();
            console.addCompleter(new StringsCompleter(Arrays.asList(commands)));
            console.addCompleter(new FileNameCompleter());
            console.setPrompt(Constants.ANSI_CYANBOLD+"flora-on> "+Constants.ANSI_RESET);
            /*console.addTriggeredAction('\\', new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("klkkl");
					System.exit(0);
				} });*/
            String line = null;
            while ((line = console.readLine()) != null) {
            	line=line.trim();
            	try {
	            	if(line.equals("")) continue;
	            	if(line.equals("\\q")) System.exit(0);
	            	if(line.equals("\\sampledata")) {
	            		System.out.println("Reading sample taxonomy");
	            		graph.dbDataUploader.uploadTaxonomyListFromStream(graph.getClass().getResourceAsStream("/taxonomia_final.csv"), false);
	            		graph.dbDataUploader.uploadTaxonomyListFromStream(graph.getClass().getResourceAsStream("/stepping_stones.csv"), false);
	            		System.out.println("Reading morphology");
	            		//graph.dbDataUploader.uploadMorphologyFromStream(graph.getClass().getResourceAsStream("/morphology.csv"));
	            		System.out.println("\nGenerating random species lists");
	            		generateRandomSpeciesLists(graph,50);
	            		// \\upload/authors?file=/home/miguel/workspace/Flora-On-server/sampledata/authors
	            		// \\upload/occurrences?file=/home/miguel/workspace/Flora-On-server/sampledata/40_records.csv
	            		/*System.out.println("\nCreating some territories");
	            		Territory pt=Territory.newFromName(graph, "Portugal", "pt", (ArangoKey)null);
	            		Territory lu=Territory.newFromName(graph, "Portugal Continental", "lu", pt.getArangoKey());
	            		Territory az=Territory.newFromName(graph, "Açores", "az", pt.getArangoKey());
	            		Territory ma=Territory.newFromName(graph, "Madeira", "ma", pt.getArangoKey());
	            		TaxEnt te=graph.dbNodeWorker.findTaxEnt("Aetheorhiza bulbosa subsp. bulbosa");
	            		lu.setTaxEntNativeStatus(te.getArangoKey(), NativeStatus.NATIVE);
	            		az.setTaxEntNativeStatus(te.getArangoKey(), NativeStatus.EXOTIC);
	            		ma.setTaxEntNativeStatus(te.getArangoKey(), NativeStatus.NATURALIZED);
	            		te=graph.dbNodeWorker.findTaxEnt("Allium ursinum subsp. ursinum");
	            		pt.setTaxEntNativeStatus(te.getArangoKey(), NativeStatus.EXOTIC);*/
	            		continue;
	            	}
	            	
	            	if(line.startsWith("\\")) {           		
	            		server.processCommand(line.substring(1), System.out,false);
	            	} else {
	    				YlemParser ylem=new YlemParser(graph,line);
	    				long start = System.nanoTime();
	    				List<SimpleTaxonResult> res=ylem.execute();
	    				long elapsedTime = System.nanoTime() - start;
	    				
	    				if(res==null)
	    					System.out.println("No results.");
	    				else {
	    					it=res.iterator();
	    					rp=new ResultProcessor<SimpleTaxonResult>(it);
	    					rp.toPrettyTable().printTable();
	    					System.out.println(res.size()+" results.");
	    					//System.out.println(rp.toCSVTable(it));
	    				}
	
	    				System.out.printf("[%.3f sec]\n", (double)elapsedTime/1000000000);
	            	}
				} catch (QueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArangoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TaxonomyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FloraOnException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
/*            	if(line.equals("\\u")) {
	            	try {
	                	//fog.getDataUploader().uploadTaxonomyListFromFile("/media/miguel/Brutal/SPB/Flora-On/Taxonomia/Grafo/taxonomia_full_novo.csv",false);
	                	fog.getDataUploader().uploadMorphologyFromCSV("/home/miguel/Desktop/morfologia(2).csv");
	                	//fog.getDataUploader().uploadTaxonomyListFromFile("/media/miguel/Brutal/SPB/Flora-On/Taxonomia/Grafo/stepping_stones.csv",false);
	            		//fog.getDataUploader().uploadAuthorsFromFile("/home/miguel/authors-uFI1sC");
	                	//fog.getDataUploader().uploadRecordsFromFile("/home/miguel/records-9zitZT");
	                	continue;
	        		} catch (IOException | NumberFormatException e1) {
	        			e1.printStackTrace();
	        			continue;
	        		}
            	}*/
            }
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       

/*    	
    	Iterator<SimpleTaxonResult> vertexIterator2;
		try {
			vertexIterator2 = fog.findTaxaWithin(38.2341329f,-8.2371427f,1000);
			//vertexIterator2 = fog.findOccurrencesWithin(38.2341329f,-8.2371427f,10000);
			ResultProcessor<SimpleTaxonResult> rp2=new ResultProcessor<SimpleTaxonResult>(SimpleTaxonResult.class);
			System.out.println(rp2.toTable(vertexIterator2));
		} catch (ArangoException e1) {
			e1.printStackTrace();
		}

    	*/

    	/*
    	List<ChecklistEntry> chklst=fog.getCheckList();
    	Collections.sort(chklst);
    	for(ChecklistEntry ce:chklst) {
    		System.out.println(ce.toString());
    	}*/

    	
    	/*    		TaxEnt tmp;
    	Iterator<TaxEntVertex> vertexIterator = fog.speciesTextQuery("cist");
    	while (vertexIterator.hasNext()) {
    		tmp=new TaxEnt(vertexIterator.next());
    		System.out.println(tmp.getFullName());
    	}*/

    	/*VertexEntity<TaxEnt> root=null;
		try {
			//arng.graphCreateVertex("exp", "vertex", new TaxEnt("Plantae","kingdom",0), true);
			vertexCursor = fog.driver.graphGetVertexCursor("exp", TaxEnt.class, new TaxEnt(null,"kingdom",null,null), null, null);
			vertexIterator = vertexCursor.iterator();
			while (vertexIterator.hasNext()) {
				root = vertexIterator.next();
				TaxEnt te = root.getEntity();
				System.out.printf("%20s  %15s%n", root.getDocumentHandle(), te.getName());
			}
		} catch (ArangoException e1) {
			e1.printStackTrace();
		}
*/

        
        try {
        	System.out.println("N: "+graph.driver.getDocuments("taxent").size());
/*
        	DocumentEntity<TaxEnt> doc=fog.driver.getDocument(root.getDocumentHandle(),TaxEnt.class);
        	System.out.println(doc.getEntity().getName());
        	TraversalQueryOptions tqo=new TraversalQueryOptions(); 
        	tqo.setDirection(Direction.ANY);
        	tqo.setStartVertex(root.getDocumentHandle());
        	tqo.setGraphName("exp");
        	tqo.setMaxDepth((long) 6);
        	//tqo.setFilter("if(path.edges.length>3) return 'prune'; else return 'exclude';");
        	tqo.setFilter("if(vertex.rank=='species') return 'prune'; else return 'exclude';");*/
        	//tqo.setFilter("if(config.datasource.graph._neighbors(vertex).length<2) return 'prune'; else return 'exclude';");
        	//tqo.setVisitor("if (!result) {result = {vertices:[] };} if (! result.hasOwnProperty('visited')) { result.visited = { vertices: [ ] }; } result.visited.vertices.push({name:vertex._id});");

/*        	
            DocumentCursor<BaseDocument> rs=fog.driver.executeDocumentQuery("FOR v in graph_vertices('exp',{}) return v"//"FOR v in graph_traversal('exp',{rank:'kingdom'},'any') return v"
            		, null, null, BaseDocument.class);
            
            Iterator<BaseDocument> iterator = rs.entityIterator();
            while (iterator.hasNext()) {
                BaseDocument aDocument = iterator.next();
                System.out.println("Key: " + aDocument.getProperties().get("name"));
            }*/
            
        	/*
        	TraversalEntity<TaxEnt,PART_OF> te=fog.driver.getTraversal(tqo, TaxEnt.class, PART_OF.class);

    		for(VertexEntity<TaxEnt> ve:te.getVertices()) {
    			System.out.println(ve.getEntity().getID()+": "+ve.getEntity().name);
    		}*/
        	
/*
        	for(PathEntity<TaxEnt,PART_OF> pe:te.getPaths()) {
        		System.out.println( pe.toString());
        		for(VertexEntity<TaxEnt> ve:pe.getVertices()) {
        			//System.out.print(ve.getEntity().dummy+" | ");
        			System.out.print(ve.getDocumentKey()+" | ");
        		}
        		System.out.print("\n");
        	}*/
		} catch (ArangoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /*
        DocumentEntity<TaxEnt> d1;
        DocumentEntity<TaxEnt> d2;
        try {
        	//GraphEntity graph=arng.getGraph("exp");
        	List<String> crt=arng.getDocuments("vertex",true);
        	for(int i=0;i<1000;i++) {
				int el1=(int) (Math.random() * (crt.size()));
				int el2=(int) (Math.random() * (crt.size()));
				System.out.println(crt.get(el1));
				d1=arng.getDocument(crt.get(el1), TaxEnt.class);
				d2=arng.getDocument(crt.get(el2), TaxEnt.class);
				System.out.println(d1.getEntity().rank);
				arng.createEdge("relation", new PART_OF(true), d1.getDocumentHandle(), d2.getDocumentHandle(), false, false);
        	}	
		} catch (ArangoException e) {
			e.printStackTrace();
		}
        */
    }
    
    public static void generateRandomSpeciesLists(FloraOnDriver fog,int number) {
    	SpeciesList sln;
    	Author autnode=null;
    	int nsp;
    	float lat,lon;
    	
    	Author[] authors=null;
		try {
			authors = new Author[3];
			authors[0]=fog.dbNodeWorker.getAuthorById(1);
			if(authors[0]==null) authors[0]=new Author(fog,1,"John Doe","email@nothing.pt","JD","JDoe",10);
			authors[1]=fog.dbNodeWorker.getAuthorById(2);
			if(authors[1]==null) authors[1]=new Author(fog,2,"Miguel Porto","email@nothing.pt","MP","MPorto",10);
			authors[2]=fog.dbNodeWorker.getAuthorById(3);
			if(authors[2]==null) authors[2]=new Author(fog,3,"Someone else","email@nothing.pt","SE","SElse",10);
		} catch (ArangoException e1) {
			System.out.println(e1.getErrorMessage());
		}
    	for(int i=0;i<number;i++) {
   			autnode=authors[ThreadLocalRandom.current().nextInt(0, authors.length)];
   			
    		lat=37+ThreadLocalRandom.current().nextFloat()*5;
    		lon=(float) (-9.5+ThreadLocalRandom.current().nextFloat()*3);
    		try {
				sln=new SpeciesList(fog,lat,lon,ThreadLocalRandom.current().nextInt(1990, 2016),ThreadLocalRandom.current().nextInt(1, 12)
					,ThreadLocalRandom.current().nextInt(1, 30),0,null,null,false,null,null);
				if(autnode!=null) sln.setObservedBy(autnode, true);
				nsp=ThreadLocalRandom.current().nextInt(1, 60 + 1);
				int[] idents=new int[nsp];
				for(int j=0;j<nsp;j++) {
					idents[j]=ThreadLocalRandom.current().nextInt(1, 4000 + 1);
				}
				Iterator<TaxEntVertex> itte=fog.dbNodeWorker.getTaxEntsByIds(idents);
				while(itte.hasNext()) {
					new TaxEnt(fog,itte.next()).setObservedIn(sln, (short)0, (short)1, PhenologicalStates.UNKNOWN, null, 10000, null, NativeStatus.WILD, null);
				}
				if(i % 100==0) {System.out.print(".");System.out.flush();}
				if(i % 1000==0) {System.out.print(i);System.out.flush();}
			} catch (ArangoException | FloraOnException e) {
				e.printStackTrace();
			}
    	}
    }
}
