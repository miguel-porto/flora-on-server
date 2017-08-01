package pt.floraon.taxonomy.servlets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import com.google.gson.Gson;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.Constants.RelTypes;
import pt.floraon.driver.Constants.TaxonRanks;
import pt.floraon.driver.Constants.TerritoryTypes;
import pt.floraon.driver.Constants.WorldNativeDistributionCompleteness;
import pt.floraon.driver.utils.BeanUtils;
import pt.floraon.ecology.entities.Habitat;
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
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		doFloraOnPost(thisRequest);
	}

	@Override
	public void doFloraOnPost(ThisRequest thisRequest)
			throws ServletException, IOException, FloraOnException {
		String rank, name, annot, shortName, description;
		INodeKey from, to, id;
		Habitat newHab;
		int res;
		
		if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES() && !thisRequest.getUser().canMODIFY_TAXA()) {
			thisRequest.error("You must login to do this operation!");
			return;
		}
		
		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("update");

		switch(partIt.next()) {
		case "delete":
			if(!thisRequest.getUser().canEDIT_FULL_CHECKLIST()) {thisRequest.error("You don't have privileges for this operation!"); return;}
			id=thisRequest.getParameterAsKey("id");
//			success(EntityFactory.toJsonElement(NWD.deleteVertexOrEdge(id),false));
			thisRequest.success(new Gson().toJsonTree(NWD.deleteVertexOrEdge(id)));
			return;

		case "deleteleaf":
			if(!thisRequest.getUser().canEDIT_FULL_CHECKLIST()) {thisRequest.error("You don't have privileges for this operation!"); return;}
			id=thisRequest.getParameterAsKey("id");
			//if(id==null || id.trim().length()<1) throw new FloraOnException("You must provide a document handle as id");
//			success(EntityFactory.toJsonElement(NWD.deleteLeafNode(id),false));
			thisRequest.success(new Gson().toJsonTree(NWD.deleteLeafNode(id)));
			return;

		case "setsynonym":
			from=thisRequest.getParameterAsKey("from");
			to=thisRequest.getParameterAsKey("to");
			driver.wrapTaxEnt(from).setSynonymOf(to);
			//.setSynonymOf(graph.dbNodeWorker.getTaxEntVertex(ArangoKey.fromString(to)));
			thisRequest.success("Ok");
			return;
			
		case "detachsynonym":
			from=thisRequest.getParameterAsKey("from");
			to=thisRequest.getParameterAsKey("to");
			NWD.detachSynonym(from, to);
			thisRequest.success("Ok");
			return;

		case "unsetcompleteterritory":
			if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES()) {thisRequest.error("You don't have privileges for this operation!"); return;}
			res = driver.wrapTaxEnt(thisRequest.getParameterAsKey("id")).unsetTerritoryWithCompleteDistribution(thisRequest.getParameterAsKey("territory"));
			thisRequest.success(res == 0 ? "Nothing removed" : "Removed");
			return;
			
		case "add":
			if(!partIt.hasNext()) {
				thisRequest.error("Choose the node type: taxent");
				return;
			}
			switch(partIt.next()) {
			case "link":
				from = thisRequest.getParameterAsKey("from");
				to = thisRequest.getParameterAsKey("to");
				String type = thisRequest.getParameterAsString("type");
				try {
					//success(n1.createRelationshipTo(n2.getNode(), RelTypes.valueOf(type.toUpperCase())).toJsonObject());
					thisRequest.success(driver.wrapNode(from).createRelationshipTo(to, RelTypes.valueOf(type.toUpperCase())).toJsonObject());
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
				return;
				
			case "inferiortaxent":	// this adds a bond taxent child of the given parent and ensures it is taxonomically valid
				thisRequest.success(driver.wrapTaxEnt(thisRequest.getParameterAsKey("parent")).createTaxEntChild(
						thisRequest.getParameterAsString("name")
					, thisRequest.getParameterAsString("author")
					, TaxonRanks.getRankFromValue(thisRequest.getParameterAsInteger("rank",null))
					, thisRequest.getParameterAsString("sensu"), thisRequest.getParameterAsString("annot"), thisRequest.getParameterAsBooleanNoNull("current")
				).toString());
				return;

			case "territory":
				if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES()) {thisRequest.error("You don't have privileges for this operation!"); return;}
				name=thisRequest.getParameterAsString("name");
				shortName=thisRequest.getParameterAsString("shortName");
				rank=thisRequest.getParameterAsString("rank");
				annot=thisRequest.getParameterAsString("annot");
				thisRequest.success(
					new GraphUpdateResult(driver
						, NWD.createTerritory(name, shortName, rank==null ? TerritoryTypes.COUNTRY : TerritoryTypes.valueOf(rank), annot, false, null).getID()
					).toJsonObject()
				);
				return;
				
			case "taxent":	// this only adds a free taxent
				thisRequest.success(
	    			new GraphUpdateResult(driver
    					, NWD.createTaxEntFromName(
							thisRequest.getParameterAsString("name")
							,thisRequest.getParameterAsString("author")
							,TaxonRanks.getRankFromValue(thisRequest.getParameterAsInteger("rank",null)),null,null,true).getID()).toJsonObject()
				);
				//success(output, graph.dbNodeWorker.createTaxEntNode(name, author, TaxonRanks.getRankFromValue(Integer.parseInt(rank)), null, true).toJsonObject(), includeHeaders);
				return;
			
			case "completeterritory":
				if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES()) {thisRequest.error("You don't have privileges for this operation!"); return;}
				res = driver.wrapTaxEnt(thisRequest.getParameterAsKey("id")).setTerritoryWithCompleteDistribution(thisRequest.getParameterAsKey("territory"));
				thisRequest.success(res == 0 ? "Nothing added" : "Added");
				return;

			case "habitat":
				if(!thisRequest.getUser().canMANAGE_HABITATS()) {thisRequest.error("You don't have privileges for this operation!"); return;}
				newHab = new Habitat(
						thisRequest.getParameterAsString("name")
						, thisRequest.getParameterAsString("description")
						, thisRequest.getParameterAsEnum("facet", Constants.HabitatFacet.class)
				);
				newHab.setLevel(thisRequest.getParameterAsInteger("level", null));
				newHab = NWD.createNode(Habitat.class, newHab);
				thisRequest.success(new GraphUpdateResult(driver, newHab.getID()).toJsonObject());
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
				thisRequest.error("Invalid node type");
				return;
			}
			
		case "update":
			if(!partIt.hasNext()) {
				thisRequest.error("Choose the node type: taxent");
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
				TaxEnt te = new TaxEnt(
						thisRequest.getParameterAsString("name")
						,thisRequest.getParameterAsInteger("rank",null)
						,thisRequest.getParameterAsString("author")
						,thisRequest.getParameterAsString("sensu")
						,thisRequest.getParameterAsString("annotation")
						,thisRequest.getParameterAsBoolean("current")
						,null
						,thisRequest.getParameterAsEnum("worldDistributionCompleteness", WorldNativeDistributionCompleteness.class)
						,thisRequest.getParameterAsInteger("oldId", null)
				);
				te.setComment(thisRequest.getParameterAsString("comment"));
				thisRequest.success(NWD.updateTaxEntNode(thisRequest.getParameterAsKey("id"), te
						, thisRequest.getParameterAsBooleanNoNull("replace")).toJsonObject());
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
				if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES()) {thisRequest.error("You don't have privileges for this operation!"); return;}
				thisRequest.success(NWD.updateTerritoryNode(
					NWD.getNode(thisRequest.getParameterAsKey("id"), Territory.class)
					, thisRequest.getParameterAsString("name")
					, thisRequest.getParameterAsString("shortName")
					, thisRequest.getParameterAsEnum("type", TerritoryTypes.class)
					, thisRequest.getParameterAsString("theme")
					, thisRequest.getParameterAsBoolean("checklist", true)
					).toJsonObject()
				);
				return;

			case "habitat":
				if(!thisRequest.getUser().canMANAGE_HABITATS()) {thisRequest.error("You don't have privileges for this operation!"); return;}
				newHab = new Habitat(
					thisRequest.getParameterAsString("name")
					, thisRequest.getParameterAsString("description")
					, thisRequest.getParameterAsEnum("facet", Constants.HabitatFacet.class)
				);
				newHab.setLevel(thisRequest.getParameterAsInteger("level", null));
				newHab = NWD.updateDocument(thisRequest.getParameterAsKey("id"), newHab, false, Habitat.class);
				thisRequest.success(new GraphUpdateResult(driver, newHab.getID()).toJsonObject());
				break;

			default:
				thisRequest.error("Invalid node type");
				return;
			}
		}
	}
}
