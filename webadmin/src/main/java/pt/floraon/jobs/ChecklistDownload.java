package pt.floraon.jobs;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnDriver;
import pt.floraon.driver.FloraOnException;
import pt.floraon.entities.TerritoryVertex;
import pt.floraon.results.NamesAndTerritoriesResult;
import pt.floraon.results.ResultProcessor;

public class ChecklistDownload implements Job {
	@Override
	public void run(FloraOnDriver driver, OutputStream outputStream) throws ArangoException, FloraOnException {
		PrintWriter out=new PrintWriter(outputStream);
		List<String> terr=new ArrayList<String>();
		for(TerritoryVertex tv : driver.checklistTerritories)
			terr.add(tv.getShortName());
		
		ResultProcessor<NamesAndTerritoriesResult> rpchk1;
		Iterator<NamesAndTerritoriesResult> chklst=driver.dbSpecificQueries.getAllSpeciesOrInferior(true, NamesAndTerritoriesResult.class, null, null, null);
		rpchk1=(ResultProcessor<NamesAndTerritoriesResult>) new ResultProcessor<NamesAndTerritoriesResult>(chklst);
		out.print(rpchk1.toCSVTable(terr));
		out.close();
	}
}
