package pt.floraon.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.arangodb.ArangoException;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import pt.floraon.dbworker.FloraOnGraph;
import pt.floraon.dbworker.QueryException;
import pt.floraon.dbworker.TaxonomyException;
import pt.floraon.entities.Author;
import pt.floraon.entities.SimpleTaxonResult;
import pt.floraon.entities.SpeciesList;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.TaxEntVertex;
import pt.floraon.queryparser.YlemParser;
import pt.floraon.server.Constants.NativeStatus;
import pt.floraon.server.Constants.NodeTypes;
import pt.floraon.server.Constants.PhenologicalStates;

public class FloraOnShell {
    public static void main( String[] args ) throws ParseException, IOException {
    	FloraOnGraph fog;
    	try {
			fog=new FloraOnGraph("flora");
		} catch (ArangoException e2) {
			e2.printStackTrace();
			return;
		}
    	
    	//generateRandomSpeciesLists(fog,50000);
    	
    	System.out.println(Constants.ANSI_GREENBOLD+"\nWelcome to the Flora-On console!\nThis is the query interpreter. Enter a query directly or issue a server command."+Constants.ANSI_RESET+"\nServer commands start with \\\nType \\q to quit.");
    	try {
			System.out.println(Constants.ANSI_WHITE+fog.getNumberOfNodesInCollection(NodeTypes.taxent)+" taxon nodes; "+fog.getNumberOfNodesInCollection(NodeTypes.attribute)+" attribute nodes; "+fog.getNumberOfNodesInCollection(NodeTypes.specieslist)+" species inventories."+Constants.ANSI_RESET);
		} catch (ArangoException e1) {
			System.out.println("Some fatal error reading database. Aborting.");
			System.out.println(e1.getMessage());
			System.exit(1);
		}
    	
    	String query;
    	Iterator<SimpleTaxonResult> it;
    	ResultProcessor<SimpleTaxonResult> rp;
        try {
            ConsoleReader console = new ConsoleReader();
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
            	try {
	            	if(line.equals("")) continue;
	            	if(line.equals("\\q")) System.exit(0);
	            	
	            	if(line.startsWith("\\")) {           		
							ServerDispatch.processCommand(line.substring(1), fog, new PrintWriter(System.out));
	            	} else {
	    				YlemParser ylem=new YlemParser(fog,line);
	    				long start = System.nanoTime();
	    				List<SimpleTaxonResult> res=ylem.execute();
	    				long elapsedTime = System.nanoTime() - start;
	    				
	    				if(res==null)
	    					System.out.println("No results.");
	    				else {
	    					System.out.println(res.size()+" results.");
	    					it=res.iterator();
	    					rp=new ResultProcessor<SimpleTaxonResult>(SimpleTaxonResult.class);
	    					System.out.println(rp.toCSVTable(it));
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
        
    	if(System.console()!=null) {
/*	    	while((query=System.console().readLine("floraon> "))!="") {
		    	//System.out.println(fog.findExistingSpeciesList(3,37.456f, -8.775f,2015,10,12,1000));
				//System.out.println(fog.findExistingSpeciesList(3,37.456f, -8.767f,null,null,null,10));
				//fog.printTaxa(fog.findTaxEntWithin(38.1714f, -7.0572f, 1000000));
				YlemParser ylem=new YlemParser(fog,query);
				it=ylem.execute().iterator();
				
				//it=fog.speciesTextQuery(query);
				rp=new ResultProcessor<SimpleTaxonResult>(SimpleTaxonResult.class);
				System.out.println(rp.toTable(it));
	    	}*/
    	} else {
    		query="perto:38.5778 -8.5563 medicago";
			YlemParser ylem=new YlemParser(fog,query);
			it=ylem.execute().iterator();
			
			//it=fog.speciesTextQuery(query);
			rp=new ResultProcessor<SimpleTaxonResult>(SimpleTaxonResult.class);
			System.out.println(rp.toCSVTable(it));
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
        	System.out.println("N: "+fog.driver.getDocuments("taxent").size());
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
    
    public static void generateRandomSpeciesLists(FloraOnGraph fog,int number) {
    	SpeciesList sln;
    	Author autnode=null;
    	int idaut,nsp;
    	float lat,lon;
    	for(int i=0;i<number;i++) {
    		autnode=null;
    		while(autnode==null) {
    			idaut=ThreadLocalRandom.current().nextInt(1, 20 + 1);
    			autnode=fog.getAuthorById(idaut);
    		}
    		lat=37+ThreadLocalRandom.current().nextFloat()*5;
    		lon=(float) (-9.5+ThreadLocalRandom.current().nextFloat()*3);
    		try {
				sln=new SpeciesList(fog,lat,lon,ThreadLocalRandom.current().nextInt(1990, 2016),ThreadLocalRandom.current().nextInt(1, 12)
					,ThreadLocalRandom.current().nextInt(1, 30),0,null,null,false);
				if(autnode!=null) sln.setObservedBy(autnode, true);
				nsp=ThreadLocalRandom.current().nextInt(1, 60 + 1);
				int[] idents=new int[nsp];
				for(int j=0;j<nsp;j++) {
					idents[j]=ThreadLocalRandom.current().nextInt(1, 4000 + 1);
				}
				Iterator<TaxEntVertex> itte=fog.getTaxEntsByIds(idents);
				while(itte.hasNext()) {
					new TaxEnt(fog,itte.next()).setObservedIn(sln, (short)0, (short)1, PhenologicalStates.UNKNOWN, null, 10000, null, NativeStatus.WILD, null);
				}
				System.out.println((i+1)+": added "+sln.getID());
			} catch (ArangoException | IOException e) {
				e.printStackTrace();
			}
    	}
    }
}
