package pt.floraon.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import com.arangodb.entity.EntityFactory;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.entities.TaxEnt;
import pt.floraon.entities.Territory;
import pt.floraon.results.GraphUpdateResult;

@MultipartConfig
public class NodeWorker extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		doFloraOnPost();
	}

	@Override
	public void doFloraOnPost()
			throws ServletException, IOException, FloraOnException {
		String rank, current, name, annot, shortName;
		INodeKey from, to, id;
		
		if(!isAuthenticated()) {
			error("You must login to do this operation!");
			return;
		}
		
		ListIterator<String> partIt=this.getPathIteratorAfter("update");

		switch(partIt.next()) {
		case "delete":
			id=getParameterAsKey("id");
			//if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
			success(EntityFactory.toJsonElement(NWD.deleteNode(id),false));
			return;

		case "deleteleaf":
			id=getParameterAsKey("id");
			//if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
			success(EntityFactory.toJsonElement(NWD.deleteLeafNode(id),false));
			return;

		case "setsynonym":
			from=getParameterAsKey("from");
			to=getParameterAsKey("to");
			driver.wrapTaxEnt(from).setSynonymOf(NWD.getTaxEntById(to));
			//.setSynonymOf(graph.dbNodeWorker.getTaxEntVertex(ArangoKey.fromString(to)));
			success("Ok");
			return;
			
		case "detachsynonym":
			from=getParameterAsKey("from");
			to=getParameterAsKey("to");
			NWD.detachSynonym(from, to);
			success("Ok");
			return;
			
		case "add":
			if(!partIt.hasNext()) {
				error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "link":
				from=getParameterAsKey("from");
				to=getParameterAsKey("to");
				
				String type=request.getParameter("type");
/*				if(id==null || id.trim().length()<1 || id2==null || id2.trim().length()<1 || type==null) {
					error("You must provide relationship type and two document handles 'from' and 'to'");
					return;
				}
												
				GeneralNodeWrapperImpl n1=graph.dbNodeWorker.getNodeWrapper(id);
				GeneralNodeWrapperImpl n2=graph.dbNodeWorker.getNodeWrapper(id2);
				if(n1==null) {
					error("Node "+id+" not found.");
					return;
				}
				if(n2==null) {
					error("Node "+id2+" not found.");
					return;
				}*/
				try {
					//success(n1.createRelationshipTo(n2.getNode(), RelTypes.valueOf(type.toUpperCase())).toJsonObject());
					success(driver.wrapNode(from).createRelationshipTo(to, RelTypes.valueOf(type.toUpperCase())).toJsonObject());
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
				return;
				
			case "inferiortaxent":	// this adds a bond taxent child of the given parent and ensures it is taxonomically valid
				success(driver.wrapTaxEnt(getParameterAsKey("parent")).createTaxEntChild(
					getParameterAsString("name")
					, getParameterAsString("author")
					, TaxonRanks.getRankFromValue(getParameterAsInteger("rank",null))
					, getParameterAsString("sensu"), getParameterAsString("annot"), getParameterAsBooleanNoNull("current")
				).toString());
				return;

			case "territory":
				name=getParameterAsString("name");
				shortName=getParameterAsString("shortName");
				rank=getParameterAsString("rank");
				annot=getParameterAsString("annot");
				success(
					new GraphUpdateResult(
						driver
						, NWD.createTerritory(name, shortName, rank==null ? TerritoryTypes.COUNTRY : TerritoryTypes.valueOf(rank), annot, false, null).getID()
					).toJsonObject()
				);
				return;
				
			case "taxent":	// this only adds a free taxent
		    	success(
	    			new GraphUpdateResult(driver
    					, NWD.createTaxEntFromName(
							getParameterAsString("name")
							,getParameterAsString("author")
							,TaxonRanks.getRankFromValue(getParameterAsInteger("rank",null)),null,null,true).getID()).toJsonObject()
				);
				//success(output, graph.dbNodeWorker.createTaxEntNode(name, author, TaxonRanks.getRankFromValue(Integer.parseInt(rank)), null, true).toJsonObject(), includeHeaders);
				return;
				
/*			case "attribute":
				name=getParameter("name");
				shortName=getParameter("shortName");
				description=getParameter("description");
				success(graph.dbNodeWorker.createAttributeNode(name, shortName, description).toJsonObject());
				return;
			
			case "character":
				name=getParameter("name");
				shortName=getParameter("shortName");
				description=getParameter("description");
				success(graph.dbNodeWorker.createCharacterNode(name, shortName, description).toJsonObject());
				return;*/
				
			default:
				error("Invalid node type");
				return;
			}
			
		case "update":
			if(!partIt.hasNext()) {
				error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "links":
				success(
					NWD.updateDocument(
						getParameterAsKey("id"), "current", getParameterAsBooleanNoNull("current")
					).toJsonObject());
				return;

			case "taxent":
				success(NWD.updateTaxEntNode(
					getParameterAsKey("id")
					,new TaxEnt(
						getParameterAsString("name")
						,getParameterAsInteger("rank",null)
						,getParameterAsString("author")
						,getParameterAsString("sensu")
						,getParameterAsString("annotation")
						,getParameterAsBoolean("current")
						,null
						,getParameterAsEnum("worldDistributionCompleteness", WorldNativeDistributionCompleteness.class)
						
					), getParameterAsBooleanNoNull("replace")).toJsonObject());
				/*
				success(NWD.updateTaxEntNode(
					NWD.getTaxEnt(getParameterAsArangoKey("id"))
					, getParameter("name")
					, rank==null ? null : TaxonRanks.getRankFromValue(Integer.parseInt(rank))
					, current==null ? null : Integer.parseInt(current)==1
					, getParameter("author"), getParameter("comment")).toJsonObject()
				);*/
				return;
				
			case "territory":
				name=getParameterAsString("name");
				shortName=getParameterAsString("shortName");
				rank=getParameterAsString("type");
				annot=getParameterAsString("theme");
				if( (current=getParameterAsString("checklist"))==null ) current="true";
				success(NWD.updateTerritoryNode(
					NWD.getNode(getParameterAsKey("id"), Territory.class)
					, name
					, shortName
					, TerritoryTypes.valueOf(rank)
					, annot
					, Boolean.parseBoolean(current)
					).toJsonObject()
				);
				return;

			default:
				error("Invalid node type");
				return;
			}
		}
	}
}
