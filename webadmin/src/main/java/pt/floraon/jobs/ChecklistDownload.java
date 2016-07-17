package pt.floraon.jobs;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.ArangoException;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.entities.Territory;
import pt.floraon.results.TaxEntAndNativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class ChecklistDownload implements Job {
	@Override
	public void run(FloraOn driver, OutputStream outputStream) throws ArangoException, FloraOnException {
		PrintWriter out=new PrintWriter(outputStream);
		List<String> terr=new ArrayList<String>();
		for(Territory tv : driver.getChecklistTerritories())
			terr.add(tv.getShortName());
		
		ResultProcessor<TaxEntAndNativeStatusResult> rpchk1;
		Iterator<TaxEntAndNativeStatusResult> chklst=driver.getListDriver().getAllSpeciesOrInferior(true, TaxEntAndNativeStatusResult.class, true, null, null, null, null);
		rpchk1=(ResultProcessor<TaxEntAndNativeStatusResult>) new ResultProcessor<TaxEntAndNativeStatusResult>(chklst);
		out.print(rpchk1.toCSVTable(terr));
		out.close();
	}
}
