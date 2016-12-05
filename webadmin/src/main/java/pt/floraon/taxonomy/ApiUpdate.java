package pt.floraon.taxonomy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import com.google.gson.Gson;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.taxonomy.entities.TaxEnt;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.driver.results.GraphUpdateResult;
import pt.floraon.server.FloraOnServlet;

/**
 * Provides services to work with nodes and links (add, update, delete)
 * @author miguel
 *
 */
@MultipartConfig
@WebServlet("/api/update/*")
public class ApiUpdate extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		doFloraOnPost();
	}

	@Override
	public void doFloraOnPost()
			throws ServletException, IOException, FloraOnException {
		String rank, name, annot, shortName;
		INodeKey from, to, id;
		int res;
		
		if(!getUser().canMODIFY_TAXA_TERRITORIES()) {
			error("You must login to do this operation!");
			return;
		}
		
		ListIterator<String> partIt=this.getPathIteratorAfter("update");

		switch(partIt.next()) {
		case "delete":
			if(!getUser().canEDIT_FULL_CHECKLIST()) error("You must login to do this operation!");
			id=getParameterAsKey("id");
//			success(EntityFactory.toJsonElement(NWD.deleteVertexOrEdge(id),false));
			success(new Gson().toJsonTree(NWD.deleteVertexOrEdge(id)));
			return;

		case "deleteleaf":
			if(!getUser().canEDIT_FULL_CHECKLIST()) error("You must login to do this operation!");
			id=getParameterAsKey("id");
			//if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
//			success(EntityFactory.toJsonElement(NWD.deleteLeafNode(id),false));
			success(new Gson().toJsonTree(NWD.deleteLeafNode(id)));
			return;

		case "setsynonym":
			from=getParameterAsKey("from");
			to=getParameterAsKey("to");
			driver.wrapTaxEnt(from).setSynonymOf(to);
			//.setSynonymOf(graph.dbNodeWorker.getTaxEntVertex(ArangoKey.fromString(to)));
			success("Ok");
			return;
			
		case "detachsynonym":
			from=getParameterAsKey("from");
			to=getParameterAsKey("to");
			NWD.detachSynonym(from, to);
			success("Ok");
			return;

		case "unsetcompleteterritory":
			res = driver.wrapTaxEnt(getParameterAsKey("id")).unsetTerritoryWithCompleteDistribution(getParameterAsKey("territory"));
			success(res == 0 ? "Nothing removed" : "Removed");
			return;
			
		case "add":
			if(!partIt.hasNext()) {
				error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "link":
				from = getParameterAsKey("from");
				to = getParameterAsKey("to");
				String type = getParameterAsString("type");
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
			
			case "completeterritory":
				res = driver.wrapTaxEnt(getParameterAsKey("id")).setTerritoryWithCompleteDistribution(getParameterAsKey("territory"));
				success(res == 0 ? "Nothing added" : "Added");
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
/*			case "links":
				success(
					NWD.updateDocument(
						getParameterAsKey("id"), "current", getParameterAsBooleanNoNull("current")
					).toJsonObject());
				return;*/

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
						,getParameterAsInteger("oldId", null)
					), getParameterAsBooleanNoNull("replace")).toJsonObject());
				// aqui o update tem de ser reformulado!
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
				success(NWD.updateTerritoryNode(
					NWD.getNode(getParameterAsKey("id"), Territory.class)
					, getParameterAsString("name")
					, getParameterAsString("shortName")
					, getParameterAsEnum("type", TerritoryTypes.class)
					, getParameterAsString("theme")
					, getParameterAsBoolean("checklist", true)
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
