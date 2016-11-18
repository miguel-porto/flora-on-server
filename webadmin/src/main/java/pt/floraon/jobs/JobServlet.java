package pt.floraon.jobs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ListIterator;

import javax.servlet.ServletException;

import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import pt.floraon.driver.FloraOnException;
import pt.floraon.server.FloraOnServlet;

public class JobServlet extends FloraOnServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		ListIterator<String> partIt=this.getPathIterator();
		while(!partIt.next().equals("job"));

		JobRunner job = JobSubmitter.getJob(partIt.next());
		if(job == null) throw new FloraOnException("Job not found");
		if(getParameterAsString("query") != null) {
			JsonObject resp = new JsonObject();
			resp.addProperty("ready", job.isReady());
			resp.addProperty("msg", job.getState());
			success(job.isReady().toString());
			return;
		}

		if(job.isFileDownload()) {
			JobRunnerFileDownload jobFD = (JobRunnerFileDownload) job;
			InputStreamReader jobInput = jobFD.getInputStreamReader(StandardCharsets.UTF_8);
			switch (jobFD.getFileType().toLowerCase()) {
				case "html":
				case "htm":
					response.setContentType("text/html; charset=utf-8");
					break;
				case "csv":
					//response.setContentType("text/csv; charset=Windows-1252");
					response.setContentType("text/csv; charset=utf-8");
					response.addHeader("Content-Disposition", "attachment;Filename=\"" + jobFD.getFileName() + "\"");
					break;
			}
			response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
			//IOUtils.copy(jobInput, response.getOutputStream());
			IOUtils.copy(jobInput, response.getWriter());
		} else {

		}
	}
}
