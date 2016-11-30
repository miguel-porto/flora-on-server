package pt.floraon.jobs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;
import pt.floraon.entities.Territory;
import pt.floraon.results.TaxEntAndNativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class ChecklistDownload implements JobFileDownload {
	private boolean finished = false;

	@Override
	public void run(IFloraOn driver, OutputStream outputStream) throws FloraOnException, IOException {
		OutputStreamWriter out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
		//PrintWriter out=new PrintWriter(outputStream);
		List<String> terr=new ArrayList<String>();
		for(Territory tv : driver.getChecklistTerritories())
			terr.add(tv.getShortName());
		
		ResultProcessor<TaxEntAndNativeStatusResult> rpchk1;
		Iterator<TaxEntAndNativeStatusResult> chklst = driver.getListDriver().getAllSpeciesOrInferior(true, TaxEntAndNativeStatusResult.class, true, null, null, null, null).iterator();
		rpchk1=(ResultProcessor<TaxEntAndNativeStatusResult>) new ResultProcessor<TaxEntAndNativeStatusResult>(chklst);
		out.write(rpchk1.toCSVTable(terr));
		
		//out.print(rpchk1.toCSVTable(terr));
		out.close();
		finished = true;
	}

	@Override
	public String getState() {
		return finished ? "Ok" : "Processing";
	}
}
