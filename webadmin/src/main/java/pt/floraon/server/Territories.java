package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.INodeKey;
import pt.floraon.driver.Constants.NativeStatus;
import pt.floraon.driver.Constants.OccurrenceStatus;
import pt.floraon.entities.Territory;

@MultipartConfig
public class Territories extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnPost() throws ServletException, IOException, FloraOnException {
		if(!isAuthenticated()) {
			error("You must login to do this operation!");
			return;
		}

		ListIterator<String> part=this.getPathIteratorAfter("territories");
		String to, tmp1, tmp2;

		if(!part.hasNext()) {
			error("Choose one of: set");
			return;
		}
		switch(part.next()) {
		case "set":
			INodeKey from=getParameterAsKey("taxon");		// the taxon id
			to=getParameterAsString("territory");
			tmp1=getParameterAsString("nativeStatus");
			tmp2=getParameterAsString("occurrenceStatus");
			if(errorIfAnyNull(response, from, to, tmp1)) return;
			Territory terr=NWD.getTerritoryFromShortName(to);
			NativeStatus nstatus=null;
			OccurrenceStatus ostatus=null;
			if(!tmp1.toUpperCase().equals("NULL")) {
				nstatus=NativeStatus.fromString(tmp1.toUpperCase());
				ostatus=tmp2==null ? null : OccurrenceStatus.valueOf(tmp2.toUpperCase());
			}
			driver.wrapTaxEnt(from).setNativeStatus(driver.asNodeKey(terr.getID()), nstatus, ostatus, getParameterAsBoolean("uncertain"));
			success( nstatus==null ? "NULL" : nstatus.toString().toUpperCase());
			break;
			
		default:
			error("Command not found");
			break;
		}

	}
}
