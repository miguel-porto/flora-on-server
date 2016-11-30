package pt.floraon.checklistmanager;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.AbundanceLevel;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.driver.Constants.PlantIntroducedStatus;
import pt.floraon.driver.Constants.PlantNaturalizationDegree;
import pt.floraon.entities.Territory;
import pt.floraon.server.FloraOnServlet;
import pt.floraon.entities.User;

@MultipartConfig
public class Territories extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
		if(!getUser().canMODIFY_TAXA_TERRITORIES()) {
			error("You must login to do this operation!");
			return;
		}

		ListIterator<String> part=this.getPathIteratorAfter("territories");
		String to;

		if(!part.hasNext()) {
			error("Choose one of: set");
			return;
		}
		switch(part.next()) {
		case "set":
			INodeKey from;
			errorIfAnyNull(response,
				from = getParameterAsKey("taxon"),		// the taxon id
				to = getParameterAsString("territory"));

			Territory terr=NWD.getTerritoryFromShortName(to);
			if(terr == null) throw new FloraOnException("Territory not found");
			int res = driver.wrapTaxEnt(from).setNativeStatus(
				driver.asNodeKey(terr.getID())
				, getParameterAsEnum("nativeStatus", NativeStatus.class)
				, getParameterAsEnum("occurrenceStatus", OccurrenceStatus.class)
				, getParameterAsEnum("abundanceLevel", AbundanceLevel.class)
				, getParameterAsEnum("introducedStatus", PlantIntroducedStatus.class)
				, getParameterAsEnum("naturalizationDegree", PlantNaturalizationDegree.class)
				, getParameterAsBooleanNoNull("uncertain")
			);
			success( res==0 ? "Nothing changed" : "Set ok!");
			break;
			
		default:
			error("Command not found");
			break;
		}

	}
}
