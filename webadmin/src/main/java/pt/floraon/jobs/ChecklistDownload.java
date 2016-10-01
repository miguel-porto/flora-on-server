package pt.floraon.jobs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.FloraOn;
import pt.floraon.entities.Territory;
import pt.floraon.results.TaxEntAndNativeStatusResult;
import pt.floraon.results.ResultProcessor;

public class ChecklistDownload implements Job {
	@Override
	public void run(FloraOn driver, OutputStream outputStream) throws FloraOnException, IOException {
		OutputStreamWriter out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
		//PrintWriter out=new PrintWriter(outputStream);
		List<String> terr=new ArrayList<String>();
		for(Territory tv : driver.getChecklistTerritories())
			terr.add(tv.getShortName());
		
		ResultProcessor<TaxEntAndNativeStatusResult> rpchk1;
		Iterator<TaxEntAndNativeStatusResult> chklst=driver.getListDriver().getAllSpeciesOrInferior(true, TaxEntAndNativeStatusResult.class, true, null, null, null, null);
		rpchk1=(ResultProcessor<TaxEntAndNativeStatusResult>) new ResultProcessor<TaxEntAndNativeStatusResult>(chklst);
		out.write(rpchk1.toCSVTable(terr));
		
		//out.print(rpchk1.toCSVTable(terr));
		out.close();
	}
}
