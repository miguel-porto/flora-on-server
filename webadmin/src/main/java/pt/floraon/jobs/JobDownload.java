package pt.floraon.jobs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ListIterator;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import pt.floraon.driver.FloraOnException;
import pt.floraon.server.FloraOnServlet;

public class JobDownload extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		String format=getParameterAsString("query");
		ListIterator<String> partIt=this.getPathIterator();
		while(!partIt.next().equals("job"));

		JobRunner job1=JobSubmitter.getJob(partIt.next());
		if(job1==null) throw new FloraOnException("Job not found");
		if(format!=null) {
			success(job1.isReady().toString());
			return;
		}
		InputStreamReader jobInput=job1.getInputStreamReader(StandardCharsets.UTF_8);
		switch(job1.getFileType().toLowerCase()) {
		case "html":
		case "htm":
			response.setContentType("text/html; charset=utf-8");
			break;
		case "csv":
			response.setContentType("text/csv; charset=Windows-1252");
			response.addHeader("Content-Disposition", "attachment;Filename=\""+job1.getFileName()+"\"");
			break;
		}
		IOUtils.copy(jobInput, response.getOutputStream());
	}
}
