package pt.floraon.taxonomy.servlets;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.driver.Constants.AbundanceLevel;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PlantIntroducedStatus;
import pt.floraon.driver.Constants.PlantNaturalizationDegree;
import pt.floraon.taxonomy.entities.Territory;
import pt.floraon.server.FloraOnServlet;

@MultipartConfig
public class Territories extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		if(!thisRequest.getUser().canMODIFY_TAXA_TERRITORIES()) {
			thisRequest.error("You must login to do this operation!");
			return;
		}

		ListIterator<String> part=thisRequest.getPathIteratorAfter("territories");
		String to;

		if(!part.hasNext()) {
			thisRequest.error("Choose one of: set");
			return;
		}
		switch(part.next()) {
		case "set":
			INodeKey from;
			errorIfAnyNull(thisRequest.response,
				from = thisRequest.getParameterAsKey("taxon"),		// the taxon id
				to = thisRequest.getParameterAsString("territory"));

			Territory terr=NWD.getTerritoryFromShortName(to);
			if(terr == null) throw new FloraOnException("Territory not found");
			int res = driver.wrapTaxEnt(from).setNativeStatus(
				driver.asNodeKey(terr.getID())
				, thisRequest.getParameterAsEnum("nativeStatus", NativeStatus.class)
				, thisRequest.getParameterAsEnum("occurrenceStatus", OccurrenceStatus.class)
				, thisRequest.getParameterAsEnum("abundanceLevel", AbundanceLevel.class)
				, thisRequest.getParameterAsEnum("introducedStatus", PlantIntroducedStatus.class)
				, thisRequest.getParameterAsEnum("naturalizationDegree", PlantNaturalizationDegree.class)
				, thisRequest.getParameterAsBooleanNoNull("uncertain")
			);
			thisRequest.success( res==0 ? "Nothing changed" : "Set ok!");
			break;
			
		default:
			thisRequest.error("Command not found");
			break;
		}

	}
}
