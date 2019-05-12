package pt.floraon.driver.jobs;

import java.io.IOException;
import java.util.*;

import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.occurrences.OccurrenceImporterJob;

public final class JobSubmitter {
	protected static Map<String, JobRunner> jobs=new HashMap<String, JobRunner>();
	
	public static JobRunnerFileDownload newJobFileDownload(JobFileDownload job, String desiredFileName, IFloraOn driver) throws IOException {
		JobRunnerFileDownload j=new JobRunnerFileDownload(job, desiredFileName, driver);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}

	public static JobRunnerTask newJobTask(JobTask job, IFloraOn driver) throws IOException {
		JobRunnerTask j = new JobRunnerTask(job, driver);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}

	public static JobRunner getJob(String uuid) {
		return JobSubmitter.jobs.get(uuid);
	}

	public static Set<String> getJobList() {
		return jobs.keySet();
	}

	public static List<JobRunner> getJobsOfType(Class<? extends Job> cl) {
		List<JobRunner> out = new ArrayList<>();
		for(Map.Entry<String, JobRunner> e : jobs.entrySet()) {
			if(cl.isInstance(e.getValue().getJob())) {
				out.add(e.getValue());
			}
		}
		return out;
	}
}
